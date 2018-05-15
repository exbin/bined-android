/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.android;

import android.graphics.Canvas;

import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.android.basic.CodeAreaScrollPosition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Hexadecimal editor painter interface.
 *
 * @version 0.2.0 2018/05/03
 * @author ExBin Project (http://exbin.org)
 */
public interface CodeAreaPainter {

    /**
     * Returns true if painter was initialized.
     *
     * @return true if initialized
     */
    boolean isInitialized();

    /**
     * Paints the main component.
     *
     * @param g Canvas
     */
    void paintComponent(@Nonnull Canvas g);

    /**
     * Paints main hexadecimal data section of the component.
     *
     * @param g Canvas
     */
    void paintMainArea(@Nonnull Canvas g);

    /**
     * Paints cursor symbol.
     *
     * @param g Canvas
     */
    void paintCursor(@Nonnull Canvas g);

    /**
     * Resets complete painter state for new painting.
     */
    void reset();

    /**
     * Resets painter font state for new painting.
     */
    void resetFont();

    /**
     * Resets painter layout state for new painting.
     */
    void resetLayout();

    /**
     * Returns type of cursor for given painter relative position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @return java.awt.Cursor cursor type value
     */
    int getMouseCursorShape(int positionX, int positionY);

    /**
     * Returns zone type for given position.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return specific zone in component
     */
    @Nonnull
    BasicCodeAreaZone getPositionZone(int x, int y);

    /**
     * Returns closest caret position for provided component relative mouse
     * position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @return closest caret position
     */
    @Nonnull
    CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY);

    void updateScrollBars();

    /**
     * Returns scroll position so that provided caret position is visible in
     * scrolled area.
     *
     * Performs minimal scrolling and tries to preserve current vertical /
     * horizontal scrolling if possible. If given position cannot be fully
     * shown, top left corner is preferred.
     *
     * @param caretPosition caret position
     * @return scroll position or null if caret position is already visible /
     * scrolled to the best fit
     */
    @Nullable
    CodeAreaScrollPosition computeRevealScrollPosition(@Nonnull CaretPosition caretPosition);

    /**
     * Returns scroll position so that provided caret position is visible in the
     * center of the scrolled area.
     *
     * Attempts to center as much as possible while preserving scrolling limits.
     *
     * @param caretPosition caret position
     * @return scroll position or null if desired scroll position is the same as
     * current scroll position.
     */
    @Nullable
    CodeAreaScrollPosition computeCenterOnScrollPosition(@Nonnull CaretPosition caretPosition);

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    @Nonnull
    CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction);

    /**
     * Computes scrolling position for given shift action.
     *
     * @param startPosition start position
     * @param direction scrolling direction
     * @return target position
     */
    @Nonnull
    CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection direction);
}
