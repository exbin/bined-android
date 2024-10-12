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

import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Command for deleting data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeleteSelectionCommand extends CodeAreaCommand {

    private final RemoveDataCommand removeCommand;
    private final long position;
    private final long size;

    public DeleteSelectionCommand(CodeAreaCore coreArea) {
        super(coreArea);
        SelectionRange selection = ((SelectionCapable) coreArea).getSelection();
        position = selection.getFirst();
        size = selection.getLast() - position + 1;
        removeCommand = new RemoveDataCommand(coreArea, position, 0, size);
    }

    @Override
    public void execute() {
        removeCommand.execute();
        ((CaretCapable) codeArea).setActiveCaretPosition(position);
        clearSelection();
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.notifyDataChanged();
    }

    @Override
    public void redo() {
        removeCommand.redo();
        ((CaretCapable) codeArea).setActiveCaretPosition(position);
        clearSelection();
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.notifyDataChanged();
    }

    @Override
    public void undo() {
        removeCommand.undo();
        clearSelection();
        ((CaretCapable) codeArea).setActiveCaretPosition(position + size);
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.notifyDataChanged();
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_REMOVED;
    }

    private void clearSelection() {
        long dataPosition = ((CaretCapable) codeArea).getDataPosition();
        ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
    }
}