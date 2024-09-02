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
package org.exbin.bined.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.EditModeChangedListener;

/**
 * Support for edit mode capability.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface EditModeCapable {

    /**
     * Returns edit mode.
     *
     * @return edit mode
     */
    @Nonnull
    EditMode getEditMode();

    /**
     * Sets edit mode.
     *
     * @param editMode edit mode
     */
    void setEditMode(EditMode editMode);

    /**
     * Returns currently active operation as set or enforced by current edit
     * mode.
     *
     * @return active edit operation
     */
    @Nonnull
    EditOperation getActiveOperation();

    /**
     * Returns currently enforced edit operation.
     *
     * @return edit operation
     */
    @Nonnull
    EditOperation getEditOperation();

    /**
     * Sets currently enforced edit operation.
     *
     * @param editOperation edit operation
     */
    void setEditOperation(EditOperation editOperation);

    /**
     * Adds edit mode change listener.
     *
     * @param editModeChangedListener edit mode change listener
     */
    void addEditModeChangedListener(EditModeChangedListener editModeChangedListener);

    /**
     * Removes edit mode change listener.
     *
     * @param editModeChangedListener edit mode change listener
     */
    void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener);
}
