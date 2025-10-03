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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.operation.command.BinaryDataAppendableCommand;
import org.exbin.bined.operation.command.BinaryDataCommand;
import org.exbin.bined.operation.command.BinaryDataCommandPhase;
import org.exbin.bined.operation.command.BinaryDataCompoundCommand;
import org.exbin.bined.operation.command.BinaryDataUndoableCommand;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Class for compound command on binary document.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaCompoundCommand extends CodeAreaCommand implements BinaryDataCompoundCommand, BinaryDataAppendableCommand {

    private final List<BinaryDataCommand> commands = new ArrayList<>();
    @Nonnull
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;

    public CodeAreaCompoundCommand(CodeAreaCore codeArea) {
        super(codeArea);
    }

    @Nullable
    public static CodeAreaCommand buildCompoundCommand(CodeAreaCore codeArea, @Nullable CodeAreaCommand... commands) {
        CodeAreaCommand resultCommand = null;
        for (CodeAreaCommand command : commands) {
            if (command != null) {
                if (resultCommand == null) {
                    resultCommand = command;
                } else if (resultCommand instanceof CodeAreaCompoundCommand) {
                    ((CodeAreaCompoundCommand) resultCommand).addCommand(command);
                } else {
                    CodeAreaCompoundCommand compoundCommand = new CodeAreaCompoundCommand(codeArea);
                    compoundCommand.addCommand(resultCommand);
                    compoundCommand.addCommand(command);
                    resultCommand = compoundCommand;
                }
            }
        }

        return resultCommand;
    }

    @Nonnull
    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.COMPOUND;
    }

    @Override
    public void performExecute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        for (BinaryDataCommand command : commands) {
            command.execute();
        }

        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void performRedo() {
        if (phase != BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        for (BinaryDataCommand command : commands) {
            if (command instanceof BinaryDataUndoableCommand) {
                ((BinaryDataUndoableCommand) command).redo();
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void performUndo() {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        for (int i = commands.size() - 1; i >= 0; i--) {
            BinaryDataCommand command = commands.get(i);
            if (command instanceof BinaryDataUndoableCommand) {
                ((BinaryDataUndoableCommand) command).undo();
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public boolean appendExecute(BinaryDataCommand command) {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        if (!isEmpty()) {
            BinaryDataCommand lastCommand = commands.get(commands.size() - 1);
            if (lastCommand instanceof BinaryDataAppendableCommand) {
                boolean appended = ((BinaryDataAppendableCommand) lastCommand).appendExecute(command);
                if (appended && command instanceof CodeAreaCommand) {
                    afterState = ((CodeAreaCommand) command).getAfterState().orElse(afterState);
                }
                return appended;
            }
        }

        command.execute();

        return false;
    }

    @Override
    public void addCommand(BinaryDataCommand command) {
        commands.add(command);
    }

    @Override
    public void addCommands(Collection<BinaryDataCommand> commands) {
        this.commands.addAll(commands);
    }

    @Nonnull
    @Override
    public List<BinaryDataCommand> getCommands() {
        return commands;
    }

    @Override
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (BinaryDataCommand command : commands) {
            command.dispose();
        }
    }
}
