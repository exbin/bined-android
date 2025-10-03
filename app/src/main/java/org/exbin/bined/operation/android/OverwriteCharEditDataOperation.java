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
import org.exbin.bined.operation.BinaryDataAppendableOperation;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.BinaryDataUndoableOperation;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation for editing data using overwrite mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OverwriteCharEditDataOperation extends CharEditDataOperation {

    protected final long startPosition;
    protected char value;
    protected Charset charset;
    protected int charLength;

    public OverwriteCharEditDataOperation(long startPosition, char value, Charset charset) {
        this.value = value;
        this.startPosition = startPosition;
        this.charset = charset;
    }

    @Nonnull
    @Override
    public BasicBinaryDataOperationType getType() {
        return BasicBinaryDataOperationType.EDIT_DATA;
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
        EditableBinaryData undoData;

        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        charLength = bytes.length;
        if (startPosition < contentData.getDataSize()) {
            long overwritten = contentData.getDataSize() - startPosition;
            if (overwritten > charLength) {
                overwritten = charLength;
            }
            undoData = (EditableBinaryData) contentData.copy(startPosition, overwritten);
            for (int i = 0; i < overwritten; i++) {
                contentData.setByte(startPosition + i, bytes[i]);
            }
        } else {
            undoData = (EditableBinaryData) contentData.copy(startPosition, 0);
        }

        if (startPosition + charLength > contentData.getDataSize()) {
            if (startPosition == contentData.getDataSize()) {
                contentData.insert(startPosition, bytes);
            } else {
                int inserted = (int) (startPosition + charLength - contentData.getDataSize());
                long insertPosition = startPosition + charLength - inserted;
                contentData.insert(insertPosition, inserted);
                for (int i = 0; i < inserted; i++) {
                    contentData.setByte(insertPosition + i, bytes[charLength - inserted + i]);
                }
            }
        }

        if (withUndo) {
            undoOperation = new UndoOperation(startPosition, undoData, charLength - undoData.getDataSize());
        }

        return undoOperation;
    }

    public int getCharLength() {
        return charLength;
    }

    @ParametersAreNonnullByDefault
    private static class UndoOperation implements BinaryDataUndoableOperation, BinaryDataAppendableOperation {

        private final long position;
        private final BinaryData data;
        private long removeLength;

        public UndoOperation(long position, BinaryData data, long removeLength) {
            this.position = position;
            this.data = data;
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
            if (operation instanceof UndoOperation) {
                ((EditableBinaryData) data).insert(data.getDataSize(), ((UndoOperation) operation).data);
                removeLength += ((UndoOperation) operation).removeLength;
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
