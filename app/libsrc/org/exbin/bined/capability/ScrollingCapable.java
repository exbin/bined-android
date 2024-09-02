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
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.ScrollingDirection;

/**
 * Support for scrolling capability.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ScrollingCapable {

    /**
     * Returns current scrolling position.
     *
     * @return scroll position
     */
    @Nonnull
    CodeAreaScrollPosition getScrollPosition();

    /**
     * Sets current scrolling position.
     *
     * @param scrollPosition scrolling position
     */
    void setScrollPosition(CodeAreaScrollPosition scrollPosition);

    /**
     * Adds scrolling listener.
     *
     * @param scrollingListener scrolling listener
     */
    void addScrollingListener(ScrollingListener scrollingListener);

    /**
     * Removes scrolling listener.
     *
     * @param scrollingListener scrolling listener
     */
    void removeScrollingListener(ScrollingListener scrollingListener);

    /**
     * Computes scrolling position for given direction.
     *
     * @param startPosition start position
     * @param direction scrolling direction
     * @return scrolling position
     */
    @Nonnull
    CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction);

    /**
     * Reveals scrolling area for current cursor position.
     */
    void revealCursor();

    /**
     * Reveals scrolling area for given caret position.
     *
     * @param caretPosition caret position
     */
    void revealPosition(CodeAreaCaretPosition caretPosition);

    /**
     * Scrolls scrolling area as centered as possible for current cursor
     * position.
     */
    void centerOnCursor();

    /**
     * Scrolls scrolling area as centered as possible for given caret position.
     *
     * @param caretPosition caret position
     */
    void centerOnPosition(CodeAreaCaretPosition caretPosition);
}
