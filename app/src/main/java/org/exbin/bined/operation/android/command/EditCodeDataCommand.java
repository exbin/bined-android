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
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataCommandPhase;
import org.exbin.bined.operation.android.DeleteCodeEditDataOperation;
import org.exbin.bined.operation.android.InsertCodeEditDataOperation;
import org.exbin.bined.operation.android.OverwriteCodeEditDataOperation;
import org.exbin.bined.operation.undo.BinaryDataAppendableCommand;
import org.exbin.bined.operation.undo.BinaryDataAppendableOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Command for editing data in code section.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditCodeDataCommand extends EditDataCommand implements BinaryDataAppendableCommand {

    private final EditCommandType commandType;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    private BinaryDataUndoableOperation activeOperation;

    public EditCodeDataCommand(CodeAreaCore codeArea, EditCommandType commandType, long position, int positionCodeOffset, byte value) {
        super(codeArea);
        this.commandType = commandType;
        switch (commandType) {
            case INSERT: {
                activeOperation = new InsertCodeEditDataOperation(codeArea, position, positionCodeOffset, value);
                break;
            }
            case OVERWRITE: {
                activeOperation = new OverwriteCodeEditDataOperation(codeArea, position, positionCodeOffset, ((CodeTypeCapable) codeArea).getCodeType(), value);
                break;
            }
            case DELETE: {
                activeOperation = new DeleteCodeEditDataOperation(codeArea, position, ((CodeTypeCapable) codeArea).getCodeType(), value);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(commandType);
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
        command.execute();

        if (command instanceof EditCodeDataCommand && activeOperation instanceof BinaryDataAppendableOperation) {
            return ((BinaryDataAppendableOperation) activeOperation).appendOperation(((EditCodeDataCommand) command).activeOperation);
        }

        return false;
    }

    @Nonnull
    @Override
    public EditCommandType getCommandType() {
        return commandType;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (activeOperation != null) {
            activeOperation.dispose();
        }
    }
}
