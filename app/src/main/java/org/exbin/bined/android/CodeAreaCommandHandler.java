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
package org.exbin.bined.android;

import android.view.KeyEvent;

import org.exbin.bined.ScrollBarOrientation;
import org.exbin.bined.basic.SelectingMode;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for code area data manipulation.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CodeAreaCommandHandler {

    /**
     * Notifies command handler about end of sequence of append-able commands.
     */
    void sequenceBreak();

    /**
     * Keyboard key was pressed.
     *
     * @param keyEvent key event
     */
    void keyPressed(KeyEvent keyEvent);

    /**
     * Keyboard key was typed.
     *
     * @param keyCode key code
     * @param keyEvent key event
     */
    void keyTyped(int keyCode, KeyEvent keyEvent);

    /**
     * Enter key was pressed.
     */
    void enterPressed();

    /**
     * Tab key was pressed.
     */
    void tabPressed();

    /**
     * Backspace key was pressed.
     */
    void backSpacePressed();

    /**
     * Delete key was pressed.
     */
    void deletePressed();

    /**
     * Deletes selection.
     */
    void delete();

    /**
     * Copies selection to clipboard.
     */
    void copy();

    /**
     * Cuts selection to clipboard.
     */
    void cut();

    /**
     * Pastes content of clipboard to cursor area.
     */
    void paste();

    /**
     * Returns true if paste action is possible.
     *
     * @return true if paste is possible
     */
    boolean canPaste();

    /**
     * Expands selection to all data.
     */
    void selectAll();

    /**
     * Clears data selection.
     */
    void clearSelection();

    /**
     * Moves caret with mouse event.
     *
     * @param positionX relative position X
     * @param positionY relative position Y
     * @param selecting selection selecting
     */
    void moveCaret(int positionX, int positionY, SelectingMode selecting);

    /**
     * Performs scrolling.
     *
     * @param scrollSize number of scroll units (positive or negative)
     * @param orientation scrollbar orientation
     */
    void wheelScroll(int scrollSize, ScrollBarOrientation orientation);

    /**
     * Checks whether edit is allowed.
     *
     * @return true if allowed
     */
    boolean checkEditAllowed();

    @ParametersAreNonnullByDefault
    interface CodeAreaCommandHandlerFactory {

        @Nonnull
        CodeAreaCommandHandler createCommandHandler(CodeAreaCore codeArea);
    }
}
