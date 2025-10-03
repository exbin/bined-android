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

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.operation.command.BinaryDataCommand;
import org.exbin.bined.operation.command.BinaryDataCommandPhase;
import org.exbin.bined.operation.android.DeleteEditDataOperation;
import org.exbin.bined.operation.android.InsertCharEditDataOperation;
import org.exbin.bined.operation.android.OverwriteCharEditDataOperation;
import org.exbin.bined.operation.command.BinaryDataAppendableCommand;
import org.exbin.bined.operation.BinaryDataAppendableOperation;
import org.exbin.bined.operation.BinaryDataUndoableOperation;
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
    @Nonnull
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    @Nonnull
    protected BinaryDataUndoableOperation activeOperation;
    protected CodeAreaCaretPosition afterCaretPosition;

    public EditCharDataCommand(CodeAreaCore codeArea, EditOperationType editOperationType, long position, char charData) {
        super(codeArea);
        this.editOperationType = editOperationType;
        CodeAreaSection activeSection = ((CaretCapable) codeArea).getActiveSection();
        switch (editOperationType) {
            case INSERT: {
                InsertCharEditDataOperation operation = new InsertCharEditDataOperation(position, charData, ((CharsetCapable) codeArea).getCharset());
                afterCaretPosition = new DefaultCodeAreaCaretPosition(position, 0, activeSection);
                activeOperation = operation;
                break;
            }
            case OVERWRITE: {
                OverwriteCharEditDataOperation operation = new OverwriteCharEditDataOperation(position, charData, ((CharsetCapable) codeArea).getCharset());
                afterCaretPosition = new DefaultCodeAreaCaretPosition(position, 0, activeSection);
                activeOperation = operation;
                break;
            }
            case DELETE: {
                DeleteEditDataOperation operation = new DeleteEditDataOperation(position, charData);
                if (operation.isBackSpace()) {
                    afterCaretPosition = new DefaultCodeAreaCaretPosition(position - 1, 0, activeSection);
                } else {
                    afterCaretPosition = new DefaultCodeAreaCaretPosition(position, 0, activeSection);
                }
                activeOperation = operation;
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(editOperationType);
        }
    }

    @Override
    public void performExecute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo(contentData);
        switch (editOperationType) {
            case INSERT: {
                afterCaretPosition = new DefaultCodeAreaCaretPosition(afterCaretPosition.getDataPosition() + ((InsertCharEditDataOperation) activeOperation).getCharLength(), afterCaretPosition.getCodeOffset(), afterCaretPosition.getSection().orElse(null));
                break;
            }
            case OVERWRITE: {
                afterCaretPosition = new DefaultCodeAreaCaretPosition(afterCaretPosition.getDataPosition() + ((OverwriteCharEditDataOperation) activeOperation).getCharLength(), afterCaretPosition.getCodeOffset(), afterCaretPosition.getSection().orElse(null));
                break;
            }
        }
        ((CaretCapable) codeArea).setActiveCaretPosition(afterCaretPosition);

        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void performUndo() {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo(contentData);
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void performRedo() {
        if (phase != BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo(contentData);
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
            boolean appended = ((BinaryDataAppendableOperation) activeOperation).appendOperation(((EditCharDataCommand) command).activeOperation);
            if (appended) {
                afterState = ((EditCharDataCommand) command).getAfterState().orElse(afterState);
            }
            return appended;
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
