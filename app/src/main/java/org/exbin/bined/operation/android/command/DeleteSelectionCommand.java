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
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.android.RemoveDataOperation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Delete selection command.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeleteSelectionCommand extends OpCodeAreaCommand {

    protected long position;

    public DeleteSelectionCommand(CodeAreaCore codeArea) {
        super(codeArea);
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        position = selection.getFirst();
        long size = selection.getLast() - position + 1;
        super.setOperation(new RemoveDataOperation(position, 0, size));
    }

    @Override
    public void performExecute() {
        super.performExecute();
        ((CaretCapable) codeArea).setActiveCaretPosition(position);
        ((SelectionCapable) codeArea).setSelection(position, position);
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_REMOVED;
    }
}
