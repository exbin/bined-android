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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.delta.list.DefaultDoublyLinkedList;

/**
 * Delta document defined as a sequence of segments.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeltaDocument implements EditableBinaryData {

    private final SegmentsRepository repository;
    private DataSource dataSource;
    private final DefaultDoublyLinkedList<DataSegment> segments = new DefaultDoublyLinkedList<>();

    private long dataLength = 0;
    private final DeltaDocumentWindow pointerWindow;
    private final List<DeltaDocumentChangedListener> changeListeners = new ArrayList<>();

    private static final int BUFFER_SIZE = 4096;

    public DeltaDocument(SegmentsRepository repository, DataSource dataSource) throws IOException {
        this.repository = repository;
        this.dataSource = dataSource;
        dataLength = dataSource.getDataLength();
        if (dataLength > 0) {
            DataSegment fullFileSegment = repository.createSourceSegment(dataSource, 0, dataLength);
            segments.add(fullFileSegment);
        }
        pointerWindow = new DeltaDocumentWindow(this);
        pointerWindow.reset();
    }

    public DeltaDocument(SegmentsRepository repository) {
        this.repository = repository;
        dataLength = 0;
        pointerWindow = new DeltaDocumentWindow(this);
        pointerWindow.reset();
    }

    /**
     * Method for accessing data pages.
     * <p>
     * Use only if you know what you are doing.
     *
     * @return segments
     */
    @Nonnull
    public DefaultDoublyLinkedList<DataSegment> getSegments() {
        return segments;
    }

    /**
     * Returns segment starting at or before given position and ending after it.
     * <p>
     * Returns null if position is at the end or after then end of the document.
     *
     * @param position requested position
     * @return data segment or null
     */
    @Nullable
    public DataSegment getSegment(long position) {
        return pointerWindow.getSegment(position);
    }

    @Override
    public boolean isEmpty() {
        return dataLength == 0;
    }

    @Override
    public long getDataSize() {
        return dataLength;
    }

    @Override
    public byte getByte(long position) {
        return pointerWindow.getByte(position);
    }

    @Override
    public void setByte(long position, byte value) {
        pointerWindow.setByte(position, value);
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        pointerWindow.insertUninitialized(startFrom, length);
    }

    @Override
    public void insert(long startFrom, long length) {
        pointerWindow.insert(startFrom, length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        pointerWindow.insert(startFrom, insertedData);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        pointerWindow.insert(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        pointerWindow.insert(startFrom, insertedData);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        pointerWindow.insert(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    /**
     * Directly inserts segment into given position.
     *
     * @param startFrom start position
     * @param segment inserted segment
     */
    public void insertSegment(long startFrom, DataSegment segment) {
        pointerWindow.insertSegment(startFrom, segment);
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long maximumDataSize) throws IOException {
        // TODO optimization later
        long processed = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        while (maximumDataSize == -1 || maximumDataSize > 0) {
            int toRead = BUFFER_SIZE;
            if (maximumDataSize >= 0 && maximumDataSize < toRead) {
                toRead = (int) maximumDataSize;
            }
            int read = inputStream.read(buffer, 0, toRead);
            if (read == -1) {
                break;
            }
            pointerWindow.insert(startFrom, buffer, 0, read);
            if (maximumDataSize >= 0) {
                maximumDataSize -= read;
            }
            startFrom += read;
            processed += read;
        }

        return processed;
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        remove(targetPosition, replacingData.getDataSize());
        insert(targetPosition, replacingData);
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long length) {
        remove(targetPosition, length);
        insert(targetPosition, replacingData, startFrom, length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        remove(targetPosition, replacingData.length);
        insert(targetPosition, replacingData);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        remove(targetPosition, length);
        insert(targetPosition, replacingData, replacingDataOffset, length);
    }

    /**
     * Directly replaces segment into given position.
     *
     * @param targetPosition target position
     * @param segment inserted segment
     */
    public void replaceSegment(long targetPosition, DataSegment segment) {
        remove(targetPosition, segment.getLength());
        insertSegment(targetPosition, segment);
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(long startFrom, long length) {
        pointerWindow.remove(startFrom, length);
    }

    @Override
    public void clear() {
        dataLength = 0;
        segments.clear();
        pointerWindow.reset();
    }

    @Override
    public void dispose() {
        repository.dropDocument(this);
    }

    @Override
    public void loadFromStream(InputStream stream) throws IOException {
        clear();
        DeltaDocumentWindow documentWindow = new DeltaDocumentWindow(this);
        byte[] buffer = new byte[BUFFER_SIZE];

        long position = 0;
        int read;
        do {
            read = stream.read(buffer);
            if (read > 0) {
                documentWindow.insert(position, buffer, 0, read);
                position += read;
            }
        } while (read >= 0);
    }

    @Override
    public void saveToStream(OutputStream stream) throws IOException {
        DeltaDocumentWindow documentWindow = new DeltaDocumentWindow(this);
        byte[] buffer = new byte[BUFFER_SIZE];

        long position = 0;
        long dataSize = getDataSize();
        while (position < dataSize) {
            long remains = dataSize - position;
            int toProcess = remains < BUFFER_SIZE ? (int) remains : BUFFER_SIZE;
            documentWindow.copyToArray(position, buffer, 0, toProcess);
            stream.write(buffer, 0, toProcess);
            position += toProcess;
        }
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        return pointerWindow.copy();
    }

    @Nonnull
    @Override
    public BinaryData copy(long startFrom, long length) {
        return pointerWindow.copy(startFrom, length);
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        // TODO optimization later
        for (int i = 0; i < length; i++) {
            target[offset + i] = getByte(startFrom + i);
        }
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        return new DeltaDocumentOutputStream(this);
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        return new DeltaDocumentInputStream(this);
    }

    @Override
    public void setDataSize(long dataSize) {
        if (dataSize < dataLength) {
            remove(dataSize, dataLength - dataSize);
        } else if (dataSize > dataLength) {
            insert(dataLength, dataSize - dataLength);
        }
    }

    /**
     * Performs save to source file.
     *
     * @throws java.io.IOException on input/output error
     */
    public void save() throws IOException {
        repository.saveDocument(this);
    }

    /**
     * Resets cached state - needed after change.
     */
    public void clearCache() {
        pointerWindow.reset();
    }

    /* package */ void setDataLength(long dataSize) {
        this.dataLength = dataSize;
    }

    /**
     * Returns segment starting from given position or copy of part of the
     * segment starting from given position up to the end of length.
     *
     * @param position position
     * @param length length
     * @return data segment
     */
    @Nullable
    public DataSegment getPartCopy(long position, long length) {
        return pointerWindow.getPartCopy(position, length);
    }

    @Nullable
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nonnull
    public SegmentsRepository getRepository() {
        return repository;
    }

    public void addChangeListener(DeltaDocumentChangedListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(DeltaDocumentChangedListener listener) {
        changeListeners.remove(listener);
    }

    public void notifyChangeListeners(DeltaDocumentWindow window) {
        for (DeltaDocumentChangedListener listener : changeListeners) {
            listener.dataChanged(window);
        }
    }

    public void validatePointerPosition() {
        pointerWindow.validatePointerPosition();
    }

    public void validateDocumentSize() {
        long segmentsSizeSum = 0;
        DataSegment segment = segments.first();
        while (segment != null) {
            segmentsSizeSum += segment.getLength();
            segment = segment.getNext();
        }

        if (segmentsSizeSum != getDataSize()) {
            throw new IllegalStateException("Invalid size " + getDataSize() + " (expected " + segmentsSizeSum + ")");
        }
    }

    public void validate() {
        validatePointerPosition();
        validateDocumentSize();
    }
}
