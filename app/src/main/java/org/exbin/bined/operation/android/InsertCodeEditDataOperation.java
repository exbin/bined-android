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

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.operation.BinaryDataAppendableOperation;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.BinaryDataUndoableOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation for editing data using insert mode.
 * <p>
 * At zero offset byte is inserted, otherwise part of the value is overwritten.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertCodeEditDataOperation extends CodeEditDataOperation {

    protected final long position;
    protected final int codeOffset;
    protected boolean trailing = false;
    @Nonnull
    protected final CodeType codeType;
    protected byte value;

    public InsertCodeEditDataOperation(long position, int codeOffset, byte value, CodeType codeType) {
        this.value = value;
        this.codeType = codeType;
        this.position = position;
        this.codeOffset = codeOffset;
    }

    @Nonnull
    @Override
    public BasicBinaryDataOperationType getType() {
        return BasicBinaryDataOperationType.EDIT_DATA;
    }

    @Nonnull
    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void execute(EditableBinaryData contentData) {
        execute(contentData, false);
    }

    @Nonnull
    @Override
    public BinaryDataUndoableOperation executeWithUndo(EditableBinaryData contentData) {
        return CodeAreaUtils.requireNonNull(execute(contentData, true));
    }

    @Nullable
    private BinaryDataUndoableOperation execute(EditableBinaryData contentData, boolean withUndo) {
        if (position > contentData.getDataSize() || (position == contentData.getDataSize() && codeOffset > 0)) {
            throw new IllegalStateException("Cannot overwrite outside of the document");
        }

        BinaryDataUndoableOperation undoOperation = null;

        if (codeOffset > 0) {
            byte byteValue = contentData.getByte(position);
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
            /* if (byteRest > 0) {
                if (trailing) {
                    throw new IllegalStateException("Unexpected trailing flag");
                }
                trailingValue = (EditableBinaryData) contentData.copy(position, 1);
                contentData.insert(editedDataPosition, 1);
                contentData.setByte(editedDataPosition, byteRest);
                byteValue -= byteRest;
                trailing = true;
            } */
            byteValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
            contentData.setByte(position, byteValue);

            if (withUndo) {
                undoOperation = new UndoOperation(position, codeType, codeOffset, 1);
            }
        } else {
            if (withUndo) {
                undoOperation = new UndoOperation(position, codeType, codeOffset, 1);
            }
            byte byteValue = CodeAreaUtils.setCodeValue((byte) 0, value, codeOffset, codeType);
            contentData.insertUninitialized(position, 1);
            contentData.setByte(position, byteValue);
        }

        return undoOperation;
    }

    public long getPosition() {
        return position;
    }

    public int getCodeOffset() {
        return codeOffset;
    }

    public boolean isLastOffset() {
        return codeOffset == codeType.getMaxDigitsForByte() - 1;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Appendable variant of RemoveDataOperation.
     */
    @ParametersAreNonnullByDefault
    private static class UndoOperation implements BinaryDataUndoableOperation, BinaryDataAppendableOperation {

        private final long position;
        private final CodeType codeType;
        private int codeOffset;
        private long length;

        public UndoOperation(long position, CodeType codeType, int codeOffset, long length) {
            this.position = position;
            this.codeType = codeType;
            this.codeOffset = codeOffset;
            this.length = length;
        }

        @Nonnull
        @Override
        public BasicBinaryDataOperationType getType() {
            return BasicBinaryDataOperationType.REMOVE_DATA;
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
        public void execute(EditableBinaryData contentData) {
            execute(contentData, false);
        }

        @Nonnull
        @Override
        public BinaryDataUndoableOperation executeWithUndo(EditableBinaryData contentData) {
            return CodeAreaUtils.requireNonNull(execute(contentData, true));
        }

        @Nullable
        private BinaryDataUndoableOperation execute(EditableBinaryData contentData, boolean withUndo) {
            BinaryDataUndoableOperation undoOperation = null;
            if (withUndo) {
                EditableBinaryData undoData = (EditableBinaryData) contentData.copy(position, length);
                undoOperation = new InsertDataOperation(position, codeOffset, undoData);
            }
            contentData.remove(position, length);
            return undoOperation;
        }

        @Override
        public void dispose() {
        }
    }
}
