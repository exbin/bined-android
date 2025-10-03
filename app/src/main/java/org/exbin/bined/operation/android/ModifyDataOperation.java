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
import org.exbin.bined.operation.BinaryDataUndoableOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Operation for modifying data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ModifyDataOperation implements BinaryDataUndoableOperation {

    protected final long position;
    @Nonnull
    protected final BinaryData data;

    public ModifyDataOperation(long position, BinaryData data) {
        this.position = position;
        this.data = data;
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

    @Nullable
    private BinaryDataUndoableOperation execute(EditableBinaryData contentData, boolean withUndo) {
        BinaryDataUndoableOperation undoOperation = null;
        if (withUndo) {
            BinaryData undoData = contentData.copy(position, data.getDataSize());
            undoOperation = new ModifyDataOperation(position, undoData);
        }
        contentData.replace(position, data);
        return undoOperation;
    }

    @Override
    public void dispose() {
        data.dispose();
    }
}
