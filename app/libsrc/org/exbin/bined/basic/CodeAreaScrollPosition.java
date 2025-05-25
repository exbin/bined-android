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
package org.exbin.bined.basic;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Code area scrolling position.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaScrollPosition {

    /**
     * Scrollbar document row position.
     */
    protected long rowPosition = 0;

    /**
     * Scrollbar document row pixel offset position.
     */
    protected int rowOffset = 0;

    /**
     * Scrollbar document character position.
     */
    protected int charPosition = 0;

    /**
     * Scrollbar document character pixel offset position.
     */
    protected int charOffset = 0;

    public CodeAreaScrollPosition() {
    }

    public CodeAreaScrollPosition(long rowPosition, int rowOffset, int charPosition, int charOffset) {
        this.rowPosition = rowPosition;
        this.rowOffset = rowOffset;
        this.charPosition = charPosition;
        this.charOffset = charOffset;
    }

    public long getRowPosition() {
        return rowPosition;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getCharPosition() {
        return charPosition;
    }

    public int getCharOffset() {
        return charOffset;
    }

    public void setRowPosition(long rowPosition) {
        this.rowPosition = rowPosition;
    }

    public void setRowOffset(int rowOffset) {
        this.rowOffset = rowOffset;
    }

    public void setCharPosition(int charPosition) {
        this.charPosition = charPosition;
    }

    public void setCharOffset(int charOffset) {
        this.charOffset = charOffset;
    }

    public void setScrollPosition(@Nullable CodeAreaScrollPosition scrollPosition) {
        if (scrollPosition == null) {
            reset();
        } else {
            rowPosition = scrollPosition.getRowPosition();
            rowOffset = scrollPosition.getRowOffset();
            charPosition = scrollPosition.getCharPosition();
            charOffset = scrollPosition.getCharOffset();
        }
    }

    /**
     * Resets scrolling position to top left corner.
     */
    public void reset() {
        rowPosition = 0;
        rowOffset = 0;
        charPosition = 0;
        charOffset = 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowPosition, rowOffset, charPosition, charOffset);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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
        if (this.rowPosition != other.rowPosition) {
            return false;
        }
        if (this.rowOffset != other.rowOffset) {
            return false;
        }
        if (this.charPosition != other.charPosition) {
            return false;
        }
        return this.charOffset != other.charOffset;
    }

    public boolean isCharPositionGreaterThan(CodeAreaScrollPosition compPosition) {
        return charPosition > compPosition.charPosition || (charPosition == compPosition.charPosition && charOffset > compPosition.charOffset);
    }

    public boolean isRowPositionGreaterThan(CodeAreaScrollPosition compPosition) {
        return rowPosition > compPosition.rowPosition || (rowPosition == compPosition.rowPosition && rowOffset > compPosition.rowOffset);
    }
}
