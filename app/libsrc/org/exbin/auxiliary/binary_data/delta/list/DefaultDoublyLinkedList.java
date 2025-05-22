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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;

/**
 * Default implementation of doubly linked list of items.
 *
 * @author ExBin Project (https://exbin.org)
 * @param <T> doubly linked list item
 */
@ParametersAreNonnullByDefault
public class DefaultDoublyLinkedList<T extends DoublyLinkedItem<T>> implements DoublyLinkedList<T> {

    private T first;

    /* Cached values. */
    private T last;
    private int size = 0;

    @Nullable
    @Override
    public T first() {
        return first;
    }

    @Nullable
    @Override
    public T last() {
        return last;
    }

    @Nullable
    @Override
    public T nextTo(T item) {
        return item.getNext();
    }

    @Nullable
    @Override
    public T prevTo(T item) {
        return item.getPrev();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Nullable
    @Override
    public T get(int index) {
        T item = first;
        while (index > 0) {
            item = nextTo(item);
            index--;
        }

        return item;
    }

    @Nonnull
    @Override
    public T set(int index, T item) {
        T origItem = first;
        while (index > 0) {
            origItem = nextTo(origItem);
            index--;
        }

        if (origItem == null) {
            throw new OutOfBoundsException("No item for index " + index);
        }

        T itemPrev = origItem.getPrev();
        T itemNext = origItem.getNext();
        origItem.setNext(null);
        origItem.setPrev(null);
        item.setPrev(itemPrev);
        item.setNext(itemNext);
        if (last == origItem) {
            last = item;
        }

        return origItem;
    }

    @Override
    public boolean contains(Object item) {
        T checkedItem = first;
        while (checkedItem != null) {
            if (checkedItem.equals(item)) {
                return true;
            }
            checkedItem = nextTo(checkedItem);
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsAll(Collection<?> items) {
        for (Object item : items) {
            if (!contains((T) item)) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private T current = first;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T result = current;
                current = result.getNext();
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        if (last == null) {
            return new Object[0];
        }
        int count = indexOf(last);
        Object[] result = new Object[count];
        int index = 0;
        T item = first;
        while (item != null) {
            result[index] = item;
            item = nextTo(item);
            index++;
        }
        return result;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <X> X[] toArray(X[] template) {
        int count = last == null ? 0 : indexOf(last);
        X[] result = template.length >= count ? template : Arrays.copyOf(template, count);
        int index = 0;
        T item = first;
        while (item != null) {
            result[index] = (X) item;
            item = nextTo(item);
            index++;
        }
        return result;
    }

    @Override
    public boolean add(T item) {
        if (last != null) {
            last.setNext(item);
            item.setPrev(last);
            item.setNext(null);
            last = item;
        } else {
            first = item;
            last = item;
            item.setNext(null);
            item.setPrev(null);
        }

        size++;
        return true;
    }

    @Override
    public void add(int index, T item) {
        if (index == 0 && size == 0) {
            add(item);
            return;
        }

        if (index > size) {
            throw new OutOfBoundsException("Index " + index + " is greater than " + size);
        } else if (index == size) {
            last.setNext(item);
            item.setPrev(last);
            item.setNext(null);
            last = item;
        } else if (index == 0) {
            first.setPrev(item);
            item.setNext(first);
            first = item;
        } else {
            T indexItem = Objects.requireNonNull(get(index));
            T prevItem = Objects.requireNonNull(indexItem.getPrev());
            item.setPrev(prevItem);
            item.setNext(indexItem);
            prevItem.setNext(item);
            indexItem.setPrev(item);
        }
        size++;
    }

    /**
     * Adds item after given listItem.
     * <p>
     * ListItem must be part of the list otherwise list will become broken. Item
     * should not be member of other list
     *
     * @param listItem item from the current list
     * @param item newly added item
     */
    public void addAfter(T listItem, T item) {
        T listItemNext = listItem.getNext();
        if (listItemNext == null) {
            listItem.setNext(item);
            item.setPrev(listItem);
            item.setNext(null);
            last = item;
        } else {
            listItem.setNext(item);
            listItemNext.setPrev(item);
            item.setPrev(listItem);
            item.setNext(listItemNext);
        }
        size++;
    }

    public void addBefore(T targetItem, T item) {
        T prev = targetItem.getPrev();
        if (prev == null) {
            targetItem.setPrev(item);
            item.setNext(targetItem);
            item.setPrev(null);
            first = item;
        } else {
            targetItem.setPrev(item);
            prev.setNext(item);
            item.setNext(targetItem);
            item.setPrev(prev);
        }
        size++;
    }

    @Override
    public boolean addAll(Collection<? extends T> elementsToAdd) {
        boolean result = false;
        for (T element : elementsToAdd) {
            result |= add(element);
        }

        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for (T t : c) {
            add(index, t);
            index++;
        }

        return true;
    }

    @Override
    public int indexOf(Object searchedItem) {
        T item = first;
        int index = 0;
        while (item != null) {
            if (item.equals(searchedItem)) {
                return index;
            }
            item = item.getNext();
            index++;
        }

        return -1;
    }

    @Override
    @Nullable
    public T remove(int index) {
        T item = get(index);
        if (item != null) {
            removeItem(item);
            return item;
        }

        return null;
    }

    @Override
    public boolean remove(Object o) {
        @SuppressWarnings("unchecked")
        T item = (T) o;
        if (item != null) {
            removeItem(item);
            return true;
        }

        return false;
    }

    private void removeItem(T item) {
        if (item == first) {
            T itemNext = item.getNext();
            if (itemNext != null) {
                itemNext.setPrev(null);
            } else {
                last = null;
            }
            first = itemNext;
        } else {
            T prev = Objects.requireNonNull(item.getPrev());
            T next = item.getNext();

            prev.setNext(next);
            if (next != null) {
                next.setPrev(prev);
            } else {
                last = prev;
            }
        }

        size--;
        item.setPrev(null);
        item.setNext(null);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        for (Object item : collection) {
            @SuppressWarnings("unchecked")
            T itemList = (T) item;
            result |= remove(itemList);
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> retainList) {
        boolean changed = false;
        if (first == null) {
            return false;
        }

        T item = first;
        do {
            if (!retainList.contains(item)) {
                changed = true;
                T nextItem = item.getNext();
                removeItem(item);
                item = nextItem;
            } else {
                item = item.getNext();
            }
        } while (item != null);
        return changed;
    }

    @Override
    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public int lastIndexOf(Object searchedItem) {
        if (size > 0) {
            T item = last;
            int index = size - 1;
            while (item != null) {
                if (item.equals(searchedItem)) {
                    return index;
                }
                item = item.getPrev();
                index--;
            }
        }

        return -1;
    }

    @Nonnull
    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Nonnull
    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Nonnull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
