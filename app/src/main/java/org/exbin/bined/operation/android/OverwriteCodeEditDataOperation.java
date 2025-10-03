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

import org.exbin.auxiliary.binary_data.BinaryData;
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
 * Operation for editing data using overwrite mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OverwriteCodeEditDataOperation extends CodeEditDataOperation {

    protected final long position;
    protected final int codeOffset;
    @Nonnull
    protected final CodeType codeType;
    protected byte value;

    public OverwriteCodeEditDataOperation(long position, int codeOffset, CodeType codeType, byte value) {
        this.value = value;
        this.position = position;
        this.codeOffset = codeOffset;
        this.codeType = codeType;
    }

    @Nonnull
    @Override
    public BasicBinaryDataOperationType getType() {
        return BasicBinaryDataOperationType.EDIT_DATA;
    }

    public long getPosition() {
        return position;
    }

    public int getCodeOffset() {
        return codeOffset;
    }

    @Nonnull
    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    public boolean isLastOffset() {
        return codeOffset == codeType.getMaxDigitsForByte() - 1;
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
        EditableBinaryData undoData;

        byte byteValue = 0;
        int removeLength = 0;
        if (codeOffset > 0) {
            undoData = (EditableBinaryData) contentData.copy(position, 1);
            byteValue = undoData.getByte(0);
        } else {
            if (position < contentData.getDataSize()) {
                undoData = (EditableBinaryData) contentData.copy(position, 1);
                byteValue = undoData.getByte(0);
            } else {
                undoData = (EditableBinaryData) contentData.copy(position, 0);
                contentData.insertUninitialized(position, 1);
                removeLength = 1;
            }
        }

        byteValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);

        contentData.setByte(position, byteValue);

        if (withUndo) {
            undoOperation = new UndoOperation(position, undoData, codeType, codeOffset, removeLength);
        }

        return undoOperation;
    }

    @ParametersAreNonnullByDefault
    private static class UndoOperation implements BinaryDataUndoableOperation, BinaryDataAppendableOperation {

        private final long position;
        private final BinaryData data;
        private final CodeType codeType;
        private int codeOffset;
        private long removeLength;

        public UndoOperation(long position, BinaryData data, CodeType codeType, int codeOffset, long removeLength) {
            this.position = position;
            this.data = data;
            this.codeType = codeType;
            this.codeOffset = codeOffset;
            this.removeLength = removeLength;
        }

        @Nonnull
        @Override
        public BasicBinaryDataOperationType getType() {
            return BasicBinaryDataOperationType.MODIFY_DATA;
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

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation && (((UndoOperation) operation).codeType == codeType)) {
                codeOffset++;
                if (codeOffset == codeType.getMaxDigitsForByte()) {
                    codeOffset = 0;
                    removeLength += ((UndoOperation) operation).removeLength;
                    ((EditableBinaryData) data).insert(data.getDataSize(), ((UndoOperation) operation).data);
                }
                return true;
            }

            return false;
        }

        @Nullable
        private BinaryDataUndoableOperation execute(EditableBinaryData contentData, boolean withUndo) {
            BinaryDataUndoableOperation undoOperation = null;
            RemoveDataOperation removeOperation = null;
            if (removeLength > 0) {
                removeOperation = new RemoveDataOperation(position + data.getDataSize(), 0, removeLength);
            }

            if (withUndo) {
                BinaryData undoData = contentData.copy(position, data.getDataSize());
                undoOperation = new ModifyDataOperation(position, undoData);
            }
            contentData.replace(position, data);
            if (removeOperation != null) {
                if (withUndo) {
                    BasicBinaryDataCompoundOperation compoundOperation = new BasicBinaryDataCompoundOperation();
                    compoundOperation.addOperation(removeOperation.executeWithUndo(contentData));
                    compoundOperation.addOperation(undoOperation);
                    undoOperation = compoundOperation;
                } else {
                    removeOperation.execute(contentData);
                }
            }
            return undoOperation;
        }

        @Override
        public void dispose() {
        }
    }
}
