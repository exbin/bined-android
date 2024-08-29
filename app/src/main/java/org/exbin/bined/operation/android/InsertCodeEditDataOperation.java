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
import org.exbin.bined.CodeType;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data using insert mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private boolean trailing = false;
    private EditableBinaryData trailingValue = null;
    private final CodeType codeType;
    private byte value;

    private long length;
    private int codeOffset = 0;

    public InsertCodeEditDataOperation(CodeAreaCore codeArea, long startPosition, int startCodeOffset, byte value) {
        super(codeArea);
        this.value = value;
        codeType = ((CodeTypeCapable) codeArea).getCodeType();
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        if (codeOffset > 0) {
            length = 1;
        }
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
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        if (startPosition > data.getDataSize() || (startPosition == data.getDataSize() && codeOffset > 0)) {
            throw new IllegalStateException("Cannot overwrite outside of the document");
        }

        long editedDataPosition = startPosition + length;
        CodeAreaOperation undoOperation = null;

        byte byteValue = 0;
        if (codeOffset > 0) {
            byteValue = data.getByte(editedDataPosition - 1);
            byte byteRest = 0;
            switch (codeType) {
                case BINARY: {
                    byteRest = (byte) (byteValue & (0xff >> codeOffset));
                    break;
                }
                case DECIMAL: {
                    byteRest = (byte) (byteValue % (codeOffset == 1 ? 100 : 10));
                    break;
                }
                case OCTAL: {
                    byteRest = (byte) (byteValue % (codeOffset == 1 ? 64 : 8));
                    break;
                }
                case HEXADECIMAL: {
                    byteRest = (byte) (byteValue & 0xf);
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(codeType);
            }
            if (byteRest > 0) {
                if (trailing) {
                    throw new IllegalStateException("Unexpected trailing flag");
                }
                trailingValue = (EditableBinaryData) data.copy(editedDataPosition - 1, 1);
                data.insert(editedDataPosition, 1);
                data.setByte(editedDataPosition, byteRest);
                byteValue -= byteRest;
                trailing = true;
            }
            editedDataPosition--;
        } else {
            data.insert(editedDataPosition, 1);
            length++;
        }

        byteValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        data.setByte(editedDataPosition, byteValue);

        if (withUndo) {
            undoOperation = new UndoOperation(codeArea, startPosition, codeType, codeOffset, length);
        }

        return undoOperation;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public int getStartCodeOffset() {
        return startCodeOffset;
    }

    public long getLength() {
        return length;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (trailingValue != null) {
            trailingValue.dispose();
        }
    }

    /**
     * Appendable variant of RemoveDataOperation.
     */
    @ParametersAreNonnullByDefault
    private static class UndoOperation extends CodeAreaOperation implements BinaryDataAppendableOperation {

        private final long position;
        private final CodeType codeType;
        private int codeOffset;
        private long length;

        public UndoOperation(CodeAreaCore codeArea, long position, CodeType codeType, int codeOffset, long length) {
            super(codeArea);
            this.position = position;
            this.codeType = codeType;
            this.codeOffset = codeOffset;
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
                codeOffset++;
                if (codeOffset == codeType.getMaxDigitsForByte()) {
                    codeOffset = 0;
                    length += ((UndoOperation) operation).length;
                }
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
                undoOperation = new InsertDataOperation(codeArea, position, codeOffset, undoData);
            }
            contentData.remove(position, length);
            ((CaretCapable) codeArea).setActiveCaretPosition(position, codeOffset);
            return undoOperation;
        }
    }
}
