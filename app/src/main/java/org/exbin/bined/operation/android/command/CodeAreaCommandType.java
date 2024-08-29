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
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataCommandType;

/**
 * Operation type enumeration.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum CodeAreaCommandType implements BinaryDataCommandType {

    /**
     * Insert data command.
     */
    DATA_INSERTED("Data inserted"),
    /**
     * Remove data command.
     */
    DATA_REMOVED("Data removed"),
    /**
     * Modify data command.
     */
    DATA_MODIFIED("Data modified"),
    /**
     * Move data command.
     */
    DATA_MOVED("Data moved"),
    /**
     * Compound command.
     */
    COMPOUND("Compound"),
    /**
     * Edit data command.
     */
    DATA_EDITED("Data edited");

    private final String name;

    CodeAreaCommandType(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
