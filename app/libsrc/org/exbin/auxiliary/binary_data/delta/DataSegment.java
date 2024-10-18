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
package org.exbin.auxiliary.binary_data.delta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.delta.list.DoublyLinkedItem;

/**
 * Abstract data segment of delta data source.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class DataSegment implements DoublyLinkedItem<DataSegment> {

    @Nullable
    private DataSegment previous;
    @Nullable
    private DataSegment next;

    public DataSegment() {
    }

    /**
     * Returns start position.
     *
     * @return start position
     */
    public abstract long getStartPosition();

    /**
     * Returns length of this segment in bytes.
     *
     * @return length of this segment
     */
    public abstract long getLength();

    /**
     * Returns detached copy of this segment.
     *
     * @return copy of this segment
     */
    @Nonnull
    public abstract DataSegment copy();

    @Nullable
    @Override
    public DataSegment getNext() {
        return next;
    }

    @Override
    public void setNext(@Nullable DataSegment next) {
        this.next = next;
    }

    @Nullable
    @Override
    public DataSegment getPrev() {
        return previous;
    }

    @Override
    public void setPrev(@Nullable DataSegment previous) {
        this.previous = previous;
    }
}
