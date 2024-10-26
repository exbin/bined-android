/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.operation.android;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeType;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data in delete mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeleteCodeEditDataOperation extends CodeEditDataOperation {

    protected static final char BACKSPACE_CHAR = '\b';
    protected static final char DELETE_CHAR = (char) 0x7f;
    protected final CodeType codeType;

    protected long position;
    protected byte value;

    public DeleteCodeEditDataOperation(CodeAreaCore codeArea, long startPosition, CodeType codeType, byte value) {
        super(codeArea);
        this.value = value;
        this.codeType = codeType;
        this.position = startPosition;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Nonnull
    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void execute() {
        execute(false);
    }

    @Nonnull
    @Override
    public BinaryDataUndoableOperation executeWithUndo() {
        return execute(true);
    }

    private CodeAreaOperation execute(boolean withUndo) {
        CodeAreaOperation undoOperation = null;
        EditableBinaryData undoData = null;

        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        switch (value) {
            case BACKSPACE_CHAR: {
                if (position > 0) {
                    position--;
                    undoData = (EditableBinaryData) data.copy(position, 1);
                    data.remove(position, 1);
                }
                break;
            }
            case DELETE_CHAR: {
                if (position < data.getDataSize()) {
                    undoData = (EditableBinaryData) data.copy(position, 1);
                    data.remove(position, 1);
                }
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected character " + value);
            }
        }
        ((CaretCapable) codeArea).setActiveCaretPosition(position);
        codeArea.repaint();

        if (withUndo) {
            undoOperation = new UndoOperation(codeArea, position, 0, undoData, value);
        }
        return undoOperation;
    }

    @ParametersAreNonnullByDefault
    private static class UndoOperation extends InsertDataOperation implements BinaryDataAppendableOperation {

        private byte value;

        public UndoOperation(CodeAreaCore codeArea, long position, int codeOffset, BinaryData data, byte value) {
            super(codeArea, position, codeOffset, data);
            this.value = value;
        }

        @Nonnull
        @Override
        public CodeAreaOperationType getType() {
            return CodeAreaOperationType.EDIT_DATA;
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation && ((UndoOperation) operation).value == value) {
                EditableBinaryData data = (EditableBinaryData) getData();
                switch (value) {
                    case BACKSPACE_CHAR: {
                        data.insert(0, ((UndoOperation) operation).getData());
                        position--;
                        break;
                    }
                    case DELETE_CHAR: {
                        data.insert(data.getDataSize(), ((UndoOperation) operation).getData());
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unexpected character " + value);
                    }
                }
                return true;
            }

            return false;
        }
    }
}
