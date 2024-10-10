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
package org.exbin.bined.operation;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for compound operation.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryDataCompoundOperation extends BinaryDataOperation {

    /**
     * Adds operation to the list of operations.
     *
     * @param operation appended operation
     */
    void addOperation(BinaryDataOperation operation);

    /**
     * Adds list of operations to the list of operations.
     *
     * @param operations appended operations
     */
    void addOperations(Collection<BinaryDataOperation> operations);

    /**
     * Returns list of operations.
     *
     * @return list of operations
     */
    @Nonnull
    List<BinaryDataOperation> getOperations();

    /**
     * Returns true if compound operation is empty.
     *
     * @return true if operation is empty
     */
    boolean isEmpty();
}
