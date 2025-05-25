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
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.CodeAreaCaretListener;

/**
 * Support for caret / cursor capability.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CaretCapable {

    /**
     * Returns current caret position.
     *
     * @return caret position
     */
    @Nonnull
    CodeAreaCaretPosition getActiveCaretPosition();

    /**
     * Returns currently active caret section.
     *
     * @return code area section
     */
    @Nonnull
    CodeAreaSection getActiveSection();

    /**
     * Returns current caret data position.
     *
     * @return data position
     */
    long getDataPosition();

    /**
     * Returns current caret code offset.
     *
     * @return code offset
     */
    int getCodeOffset();

    /**
     * Sets current caret position to given position.
     *
     * @param caretPosition caret position
     */
    void setActiveCaretPosition(CodeAreaCaretPosition caretPosition);

    /**
     * Sets current caret position to given data position.
     *
     * @param dataPosition data position
     */
    void setActiveCaretPosition(long dataPosition);

    /**
     * Sets current caret position to given data position and offset.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     */
    void setActiveCaretPosition(long dataPosition, int codeOffset);

    /**
     * Returns handler for caret.
     *
     * @return caret handler
     */
    @Nonnull
    CodeAreaCaret getCodeAreaCaret();

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    @Nonnull
    CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction);

    /**
     * Computes closest caret position for given relative component position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @param overlapMode caret overlapping mode
     * @return mouse position
     */
    @Nonnull
    CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overlapMode);

    /**
     * Returns if cursor should be visible in other sections.
     *
     * @return true if cursor should be mirrored
     */
    boolean isShowMirrorCursor();

    /**
     * Sets if cursor should be visible in other sections.
     *
     * @param showMirrorCursor true if cursor should be mirrored
     */
    void setShowMirrorCursor(boolean showMirrorCursor);

    /**
     * Returns cursor shape type for given position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @return cursor type from java.awt.Cursor
     */
    int getMouseCursorShape(int positionX, int positionY);

    /**
     * Adds caret movement listener.
     *
     * @param caretMovedListener listener
     */
    void addCaretMovedListener(CodeAreaCaretListener caretMovedListener);

    /**
     * Removes caret movement listener.
     *
     * @param caretMovedListener listener
     */
    void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener);
}
