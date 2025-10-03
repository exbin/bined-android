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
import org.exbin.bined.operation.BinaryDataUndoableOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation for deleting section of data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class RemoveDataOperation implements BinaryDataUndoableOperation {

    protected final long position;
    protected final int codeOffset;
    protected final long length;

    public RemoveDataOperation(long position, int codeOffset, long length) {
        this.position = position;
        this.codeOffset = codeOffset;
        this.length = length;
    }

    @Nonnull
    @Override
    public BasicBinaryDataOperationType getType() {
        return BasicBinaryDataOperationType.REMOVE_DATA;
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
