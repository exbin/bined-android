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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataUndoRedoChangeListener;

/**
 * Empty binary data undo.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EmptyBinaryDataUndoRedo implements BinaryDataUndoRedo {

    @Override
    public boolean canRedo() {
        return false;
    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public void execute(BinaryDataCommand command) {
        command.execute();
    }

    @Nonnull
    @Override
    public List<BinaryDataCommand> getCommandList() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Optional<BinaryDataCommand> getTopUndoCommand() {
        return Optional.empty();
    }

    @Override
    public int getCommandPosition() {
        return 0;
    }

    @Override
    public int getCommandsCount() {
        return 0;
    }

    @Override
    public int getSyncPosition() {
        return 0;
    }

    @Override
    public void performUndo() {
        throw new IllegalStateException();
    }

    @Override
    public void performUndo(int count) {
        throw new IllegalStateException();
    }

    @Override
    public void performRedo() {
        throw new IllegalStateException();
    }

    @Override
    public void performRedo(int count) {
        throw new IllegalStateException();
    }

    @Override
    public void performSync() {
    }

    @Override
    public void setSyncPosition(int syncPosition) {
        throw new IllegalStateException();
    }

    @Override
    public void setSyncPosition() {
    }

    @Override
    public void addChangeListener(BinaryDataUndoRedoChangeListener listener) {
    }

    @Override
    public void removeChangeListener(BinaryDataUndoRedoChangeListener listener) {
    }
}
