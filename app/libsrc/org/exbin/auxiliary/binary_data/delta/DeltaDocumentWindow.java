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
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;
import org.exbin.auxiliary.binary_data.delta.list.DefaultDoublyLinkedList;

/**
 * Access window for delta document.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeltaDocumentWindow {

    @Nonnull
    private final DeltaDocument document;

    private final DataPointer pointer = new DataPointer();

    public DeltaDocumentWindow(DeltaDocument document) {
        this.document = document;
        document.addChangeListener((DeltaDocumentWindow window) -> {
            if (window != DeltaDocumentWindow.this) {
                pointer.segment = window.pointer.segment;
                pointer.position = window.pointer.position;
            }
        });
    }

    public long getDataSize() {
        return document.getDataSize();
    }

    public byte getByte(long position) {
        focusSegment(position);

        DataSegment targetSegment = pointer.segment;
        if (targetSegment == null) {
            throw new NullPointerException("Missing segment for position " + position);
        }
        if (targetSegment instanceof SourceSegment) {
            return ((SourceSegment) targetSegment).getByte(targetSegment.getStartPosition() + (position - pointer.position));
        } else {
            return ((MemorySegment) targetSegment).getByte(targetSegment.getStartPosition() + (position - pointer.position));
        }
    }

    public void setByte(long position, byte value) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        focusSegment(position);

        if (pointer.segment instanceof SourceSegment) {
            if (pointer.position != position) {
                splitSegment(position);
                focusSegment(position);
            }

            DataSegment prev = segments.prevTo(pointer.segment);
            if (prev instanceof MemorySegment) {
                repository.setMemoryByte((MemorySegment) prev, prev.getLength(), value);
            } else {
                MemorySegment segment = repository.createMemorySegment(repository.openMemorySource(), 0, 0);
                repository.setMemoryByte(segment, 0, value);
                segments.addBefore(pointer.segment, segment);
            }
            pointer.position++;
            SourceSegment documentSegment = ((SourceSegment) pointer.segment);
            if (documentSegment.getLength() == 1) {
                segments.remove(documentSegment);
                repository.dropSegment(documentSegment);
                pointer.position = 0;
                pointer.segment = null;
            } else {
                repository.updateSegment(documentSegment, documentSegment.getStartPosition() + 1, documentSegment.getLength() - 1);
            }
        } else {
            if (pointer.segment == null) {
                pointer.segment = repository.createMemorySegment(repository.openMemorySource(), 0, 0);
                segments.add(pointer.segment);
            }
            repository.setMemoryByte((MemorySegment) pointer.segment, position - pointer.position, value);
        }

        if (position >= getDataSize()) {
            document.setDataLength(position + 1);
        }
        document.notifyChangeListeners(this);
    }

    public void insertUninitialized(long startFrom, long length) {
        if (length == 0) {
            return;
        }

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();

        focusSegment(startFrom);
        long targetLength = document.getDataSize() + length;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertUninitializedMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, length);
            document.setDataLength(targetLength);
        } else if (pointer.segment != null && pointer.position == startFrom && pointer.segment.getPrev() instanceof MemorySegment
                && pointer.segment.getPrev().getStartPosition() + pointer.segment.getPrev().getLength() == ((MemorySegment) pointer.segment.getPrev()).getSource().getDataSize()) {
            MemorySegment prevSegment = (MemorySegment) pointer.segment.getPrev();
            prevSegment.getSource().insertUninitialized(prevSegment.getSource().getDataSize(), length);
            repository.updateSegmentLength(prevSegment, prevSegment.getLength() + length);
            pointer.position++;
            document.setDataLength(targetLength);
        } else if (pointer.segment == null && segments.last() instanceof MemorySegment
                && segments.last().getStartPosition() + segments.last().getLength() == ((MemorySegment) segments.last()).getSource().getDataSize()) {
            MemorySegment prevSegment = (MemorySegment) segments.last();
            prevSegment.getSource().insertUninitialized(prevSegment.getSource().getDataSize(), length);
            repository.updateSegmentLength(prevSegment, prevSegment.getLength() + length);
            pointer.position++;
            document.setDataLength(targetLength);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertUninitializedMemoryData(insertedSegment, 0, length);
            if (pointer.segment == null) {
                segments.add(insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, length);
        }
        document.notifyChangeListeners(this);
    }

    public void insert(long startFrom, long length) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        if (length == 0) {
            return;
        }

        focusSegment(startFrom);
        long targetLength = document.getDataSize() + length;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, length);
            document.setDataLength(targetLength);
        } else if (pointer.segment != null && pointer.position == startFrom && pointer.segment.getPrev() instanceof MemorySegment
                && pointer.segment.getPrev().getStartPosition() + pointer.segment.getPrev().getLength() == ((MemorySegment) pointer.segment.getPrev()).getSource().getDataSize()) {
            MemorySegment prevSegment = (MemorySegment) pointer.segment.getPrev();
            prevSegment.getSource().insert(prevSegment.getSource().getDataSize(), length);
            repository.updateSegmentLength(prevSegment, prevSegment.getLength() + length);
            pointer.position++;
            document.setDataLength(targetLength);
        } else if (pointer.segment == null && segments.last() instanceof MemorySegment
                && segments.last().getStartPosition() + segments.last().getLength() == ((MemorySegment) segments.last()).getSource().getDataSize()) {
            MemorySegment prevSegment = (MemorySegment) segments.last();
            prevSegment.getSource().insert(prevSegment.getSource().getDataSize(), length);
            repository.updateSegmentLength(prevSegment, prevSegment.getLength() + length);
            pointer.position++;
            document.setDataLength(targetLength);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData(insertedSegment, 0, length);
            if (pointer.segment == null) {
                segments.add(insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, length);
        }
        document.notifyChangeListeners(this);
    }

    public void insert(long startFrom, byte[] insertedData) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        if (insertedData.length == 0) {
            return;
        }

        focusSegment(startFrom);
        long targetLength = document.getDataSize() + insertedData.length;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData);
            document.setDataLength(targetLength);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData(insertedSegment, 0, insertedData);
            if (pointer.segment == null) {
                segments.add(insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, insertedData.length);
        }
        document.notifyChangeListeners(this);
    }

    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();

        focusSegment(startFrom);
        long targetLength = document.getDataSize() + insertedDataLength;
        if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData);
            document.setDataLength(targetLength);
        } else {
            if (startFrom > pointer.position) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData(insertedSegment, 0, insertedData);
            if (pointer.segment == null) {
                segments.add(insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, insertedData.length);
        }
        document.notifyChangeListeners(this);
    }

    public void insert(long startFrom, BinaryData insertedData) {
        if (insertedData.isEmpty()) {
            return;
        }

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();

        focusSegment(startFrom);
        long targetLength = document.getDataSize() + insertedData.getDataSize();
        if (insertedData instanceof DeltaDocument) {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }

            // Copy all segments from inserted document
            DeltaDocument insertedDocument = (DeltaDocument) insertedData;
            DataSegment segment = insertedDocument.getSegments().first();
            DataSegment copy = repository.copySegment(segment);
            DataSegment first = copy;
            if (pointer.segment == null) {
                segments.add(copy);
            } else {
                segments.addBefore(pointer.segment, copy);
            }
            DataSegment next = segment.getNext();
            while (next != null) {
                DataSegment nextCopy = repository.copySegment(next);
                segments.addAfter(copy, nextCopy);
                copy = nextCopy;
                next = next.getNext();
            }
            pointer.segment = first;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, insertedData.getDataSize());
        } else if (pointer.segment instanceof MemorySegment) {
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData);
            document.setDataLength(targetLength);
        } else {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData(insertedSegment, 0, insertedData);
            if (pointer.segment == null) {
                segments.add(insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, insertedData.getDataSize());
        }
        document.notifyChangeListeners(this);
    }

    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        if (insertedDataLength == 0) {
            return;
        }

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        focusSegment(startFrom);
        long targetLength = document.getDataSize() + insertedDataLength;
        if (insertedData instanceof DeltaDocument) {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }

            // Copy all segments from inserted document
            DeltaDocument insertedDocument = (DeltaDocument) insertedData;

            long position = insertedDataOffset;
            long length = insertedDataLength;
            DataSegment segment = insertedDocument.getPartCopy(position, length);
            position += segment.getLength();
            length -= segment.getLength();
            DataSegment first = segment;
            if (pointer.segment == null) {
                segments.add(segment);
            } else {
                segments.addBefore(pointer.segment, segment);
            }
            DataSegment next = segment.getNext();
            while (length > 0) {
                DataSegment nextSegment = insertedDocument.getPartCopy(position, length);
                position += nextSegment.getLength();
                length -= nextSegment.getLength();
                segments.addAfter(segment, nextSegment);
                segment = nextSegment;
                next = next.getNext();
            }
            pointer.segment = first;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, insertedData.getDataSize());
        } /*else if (pointer.segment instanceof MemorySegment) {
            // TODO can't do that directly, also it breaks
            repository.insertMemoryData((MemorySegment) pointer.segment, startFrom - pointer.position, insertedData, insertedDataOffset, insertedDataLength);
            document.setDataLength(targetLength);
        } */ else {
            if (pointer.position < startFrom) {
                splitSegment(startFrom);
                focusSegment(startFrom);
            }
            MemorySegment insertedSegment = repository.createMemorySegment();
            repository.insertMemoryData(insertedSegment, 0, insertedData, insertedDataOffset, insertedDataLength);
            if (pointer.segment == null) {
                segments.add(insertedSegment);
            } else {
                segments.addBefore(pointer.segment, insertedSegment);
            }
            pointer.segment = insertedSegment;
            document.setDataLength(targetLength);
            tryMergeArea(startFrom, insertedData.getDataSize());
        }
        document.notifyChangeListeners(this);
    }

    /**
     * Directly inserts segment into given position.
     *
     * @param startFrom start position
     * @param insertedSegment inserted segment
     */
    public void insertSegment(long startFrom, DataSegment insertedSegment) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        long targetLength = document.getDataSize() + insertedSegment.getLength();
        focusSegment(startFrom);
        if (pointer.position < startFrom) {
            splitSegment(startFrom);
            focusSegment(startFrom);
        }
        if (pointer.segment == null) {
            segments.add(insertedSegment);
        } else {
            segments.addBefore(pointer.segment, insertedSegment);
        }
        pointer.segment = insertedSegment;
        document.setDataLength(targetLength);
        document.notifyChangeListeners(this);
    }

    public void remove(long startFrom, long length) {
        if (startFrom + length > document.getDataSize()) {
            throw new OutOfBoundsException("Removed area is out of bounds");
        }

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        if (length > 0) {
            long targetLength = document.getDataSize() - length;
            focusSegment(startFrom + length);
            splitSegment(startFrom + length);
            focusSegment(startFrom);
            splitSegment(startFrom);
            focusSegment(startFrom);

            // Save position to return to
            DataSegment prevSegment = (DataSegment) pointer.segment.getPrev();
            long prevPointerPosition = prevSegment == null ? 0 : pointer.position - prevSegment.getLength();

            // Drop all segments in given range
            while (length > 0) {
                length -= pointer.segment.getLength();
                DataSegment next = segments.nextTo(pointer.segment);
                repository.dropSegment(pointer.segment);
                segments.remove(pointer.segment);
                pointer.segment = next;
            }

            // Set pointer position
            pointer.segment = prevSegment;
            pointer.position = prevPointerPosition;
            document.setDataLength(targetLength);
            tryMergeSegments(startFrom);
        }
        document.notifyChangeListeners(this);
    }

    public void reset() {
        pointer.setPointer(0, document.getSegments().first());
    }

    public void setDataSize(long dataSize) {
        document.setDataSize(dataSize);
    }

    @Nonnull
    public BinaryData copy() {
        SegmentsRepository repository = document.getRepository();
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        DeltaDocument copy = repository.createDocument();
        copy.setDataLength(getDataSize());
        for (DataSegment segment : segments) {
            copy.getSegments().add(repository.copySegment(segment));
        }
        return copy;
    }

    @Nonnull
    public BinaryData copy(long startFrom, long length) {
        SegmentsRepository repository = document.getRepository();
        DeltaDocument copy = repository.createDocument();
        copy.setDataLength(length);
        focusSegment(startFrom);

        if (length > 0) {
            DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
            DataSegment segment = pointer.segment;
            if (segment == null) {
                throw new NullPointerException("Segment on given position not found");
            }
            long offset = startFrom - pointer.position;
            while (length > 0) {
                long segmentLength = segment.getLength();
                long copyLength = segmentLength - offset;
                if (copyLength > length) {
                    copyLength = length;
                }

                if (offset == 0 && copyLength == segmentLength) {
                    copy.getSegments().add(repository.copySegment(segment));
                } else if (segment instanceof MemorySegment) {
                    MemorySegment memorySegment = (MemorySegment) segment;
                    copy.getSegments().add(repository.createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + offset, copyLength));
                } else {
                    SourceSegment fileSegment = (SourceSegment) segment;
                    copy.getSegments().add(repository.createSourceSegment(fileSegment.getSource(), fileSegment.getStartPosition() + offset, copyLength));
                }
                length -= copyLength;
                offset = 0;
                segment = segments.nextTo(segment);
                if (length > 0 && segment == null) {
                    throw new NullPointerException("Unexpected end of segments sequence");
                }
            }
        }

        return copy;
    }

    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        document.copyToArray(startFrom, target, offset, length);
    }

    /**
     * Splits current pointer segment on given absolute position.
     *
     * @param position split position
     */
    public void splitSegment(long position) {
        long pointerPosition = pointer.position;
        DataSegment pointerSegment = pointer.segment;
        if (position < pointerPosition || (pointerSegment != null && position > pointerPosition + pointerSegment.getLength())) {
            throw new IllegalStateException("Split position is out of current segment");
        }

        if (pointerPosition == position || pointerSegment == null || pointerPosition + pointerSegment.getLength() == position) {
            // No action needed
            return;
        }

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        long firstPartSize = position - pointerPosition;
        if (pointerSegment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) pointerSegment;
            MemorySegment newSegment = repository.createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + firstPartSize, memorySegment.getLength() - firstPartSize);
            repository.updateSegmentLength(memorySegment, firstPartSize);
            segments.addAfter(pointerSegment, newSegment);
        } else {
            SourceSegment fileSegment = (SourceSegment) pointerSegment;
            SourceSegment newSegment = repository.createSourceSegment(fileSegment.getSource(), fileSegment.getStartPosition() + firstPartSize, fileSegment.getLength() - firstPartSize);
            repository.updateSegmentLength(fileSegment, firstPartSize);
            segments.addAfter(fileSegment, newSegment);
        }
    }

    @Nullable
    public DataSegment getSegment(long position) {
        focusSegment(position);
        return pointer.segment;
    }

    /**
     * Returns segment starting from given position or copy of part of the
     * segment starting from given position up to the end of length.
     *
     * @param position position
     * @param length length
     * @return data segment or null
     */
    @Nullable
    public DataSegment getPartCopy(long position, long length) {
        focusSegment(position);
        if (pointer.segment == null) {
            return null;
        }

        SegmentsRepository repository = document.getRepository();
        long offset = position - pointer.position;
        long partLength = length;
        if (pointer.segment.getLength() - offset < partLength) {
            partLength = pointer.segment.getLength() - offset;
        }
        return repository.copySegment(pointer.segment, offset, partLength);
    }

    /**
     * Focuses segment starting at or before given position and ending after it.
     * <p>
     * Returns null if position is at the end of the document and throws out of
     * bounds exception otherwise.
     *
     * @param position requested position
     * @throws OutOfBoundsException if position is before or after document
     */
    private void focusSegment(long position) {
        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        long dataSize = getDataSize();
        if (position == 0) {
            pointer.position = 0;
            pointer.segment = segments.first();
            return;
        } else if (position == dataSize) {
            pointer.position = dataSize;
            pointer.segment = null;
            return;
        } else if (position < 0 || position > dataSize) {
            throw new OutOfBoundsException("Position index out of range");
        }

        if (pointer.segment == null) {
            pointer.segment = segments.last();
            pointer.position = dataSize - pointer.segment.getLength();
        }

        if (position < pointer.position) {
            if (pointer.segment == null && position == dataSize) {
                pointer.segment = segments.last();
                if (pointer.segment == null) {
                    throwNullSegmentException();
                }
                pointer.position -= pointer.segment.getLength();
            }

            while (position < pointer.position) {
                pointer.segment = segments.prevTo(pointer.segment);
                if (pointer.segment == null) {
                    throwNullSegmentException();
                }
                pointer.position -= pointer.segment.getLength();
            }
        } else {
            if (pointer.segment == null) {
                throwNullSegmentException();
            }

            while (position >= pointer.position + pointer.segment.getLength()) {
                if ((pointer.segment.getNext() == null) && (position == pointer.position + pointer.segment.getLength())) {
                    break;
                }
                pointer.position += pointer.segment.getLength();
                pointer.segment = segments.nextTo(pointer.segment);
                if (pointer.segment == null) {
                    throwNullSegmentException();
                }
            }
        }
    }

    private void tryMergeArea(long position, long length) {
        tryMergeSegments(position);
        // TODO
        tryMergeSegments(position + length);
    }

    /**
     * Attempts to merge segments at specified position.
     *
     * @param position target position
     */
    private boolean tryMergeSegments(long position) {
        if (position == 0 || position >= getDataSize()) {
            return false;
        }

        DefaultDoublyLinkedList<DataSegment> segments = document.getSegments();
        SegmentsRepository repository = document.getRepository();
        focusSegment(position);
        DataSegment nextSegment = pointer.segment;
        focusSegment(position - 1);
        DataSegment segment = pointer.segment;
        if (segment == nextSegment) {
            return false;
        }

        if (segment instanceof SourceSegment && nextSegment instanceof SourceSegment) {
            if (((SourceSegment) segment).getStartPosition() + segment.getLength() == ((SourceSegment) nextSegment).getStartPosition()) {
                repository.updateSegmentLength(segment, segment.getLength() + nextSegment.getLength());
                repository.dropSegment(nextSegment);
                segments.remove(nextSegment);
                return true;
            }

            return false;
        }

        if (segment instanceof MemorySegment && nextSegment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) segment;
            MemorySegment nextMemorySegment = (MemorySegment) nextSegment;
            if (memorySegment.getSource() == nextMemorySegment.getSource()) {
                if (memorySegment.getStartPosition() + segment.getLength() == nextMemorySegment.getStartPosition()) {
                    repository.updateSegmentLength(memorySegment, segment.getLength() + nextSegment.getLength());
                    repository.dropSegment(nextSegment);
                    segments.remove(nextSegment);
                    return true;
                }
            }
            // TODO join two single memory segments?
        }

        return false;
    }

    public void validatePointerPosition() {
        if (pointer.segment == null) {
            if (pointer.position > 0 && pointer.position < getDataSize()) {
                throw new IllegalStateException("Illegal pointer position " + pointer.position + " for null segment");
            }
        } else {
            long segmentsLengthSum = 0;
            DataSegment segment = pointer.segment.getPrev();
            while (segment != null) {
                segmentsLengthSum += segment.getLength();
                segment = segment.getPrev();
            }

            if (segmentsLengthSum != pointer.position) {
                throw new IllegalStateException("Illegal pointer position " + pointer.position);
            }
        }
    }

    private static void throwNullSegmentException() {
        throw new IllegalStateException("Unexpected null segment");
    }

    /**
     * POJO structure for sliding data pointer.
     */
    private static class DataPointer {

        long position;
        @Nullable
        DataSegment segment;

        void setPointer(long position, @Nullable DataSegment segment) {
            this.position = position;
            this.segment = segment;
        }
    }
}
