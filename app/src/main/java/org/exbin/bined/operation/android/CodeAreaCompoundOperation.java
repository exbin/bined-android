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
package org.exbin.bined.operation.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.operation.BinaryDataCompoundOperation;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.undo.BinaryDataUndoableOperation;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Abstract class for compound operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaCompoundOperation extends CodeAreaOperation implements BinaryDataCompoundOperation {

    private final List<BinaryDataOperation> operations = new ArrayList<>();

    public CodeAreaCompoundOperation(CodeAreaCore codeArea) {
        super(codeArea);
    }

    public CodeAreaCompoundOperation(CodeAreaCore codeArea, @Nullable CodeAreaCaretPosition backPosition) {
        super(codeArea, backPosition);
    }

    @Override
    public BinaryDataUndoableOperation executeWithUndo() {
        CodeAreaCompoundOperation undoOperations = new CodeAreaCompoundOperation(codeArea);
        for (BinaryDataOperation operation : operations) {
            BinaryDataUndoableOperation undoOperation = ((BinaryDataUndoableOperation) operation).executeWithUndo();
            undoOperations.insertOperation(0, undoOperation);
        }
        return undoOperations;
    }

    @Override
    public void execute() {
        for (BinaryDataOperation operation : operations) {
            operation.execute();
        }
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.COMPOUND;
    }

    @Override
    public void addOperation(BinaryDataOperation operation) {
        operations.add(operation);
    }

    @Override
    public void addOperations(Collection<BinaryDataOperation> operations) {
        operations.addAll(operations);
    }

    public void insertOperation(int index, BinaryDataOperation operation) {
        operations.add(index, operation);
    }

    @Nonnull
    @Override
    public List<BinaryDataOperation> getOperations() {
        return operations;
    }

    @Override
    public boolean isEmpty() {
        return operations.isEmpty();
    }

}
