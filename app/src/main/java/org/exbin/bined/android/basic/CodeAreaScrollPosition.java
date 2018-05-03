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
package org.exbin.bined.android.basic;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Code Area scrolling position.
 *
 * @version 0.2.0 2018/03/24
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaScrollPosition {

    /**
     * Scrollbar document row position.
     */
    private long scrollRowPosition = 0;

    /**
     * Scrollbar document row pixel offset position.
     */
    private int scrollRowOffset = 0;

    /**
     * Scrollbar document character position.
     */
    private int scrollCharPosition = 0;

    /**
     * Scrollbar document character pixel offset position.
     */
    private int scrollCharOffset = 0;

    public long getScrollRowPosition() {
        return scrollRowPosition;
    }

    public int getScrollRowOffset() {
        return scrollRowOffset;
    }

    public int getScrollCharPosition() {
        return scrollCharPosition;
    }

    public int getScrollCharOffset() {
        return scrollCharOffset;
    }

    public void setScrollRowPosition(long scrollRowPosition) {
        this.scrollRowPosition = scrollRowPosition;
    }

    public void setScrollRowOffset(int scrollRowOffset) {
        this.scrollRowOffset = scrollRowOffset;
    }

    public void setScrollCharPosition(int scrollCharPosition) {
        this.scrollCharPosition = scrollCharPosition;
    }

    public void setScrollCharOffset(int scrollCharOffset) {
        this.scrollCharOffset = scrollCharOffset;
    }

    public void setScrollPosition(@Nullable CodeAreaScrollPosition scrollPosition) {
        if (scrollPosition == null) {
            reset();
        } else {
            scrollRowPosition = scrollPosition.getScrollRowPosition();
            scrollRowOffset = scrollPosition.getScrollRowOffset();
            scrollCharPosition = scrollPosition.getScrollCharPosition();
            scrollCharOffset = scrollPosition.getScrollCharOffset();
        }
    }

    /**
     * Resets scrolling position to top left corner.
     */
    public void reset() {
        scrollRowPosition = 0;
        scrollRowOffset = 0;
        scrollCharPosition = 0;
        scrollCharOffset = 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scrollRowPosition, scrollRowOffset, scrollCharPosition, scrollCharOffset);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CodeAreaScrollPosition other = (CodeAreaScrollPosition) obj;
        if (this.scrollRowPosition != other.scrollRowPosition) {
            return false;
        }
        if (this.scrollRowOffset != other.scrollRowOffset) {
            return false;
        }
        if (this.scrollCharPosition != other.scrollCharPosition) {
            return false;
        }
        return this.scrollCharOffset != other.scrollCharOffset;
    }
}
