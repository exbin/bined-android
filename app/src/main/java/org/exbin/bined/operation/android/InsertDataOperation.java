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
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for inserting data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataOperation extends CodeAreaOperation {

    protected long position;
    protected int codeOffset;
    protected final BinaryData data;

    public InsertDataOperation(CodeAreaCore codeArea, long position, int codeOffset, BinaryData data) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.data = data;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.INSERT_DATA;
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
        contentData.insert(position, data);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(codeArea, position, codeOffset, data.getDataSize());
        }
        ((CaretCapable) codeArea).setActiveCaretPosition(position + data.getDataSize(), codeOffset);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
    }

    @Nonnull
    public BinaryData getData() {
        return data;
    }

    @Override
    public void dispose() {
        super.dispose();
        data.dispose();
    }
}
