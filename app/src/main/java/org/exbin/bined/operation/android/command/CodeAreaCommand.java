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

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.android.CodeAreaState;
import org.exbin.bined.operation.command.BinaryDataAbstractCommand;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Abstract class for command on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class CodeAreaCommand extends BinaryDataAbstractCommand {

    @Nonnull
    protected final CodeAreaCore codeArea;
    protected CodeAreaState beforeState;
    protected CodeAreaState afterState;

    public CodeAreaCommand(CodeAreaCore codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void redo() {
        performRedo();
        restoreState(afterState);
    }

    @Override
    public void execute() {
        beforeState = fetchState();
        performExecute();
        afterState = fetchState();
    }

    @Override
    public void undo() {
        performUndo();
        restoreState(beforeState);
    }

    /**
     * Executes main command.
     */
    public void performRedo() {
        performExecute();
    }

    /**
     * Executes main command.
     */
    public abstract void performExecute();

    /**
     * Executes main undo command.
     */
    public abstract void performUndo();

    @Nonnull
    public Optional<CodeAreaState> getBeforeState() {
        return Optional.ofNullable(beforeState);
    }

    @Nonnull
    public Optional<CodeAreaState> getAfterState() {
        return Optional.ofNullable(afterState);
    }

    @Nonnull
    public CodeAreaState fetchState() {
        DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();
        caretPosition.setPosition(((CaretCapable) codeArea).getActiveCaretPosition());
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        return new CodeAreaState(caretPosition, selection);
    }

    public void restoreState(CodeAreaState codeAreaState) {
        CodeAreaCaretPosition caretPosition = codeAreaState.getCaretPosition();
        ((CaretCapable) codeArea).getCodeAreaCaret().setCaretPosition(caretPosition);
        SelectionRange selection = codeAreaState.getSelection();
        if (selection.isEmpty()) {
            ((SelectionCapable) codeArea).setSelection(caretPosition.getDataPosition(), caretPosition.getDataPosition());
        } else {
            ((SelectionCapable) codeArea).setSelection(selection);
        }
    }
}
