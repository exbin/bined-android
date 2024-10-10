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
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for deleting child block.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class RemoveDataOperation extends CodeAreaOperation {

    protected final long position;
    protected final int codeOffset;
    protected final long length;

    public RemoveDataOperation(CodeAreaCore codeArea, long position, int codeOffset, long length) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.length = length;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.REMOVE_DATA;
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
