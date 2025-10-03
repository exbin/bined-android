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
package org.exbin.bined.operation.android.command;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditModeCapable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Paste data command.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PasteDataCommand extends CodeAreaCommand {

    protected CodeAreaCommand modifyCommand = null;
    protected CodeAreaCommand insertCommand = null;
    protected BinaryData pastedData;

    public PasteDataCommand(CodeAreaCore codeArea, BinaryData pastedData) {
        super(codeArea);
        this.pastedData = pastedData.copy();
    }

    public boolean isEmpty() {
        return modifyCommand == null && insertCommand == null;
    }

    @Override
    public void performExecute() {
        long dataPosition = ((CaretCapable) codeArea).getDataPosition();
        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
        long dataSize = codeArea.getDataSize();

        BinaryData insertedData = null;
        long clipDataSize = pastedData.getDataSize();
        long insertionPosition = dataPosition;
        if (editMode == EditMode.INPLACE) {
            long toReplace = clipDataSize;
            if (dataPosition + toReplace > codeArea.getDataSize()) {
                toReplace = codeArea.getDataSize() - dataPosition;
            }
            if (toReplace > 0) {
                modifyCommand = new ModifyDataCommand(codeArea, dataPosition, pastedData.copy(0, toReplace));
            }
        } else {
            long replacedPartSize = 0;
            if (editOperation == EditOperation.OVERWRITE) {
                replacedPartSize = clipDataSize;
                if (insertionPosition + replacedPartSize > dataSize) {
                    replacedPartSize = dataSize - insertionPosition;
                }
                if (replacedPartSize > 0) {
                    modifyCommand = new ModifyDataCommand(codeArea, dataPosition, pastedData.copy(0, replacedPartSize));
                }
            }

            if (editMode == EditMode.EXPANDING && clipDataSize > replacedPartSize) {
                insertedData = pastedData.copy(replacedPartSize, clipDataSize - replacedPartSize);
                insertionPosition += replacedPartSize;
            }
        }

        pastedData.dispose();
        pastedData = null;

        if (insertedData != null && !insertedData.isEmpty()) {
            insertCommand = new InsertDataCommand(codeArea, insertionPosition, ((CaretCapable) codeArea).getCodeOffset(), insertedData);
        }

        if (modifyCommand != null) {
            modifyCommand.execute();
        }

        if (insertCommand != null) {
            insertCommand.execute();
        }
    }

    @Override
    public void performRedo() {
        if (modifyCommand != null) {
            modifyCommand.redo();
        }
        if (insertCommand != null) {
            insertCommand.redo();
        }
    }

    @Override
    public void performUndo() {
        if (insertCommand != null) {
            insertCommand.undo();
        }
        if (modifyCommand != null) {
            modifyCommand.undo();
        }
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.COMPOUND;
    }
}
