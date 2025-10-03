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
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.command.BinaryDataCommandPhase;
import org.exbin.bined.operation.BinaryDataUndoableOperation;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Abstract class for operation on binary document.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class OpCodeAreaCommand extends CodeAreaCommand {

    @Nullable
    protected BinaryDataUndoableOperation operation;
    @Nonnull
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;

    public OpCodeAreaCommand(CodeAreaCore codeArea) {
        super(codeArea);
    }

    public void setOperation(BinaryDataUndoableOperation operation) {
        if (this.operation != null) {
            this.operation.dispose();
        }
        this.operation = operation;
    }

    @Override
    public void performExecute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        doCommand();
    }

    @Override
    public void performUndo() {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation redoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo(contentData);
        operation.dispose();
        operation = redoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void performRedo() {
        if (phase != BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        doCommand();
    }

    private void doCommand() {
        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        BinaryDataUndoableOperation undoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo(contentData);
        operation.dispose();
        operation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (operation != null) {
            operation.dispose();
        }
    }
}
