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
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for modifying data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ModifyDataOperation extends CodeAreaOperation {

    protected final long position;
    protected final BinaryData data;

    public ModifyDataOperation(CodeAreaCore codeArea, long position, BinaryData data) {
        super(codeArea);
        this.position = position;
        this.data = data;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.MODIFY_DATA;
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
        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        if (withUndo) {
            BinaryData undoData = contentData.copy(position, data.getDataSize());
            undoOperation = new ModifyDataOperation(codeArea, position, undoData);
        }
        contentData.replace(position, data);
        return undoOperation;
    }

    @Override
    public void dispose() {
        super.dispose();
        data.dispose();
    }
}
