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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.BinaryDataCommandPhase;
import org.exbin.bined.operation.android.CodeAreaOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Abstract class for operation on hexadecimal document.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class OpCodeAreaCommand extends CodeAreaCommand {

    @Nullable
    protected BinaryDataUndoableOperation operation;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;

    public OpCodeAreaCommand(CodeAreaCore codeArea) {
        super(codeArea);
    }

    public void setOperation(CodeAreaOperation operation) {
        if (this.operation != null) {
            this.operation.dispose();
        }
        this.operation = operation;
    }

    @Override
    public void execute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        executeInt();
    }

    @Override
    public void undo() {
        if (phase == BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation redoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo();
        operation.dispose();
        operation = redoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void redo() {
        if (phase == BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        executeInt();
    }

    private void executeInt() {
        BinaryDataUndoableOperation undoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo();
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
