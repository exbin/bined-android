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

import org.exbin.bined.operation.command.BinaryDataCommandType;

/**
 * Code area command type enumeration.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum CodeAreaCommandType implements BinaryDataCommandType {

    /**
     * Insert data command.
     */
    DATA_INSERTED,
    /**
     * Remove data command.
     */
    DATA_REMOVED,
    /**
     * Modify data command.
     */
    DATA_MODIFIED,
    /**
     * Move data command.
     */
    DATA_MOVED,
    /**
     * Compound command.
     */
    COMPOUND,
    /**
     * Edit data command.
     */
    DATA_EDITED;
}
