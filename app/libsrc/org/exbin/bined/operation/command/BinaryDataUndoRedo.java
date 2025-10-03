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
package org.exbin.bined.operation.command;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataModifiedState;
import org.exbin.bined.operation.BinaryDataUndoRedoChangeListener;
import org.exbin.bined.operation.BinaryDataUndoRedoState;

/**
 * Undoable command sequence.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryDataUndoRedo extends BinaryDataUndoRedoState, BinaryDataModifiedState {

    /**
     * Executes given command and adds it at the end of the sequence replacing
     * scheduled commands if present.
     *
     * @param command command to execute
     */
    void execute(BinaryDataCommand command);

    /**
     * Returns top undo command.
     *
     * @return top undo command if exists
     */
    @Nonnull
    Optional<BinaryDataCommand> getTopUndoCommand();

    /**
     * Returns list of commands.
     *
     * @return list of commands
     */
    @Nonnull
    List<BinaryDataCommand> getCommandList();

    /**
     * Returns position in sequence between already executed and scheduled
     * commands.
     *
     * @return position in sequence.
     */
    int getCommandPosition();

    /**
     * Returns commands count.
     *
     * @return commands count.
     */
    int getCommandsCount();

    /**
     * Resets / clears all commands in sequence.
     */
    void clear();

    /**
     * Performs redo.
     */
    void performRedo();

    /**
     * Performs specific number of redo commands.
     *
     * @param count count
     */
    void performRedo(int count);

    /**
     * Performs undo.
     */
    void performUndo();

    /**
     * Performs specific number of undo commands.
     *
     * @param count count
     */
    void performUndo(int count);

    /**
     * Performs executions or reverts to reach synchronization position.
     */
    void performSync();

    /**
     * Returns synchronization mark position.
     *
     * @return command position
     */
    int getSyncPosition();

    /**
     * Sets synchronization mark position.
     *
     * @param commandPosition command position
     */
    void setSyncPosition(int commandPosition);

    /**
     * Sets synchronization mark position to current command position.
     */
    void setSyncPosition();

    /**
     * Registers change listener.
     *
     * @param listener listener
     */
    void addChangeListener(BinaryDataUndoRedoChangeListener listener);

    /**
     * Unregisters change listener.
     *
     * @param listener listener
     */
    void removeChangeListener(BinaryDataUndoRedoChangeListener listener);

}
