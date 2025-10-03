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
import org.exbin.bined.operation.BinaryDataAppendableOperation;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.BinaryDataUndoableOperation;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation for editing data using insert mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertCharEditDataOperation extends CharEditDataOperation {

    protected final long startPosition;
    protected final char value;
    protected final Charset charset;
    protected int charLength;

    public InsertCharEditDataOperation(long startPosition, char value, Charset charset) {
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
        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        contentData.insert(startPosition, bytes);
        charLength = bytes.length;

        if (withUndo) {
            undoOperation = new UndoOperation(startPosition, charLength);
        }

        return undoOperation;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public int getCharLength() {
        return charLength;
    }

    /**
     * Appendable variant of RemoveDataOperation.
     */
    @ParametersAreNonnullByDefault
    private static class UndoOperation implements BinaryDataUndoableOperation, BinaryDataAppendableOperation {

        private final long position;
        private long length;

        public UndoOperation(long position, long length) {
            this.position = position;
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
                length += ((UndoOperation) operation).length;
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
                undoOperation = new InsertDataOperation(position, 0, undoData);
            }
            contentData.remove(position, length);
            return undoOperation;
        }

        @Override
        public void dispose() {
        }
    }
}
