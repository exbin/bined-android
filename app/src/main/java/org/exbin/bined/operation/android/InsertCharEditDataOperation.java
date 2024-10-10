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

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data using insert mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertCharEditDataOperation extends CharEditDataOperation {

    protected final long startPosition;
    protected final char value;

    public InsertCharEditDataOperation(CodeAreaCore coreArea, long startPosition, char value) {
        super(coreArea);
        this.value = value;
        this.startPosition = startPosition;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
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
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        Charset charset = ((CharsetCapable) codeArea).getCharset();
        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        data.insert(startPosition, bytes);
        long length = bytes.length;
        long dataPosition = startPosition + length;
        ((CaretCapable) codeArea).setActiveCaretPosition(dataPosition);
        ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);

        if (withUndo) {
            undoOperation = new UndoOperation(codeArea, startPosition, length);
        }

        return undoOperation;
    }

    public long getStartPosition() {
        return startPosition;
    }

    /**
     * Appendable variant of RemoveDataOperation.
     */
    @ParametersAreNonnullByDefault
    private static class UndoOperation extends CodeAreaOperation implements BinaryDataAppendableOperation {

        private final long position;
        private long length;

        public UndoOperation(CodeAreaCore codeArea, long position, long length) {
            super(codeArea);
            this.position = position;
            this.length = length;
        }

        @Nonnull
        @Override
        public CodeAreaOperationType getType() {
            return CodeAreaOperationType.REMOVE_DATA;
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation) {
                length += ((UndoOperation) operation).length;
                return true;
            }

            return false;
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
            EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
            CodeAreaOperation undoOperation = null;
            if (withUndo) {
                EditableBinaryData undoData = (EditableBinaryData) contentData.copy(position, length);
                undoOperation = new InsertDataOperation(codeArea, position, 0, undoData);
            }
            contentData.remove(position, length);
            ((CaretCapable) codeArea).setActiveCaretPosition(position, 0);
            return undoOperation;
        }
    }
}
