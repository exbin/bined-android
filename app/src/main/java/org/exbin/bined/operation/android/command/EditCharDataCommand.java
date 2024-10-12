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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataCommandPhase;
import org.exbin.bined.operation.android.DeleteCharEditDataOperation;
import org.exbin.bined.operation.android.InsertCharEditDataOperation;
import org.exbin.bined.operation.android.OverwriteCharEditDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableCommand;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Command for editing data in text mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditCharDataCommand extends EditDataCommand implements BinaryDataAppendableCommand {

    @Nonnull
    protected final EditOperationType editOperationType;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    protected BinaryDataUndoableOperation activeOperation;

    public EditCharDataCommand(CodeAreaCore codeArea, EditOperationType editOperationType, long position, char charData) {
        super(codeArea);
        this.editOperationType = editOperationType;
        switch (editOperationType) {
            case INSERT: {
                activeOperation = new InsertCharEditDataOperation(codeArea, position, charData);
                break;
            }
            case OVERWRITE: {
                activeOperation = new OverwriteCharEditDataOperation(codeArea, position, charData);
                break;
            }
            case DELETE: {
                activeOperation = new DeleteCharEditDataOperation(codeArea, position, charData);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(editOperationType);
        }
    }

    @Override
    public void execute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo();
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void undo() {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo();
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void redo() {
        if (phase != BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo();
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean appendExecute(BinaryDataCommand command) {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        command.execute();

        if (command instanceof EditCharDataCommand && activeOperation instanceof BinaryDataAppendableOperation) {
            return ((BinaryDataAppendableOperation) activeOperation).appendOperation(((EditCharDataCommand) command).activeOperation);
        }

        return false;
    }

    @Nonnull
    @Override
    public EditOperationType getEditOperationType() {
        return editOperationType;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (activeOperation != null) {
            activeOperation.dispose();
        }
    }
}
