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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation for deleting data via delete or backspace.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeleteEditDataOperation extends CharEditDataOperation {

    public static final char BACKSPACE_CHAR = '\b';
    public static final char DELETE_CHAR = (char) 0x7f;

    protected long position;
    protected char value;

    public DeleteEditDataOperation(long startPosition, char value) {
        this.value = value;
        this.position = startPosition;
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
        EditableBinaryData undoData = null;

        switch (value) {
            case BACKSPACE_CHAR: {
                if (position <= 0) {
                    throw new IllegalStateException("Cannot apply backspace on position " + position);
                }

                position--;
                undoData = (EditableBinaryData) contentData.copy(position, 1);
                contentData.remove(position, 1);
                break;
            }
            case DELETE_CHAR: {
                if (position >= contentData.getDataSize()) {
                    throw new IllegalStateException("Cannot apply delete on position " + position);
                }

                undoData = (EditableBinaryData) contentData.copy(position, 1);
                contentData.remove(position, 1);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected character " + value);
            }
        }

        if (withUndo) {
            undoOperation = new DeleteEditUndoOperation(position, undoData, value);
        }
        return undoOperation;
    }

    public boolean isBackSpace() {
        return value == BACKSPACE_CHAR;
    }

    /**
     * Appendable variant to merge sequence of deletion sequence into single
     * undo step.
     */
    @ParametersAreNonnullByDefault
    private static class DeleteEditUndoOperation extends InsertDataOperation implements BinaryDataAppendableOperation {

        private char value;

        public DeleteEditUndoOperation(long position, BinaryData data, char value) {
            super(position, 0, data);
            this.value = value;
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof DeleteEditUndoOperation) {
                if (((DeleteEditUndoOperation) operation).position != position) {
                    return false;
                }

                EditableBinaryData editableData = (EditableBinaryData) data;
                switch (((DeleteEditUndoOperation) operation).value) {
                    case BACKSPACE_CHAR: {
                        editableData.insert(0, ((DeleteEditUndoOperation) operation).getData());
                        position--;
                        break;
                    }
                    case DELETE_CHAR: {
                        editableData.insert(editableData.getDataSize(), ((DeleteEditUndoOperation) operation).getData());
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
