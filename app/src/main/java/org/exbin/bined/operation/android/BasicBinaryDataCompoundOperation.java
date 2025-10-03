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

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.operation.BinaryDataCompoundOperation;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.BinaryDataUndoableOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Compound binary data operation.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicBinaryDataCompoundOperation implements BinaryDataCompoundOperation, BinaryDataUndoableOperation {

    protected final List<BinaryDataOperation> operations = new ArrayList<>();

    public BasicBinaryDataCompoundOperation() {
    }

    @Nonnull
    @Override
    public BinaryDataUndoableOperation executeWithUndo(EditableBinaryData contentData) {
        BasicBinaryDataCompoundOperation undoOperations = new BasicBinaryDataCompoundOperation();
        for (BinaryDataOperation operation : operations) {
            BinaryDataUndoableOperation undoOperation = ((BinaryDataUndoableOperation) operation).executeWithUndo(contentData);
            undoOperations.insertOperation(0, undoOperation);
        }
        return undoOperations;
    }

    @Override
    public void execute(EditableBinaryData contentData) {
        for (BinaryDataOperation operation : operations) {
            operation.execute(contentData);
        }
    }

    @Nonnull
    @Override
    public BasicBinaryDataOperationType getType() {
        return BasicBinaryDataOperationType.COMPOUND;
    }

    @Override
    public void addOperation(BinaryDataOperation operation) {
        operations.add(operation);
    }

    @Override
    public void addOperations(Collection<BinaryDataOperation> operations) {
        this.operations.addAll(operations);
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

    @Override
    public void dispose() {
    }
}
