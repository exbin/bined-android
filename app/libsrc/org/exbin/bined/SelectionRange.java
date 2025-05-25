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

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Selection range is selection between two positions where begin represents
 * origin point and end of the selection can be before or after begin.
 *
 * @author ExBin Project (https://exbin.org)
 */
@Immutable
public class SelectionRange {

    private final long start;
    private final long end;

    /**
     * Creates empty selection range.
     */
    public SelectionRange() {
        this(0, 0);
    }

    public SelectionRange(@Nullable SelectionRange selectionRange) {
        if (selectionRange == null) {
            start = end = 0;
        } else {
            start = selectionRange.start;
            end = selectionRange.end;
        }
    }

    /**
     * Creates selection range from start to end including the start and not
     * including character at the end position.
     *
     * @param start selection start position
     * @param end selection end position without character at the end position
     */
    public SelectionRange(long start, long end) {
        if (start < 0) {
            throw new IllegalArgumentException("Selection with negative range start (" + start + ") is not allowed");
        }

        if (end < 0) {
            throw new IllegalArgumentException("Selection with negative range end (" + end + ") is not allowed");
        }

        this.start = start;
        this.end = end;
    }

    /**
     * Returns selection start position.
     *
     * @return selection start position.
     */
    public long getStart() {
        return start;
    }

    /**
     * Returns selection end position where selection is not including character at the end position.
     *
     * @return selection end position.
     */
    public long getEnd() {
        return end;
    }

    /**
     * Returns first data position of the selection.
     *
     * @return data position
     */
    public long getFirst() {
        return Math.min(start, end);
    }

    /**
     * Returns last data position of the selection.
     *
     * @return data position
     */
    public long getLast() {
        return start < end ? end - 1 : start - 1;
    }

    /**
     * Returns length of the selected area.
     *
     * @return length in bytes
     */
    public long getLength() {
        return start < end ? end - start : start - end;
    }

    /**
     * Returns true if selection is empty.
     *
     * @return true if selection is empty
     */
    public boolean isEmpty() {
        return start == end;
    }

    /**
     * Checks if position belongs to this selection.
     *
     * @param position position
     * @return true if position belongs to current selection range.
     */
    public boolean isInSelection(long position) {
        return start < end ? position >= start && position < end : position >= end && position < start;
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
        final SelectionRange other = (SelectionRange) obj;
        if (this.start != other.start) {
            return false;
        }
        return this.end == other.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
