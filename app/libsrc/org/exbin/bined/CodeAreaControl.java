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
package org.exbin.bined;

/**
 * Code Area control interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface CodeAreaControl extends DataProvider {

    /**
     * Copies selection to clipboard.
     */
    void copy();

    /**
     * Cuts selection to clipboard.
     */
    void cut();

    /**
     * Pastes content of the clipboard.
     */
    void paste();

    /**
     * Deletes selected section.
     */
    void delete();

    /**
     * Expands selection to all data.
     */
    void selectAll();

    /**
     * Returns true if content of the clipboard is valid for paste operation.
     *
     * @return true if paste can proceed
     */
    boolean canPaste();

    /**
     * Returns true if selection is not empty.
     *
     * @return true if selection is not empty
     */
    boolean hasSelection();

    /**
     * Clears data selection.
     */
    void clearSelection();
}
