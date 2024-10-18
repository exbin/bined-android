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
package org.exbin.auxiliary.binary_data.delta.list;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Doubly linked list of items.
 *
 * @author ExBin Project (https://exbin.org)
 * @param <T> doubly linked list item
 */
@ParametersAreNonnullByDefault
public interface DoublyLinkedList<T> extends List<T> {

    /**
     * Returns first item of the list.
     *
     * @return first item
     */
    @Nullable
    T first();

    /**
     * Returns last item of the list.
     *
     * @return last item
     */
    @Nullable
    T last();

    /**
     * Returns item next to given item.
     *
     * @param item item
     * @return next item or null
     */
    @Nullable
    T nextTo(T item);

    /**
     * Returns item previous to given item.
     *
     * @param item item
     * @return previous item or null
     */
    @Nullable
    T prevTo(T item);
}
