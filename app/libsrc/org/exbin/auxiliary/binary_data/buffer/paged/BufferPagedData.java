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
package org.exbin.auxiliary.binary_data.buffer.paged;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.BinaryDataInputStream;
import org.exbin.auxiliary.binary_data.BinaryDataOutputStream;
import org.exbin.auxiliary.binary_data.buffer.BufferData;
import org.exbin.auxiliary.binary_data.buffer.BufferEditableData;
import org.exbin.auxiliary.binary_data.DataOverflowException;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.auxiliary.binary_data.paged.DataPageCreator;

/**
 * Paged data stored using byte buffer.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BufferPagedData implements PagedData {

    public static final int DEFAULT_PAGE_SIZE = 4096;
    public static final long MAX_DATA_SIZE = Long.MAX_VALUE;

    protected int pageSize = DEFAULT_PAGE_SIZE;
    @Nonnull
    protected final List<BufferData> data = new ArrayList<>();

    @Nullable
    protected DataPageCreator dataPageCreator = null;

    public BufferPagedData() {
    }

    public BufferPagedData(DataPageCreator dataPageCreator) {
        this.dataPageCreator = dataPageCreator;
    }

    public BufferPagedData(int pageSize) {
        if (pageSize < 1) {
            throw new InvalidParameterException("Page size cannot be less than 1");
        }
        this.pageSize = pageSize;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public long getDataSize() {
        return (data.size() > 1 ? (long) (data.size() - 1) * pageSize : 0) + (!data.isEmpty() ? data.get(data.size() - 1).getDataSize() : 0);
    }

    @Override
    public void setDataSize(long size) {
        if (size < 0) {
            throw new InvalidParameterException("Size cannot be negative");
        }

        long dataSize = getDataSize();
        if (size > dataSize) {
            int lastPage = (int) (dataSize / pageSize);
            int lastPageSize = (int) (dataSize % pageSize);
            long remaining = size - dataSize;
            // extend last page
            if (lastPageSize > 0) {
                BufferData page = getPage(lastPage);
                int nextPageSize = remaining + lastPageSize > pageSize ? pageSize : (int) remaining + lastPageSize;
                BufferData newPage = createNewPage(nextPageSize);
                page.getData().rewind();
                newPage.getData().put(page.getData());
                setPage(lastPage, newPage);
                remaining -= (nextPageSize - lastPageSize);
                lastPage++;
            }

            while (remaining > 0) {
                int nextPageSize = remaining > pageSize ? pageSize : (int) remaining;
                data.add(createNewPage(nextPageSize));
                remaining -= nextPageSize;
            }
        } else if (size < dataSize) {
            int lastPage = (int) (size / pageSize);
            int lastPageSize = (int) (size % pageSize);
            // shrink last page
            if (lastPageSize > 0) {
                BufferData page = getPage(lastPage);
                BufferData newPage = createNewPage(lastPageSize);
                BufferPagedData.put(newPage.getData(), 0, page.getData(), 0, lastPageSize);
                setPage(lastPage, newPage);
                lastPage++;
            }

            for (int pageIndex = data.size() - 1; pageIndex >= lastPage; pageIndex--) {
                data.remove(pageIndex);
            }
        }
    }

    @Override
    public byte getByte(long position) {
        BufferData page = getPage((int) (position / pageSize));
        try {
            return page.getByte(position % pageSize);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        BufferData page = getPage((int) (position / pageSize));
        try {
            page.getData().put((int) (position % pageSize), value);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of inserted block must be non-negative");
        }
        if (startFrom < 0) {
            throw new IllegalArgumentException("Position of inserted block must be non-negative");
        }
        long dataSize = getDataSize();
        if (startFrom > dataSize) {
            throw new OutOfBoundsException("Inserted block must be inside or directly after existing data");
        }
        if (length > MAX_DATA_SIZE - getDataSize()) {
            throw new DataOverflowException("Maximum array size overflow");
        }

        if (length == 0) {
            return;
        }

        long copyLength = dataSize - startFrom;
        dataSize = dataSize + length;
        setDataSize(dataSize);
        long sourceEnd = dataSize - length;
        long targetEnd = dataSize;
        // Backward copy
        while (copyLength > 0) {
            BufferData sourcePage;
            int sourceOffset = (int) (sourceEnd % pageSize);
            if (sourceOffset == 0) {
                sourcePage = getPage((int) ((sourceEnd - 1) / pageSize));
                sourceOffset = (int) sourcePage.getDataSize();
            } else {
                sourcePage = getPage((int) (sourceEnd / pageSize));
            }

            BufferData targetPage;
            int targetOffset = (int) (targetEnd % pageSize);
            if (targetOffset == 0) {
                targetPage = getPage((int) ((targetEnd - 1) / pageSize));
                targetOffset = (int) targetPage.getDataSize();
            } else {
                targetPage = getPage((int) (targetEnd / pageSize));
            }

            int copySize = Math.min(sourceOffset, targetOffset);
            if (copySize > copyLength) {
                copySize = (int) copyLength;
            }

            BufferPagedData.put(targetPage.getData(), targetOffset - copySize, sourcePage.getData(), sourceOffset - copySize, copySize);
            copyLength -= copySize;
            sourceEnd -= copySize;
            targetEnd -= copySize;
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        insertUninitialized(startFrom, length);
        fillData(startFrom, length);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        long length = insertedData.getDataSize();
        insertUninitialized(startFrom, length);
        replace(startFrom, insertedData, 0, length);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        insertUninitialized(startFrom, insertedDataLength);
        replace(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        insert(startFrom, insertedData, 0, insertedData.length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        if (insertedDataLength <= 0) {
            return;
        }

        insertUninitialized(startFrom, insertedDataLength);

        while (insertedDataLength > 0) {
            BufferData targetPage = getPage((int) (startFrom / pageSize));
            int targetOffset = (int) (startFrom % pageSize);
            int blockLength = pageSize - targetOffset;
            if (blockLength > insertedDataLength) {
                blockLength = insertedDataLength;
            }

            try {
                targetPage.getData().position(targetOffset);
                targetPage.getData().put(insertedData, insertedDataOffset, blockLength);
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            insertedDataOffset += blockLength;
            insertedDataLength -= blockLength;
            startFrom += blockLength;
        }
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long maximumDataSize) throws IOException {
        if (maximumDataSize > MAX_DATA_SIZE - getDataSize()) {
            throw new DataOverflowException("Maximum array size overflow");
        }

        if (startFrom > getDataSize()) {
            setDataSize(startFrom);
        }

        long loadedData = 0;
        int pageOffset = (int) (startFrom % pageSize);
        byte[] buffer = new byte[pageSize];
        while (maximumDataSize == -1 || maximumDataSize > 0) {
            int dataToRead = pageSize - pageOffset;
            if (maximumDataSize >= 0 && maximumDataSize < dataToRead) {
                dataToRead = (int) maximumDataSize;
            }
            if (pageOffset > 0 && dataToRead > pageOffset) {
                // Align to data pages
                dataToRead = pageOffset;
            }

            int readLength = 0;
            while (dataToRead > 0) {
                int read = inputStream.read(buffer, readLength, dataToRead);
                if (read == -1) {
                    break;
                }

                readLength += read;
                dataToRead -= read;
            }

            insert(startFrom, buffer, 0, readLength);
            startFrom += readLength;
            if (maximumDataSize >= 0) {
                maximumDataSize -= readLength;
            }
            loadedData += readLength;
            pageOffset = 0;
        }
        return loadedData;
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of filled block must be non-negative");
        }
        if (startFrom < 0) {
            throw new IllegalArgumentException("Position of filler block must be non-negative");
        }
        if (startFrom + length > getDataSize()) {
            throw new OutOfBoundsException("Filled block must be inside existing data");
        }

        while (length > 0) {
            BufferData page = getPage((int) (startFrom / pageSize));
            int pageOffset = (int) (startFrom % pageSize);
            int fillSize = (int) (page.getDataSize() - pageOffset);
            if (fillSize > length) {
                fillSize = (int) length;
            }
            ByteBuffer pageBuffer = page.getData();
            for (int i = pageOffset; i < pageOffset + fillSize; i++) {
                pageBuffer.put(i, fill);

            }
            length -= fillSize;
            startFrom += fillSize;
        }
    }

    @Nonnull
    @Override
    public BufferPagedData copy() {
        BufferPagedData targetData = new BufferPagedData();
        targetData.insert(0, this);
        return targetData;
    }

    @Nonnull
    @Override
    public BufferPagedData copy(long startFrom, long length) {
        BufferPagedData targetData = new BufferPagedData();
        targetData.insertUninitialized(0, length);
        targetData.replace(0, this, startFrom, length);
        return targetData;
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        while (length > 0) {
            BufferData page = getPage((int) (startFrom / pageSize));
            int pageOffset = (int) (startFrom % pageSize);
            int copySize = pageSize - pageOffset;
            if (copySize > length) {
                copySize = length;
            }

            page.copyToArray(pageOffset, target, offset, copySize);

            length -= copySize;
            offset += copySize;
            startFrom += copySize;
        }
    }

    @Override
    public void remove(long startFrom, long length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of removed block must be non-negative");
        }
        if (startFrom < 0) {
            throw new IllegalArgumentException("Position of removed block must be non-negative");
        }
        if (startFrom + length > getDataSize()) {
            throw new OutOfBoundsException("Removed block must be inside existing data");
        }

        if (length > 0) {
            long dataSize = getDataSize();
            replace(startFrom, this, startFrom + length, dataSize - startFrom - length);
            setDataSize(dataSize - length);
        }
    }

    @Override
    public void clear() {
        data.clear();
    }

    /**
     * Returns number of pages currently used.
     *
     * @return count of pages
     */
    @Override
    public int getPagesCount() {
        return data.size();
    }

    /**
     * Returns currently used page size.
     *
     * @return page size in bytes
     */
    @Override
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Gets data page allowing direct access to it.
     *
     * @param pageIndex page index
     * @return data page
     */
    @Nonnull
    @Override
    public BufferData getPage(int pageIndex) {
        try {
            return data.get(pageIndex);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    /**
     * Sets data page replacing existing page by reference.
     *
     * @param pageIndex page index
     * @param dataPage data page
     */
    @Override
    public void setPage(int pageIndex, BinaryData dataPage) {
        if (!(dataPage instanceof BufferData)) {
            throw new UnsupportedOperationException("Unsupported data page type");
        }
        try {
            data.set(pageIndex, (BufferData) dataPage);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.getDataSize());
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }

        if (replacingData instanceof BufferPagedData) {
            if (replacingData != this || (startFrom > targetPosition) || (startFrom + length < targetPosition)) {
                while (length > 0) {
                    BufferData page = getPage((int) (targetPosition / pageSize));
                    int offset = (int) (targetPosition % pageSize);

                    BufferData sourcePage = ((BufferPagedData) replacingData).getPage((int) (startFrom / ((BufferPagedData) replacingData).getPageSize()));
                    int sourceOffset = (int) (startFrom % ((BufferPagedData) replacingData).getPageSize());

                    int copySize = pageSize - offset;
                    if (copySize > ((BufferPagedData) replacingData).getPageSize() - sourceOffset) {
                        copySize = ((BufferPagedData) replacingData).getPageSize() - sourceOffset;
                    }
                    if (copySize > length) {
                        copySize = (int) length;
                    }

                    try {
                        BufferPagedData.put(page.getData(), offset, sourcePage.getData(), sourceOffset, copySize);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new OutOfBoundsException(ex);
                    }
                    length -= copySize;
                    targetPosition += copySize;
                    startFrom += copySize;
                }
            } else {
                targetPosition += length - 1;
                startFrom += length - 1;
                while (length > 0) {
                    BufferData page = getPage((int) (targetPosition / pageSize));
                    int upTo = (int) (targetPosition % pageSize) + 1;

                    BufferData sourcePage = ((BufferPagedData) replacingData).getPage((int) (startFrom / ((BufferPagedData) replacingData).getPageSize()));
                    int sourceUpTo = (int) (startFrom % ((BufferPagedData) replacingData).getPageSize()) + 1;

                    int copySize = upTo;
                    if (copySize > sourceUpTo) {
                        copySize = sourceUpTo;
                    }
                    if (copySize > length) {
                        copySize = (int) length;
                    }
                    int offset = upTo - copySize;
                    int sourceOffset = sourceUpTo - copySize;

                    BufferPagedData.put(page.getData(), offset, sourcePage.getData(), sourceOffset, copySize);
                    length -= copySize;
                    targetPosition -= copySize;
                    startFrom -= copySize;
                }
            }
        } else {
            while (length > 0) {
                BufferData page = getPage((int) (targetPosition / pageSize));
                int offset = (int) (targetPosition % pageSize);

                int copySize = pageSize - offset;
                if (copySize > length) {
                    copySize = (int) length;
                }

                byte[] buffer = new byte[copySize];
                replacingData.copyToArray(startFrom, buffer, 0, copySize);
                page.getData().position(offset);
                page.getData().put(buffer);

                length -= copySize;
                targetPosition += copySize;
                startFrom += copySize;
            }
        }
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }

        while (length > 0) {
            BufferData page = getPage((int) (targetPosition / pageSize));
            int offset = (int) (targetPosition % pageSize);

            int copySize = pageSize - offset;
            if (copySize > length) {
                copySize = length;
            }

            try {
                page.getData().position(offset);
                page.getData().put(replacingData, replacingDataOffset, copySize);
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }

            length -= copySize;
            targetPosition += copySize;
            replacingDataOffset += copySize;
        }
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        data.clear();
        byte[] buffer = new byte[pageSize];
        int cnt;
        int offset = 0;
        while ((cnt = inputStream.read(buffer, offset, buffer.length - offset)) > 0) {
            if (cnt + offset < pageSize) {
                offset = offset + cnt;
            } else {
                data.add(createNewPage(buffer));
                buffer = new byte[pageSize];
                offset = 0;
            }
        }

        if (offset > 0) {
            byte[] tail = new byte[offset];
            System.arraycopy(buffer, 0, tail, 0, offset);
            data.add(createNewPage(tail));
        }
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        for (BufferData dataPage : data) {
            dataPage.saveToStream(outputStream);
        }
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        return new BinaryDataOutputStream(this);
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        return new BinaryDataInputStream(this);
    }

    @Nonnull
    protected BufferData createNewPage(byte[] pageData) {
        if (dataPageCreator != null) {
            EditableBinaryData page = dataPageCreator.createPage(pageData.length);
            page.replace(0, pageData);
            return (BufferData) page;
        }

        return new BufferData(pageData);
    }

    @Nonnull
    protected BufferData createNewPage(int pageDataSize) {
        if (dataPageCreator != null) {
            return (BufferData) dataPageCreator.createPage(pageDataSize);
        }

        return new BufferData(pageDataSize);
    }

    @Nullable
    public DataPageCreator getDataPageCreator() {
        return dataPageCreator;
    }

    public void setDataPageCreator(@Nullable DataPageCreator dataPageCreator) {
        this.dataPageCreator = dataPageCreator;
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
            if (obj instanceof BinaryData) {
                BinaryData other = (BinaryData) obj;
                long dataSize = getDataSize();
                if (other.getDataSize() != dataSize) {
                    return false;
                }

                int pageIndex = 0;
                int bufferSize = dataSize > pageSize ? pageSize : (int) dataSize;
                byte[] buffer = new byte[bufferSize];
                int offset = 0;
                int remain = (int) dataSize;
                while (remain > 0) {
                    int length = remain > bufferSize ? bufferSize : remain;
                    other.copyToArray(offset, buffer, 0, length);

                    BufferData pageData = data.get(pageIndex);
                    for (int i = 0; i < length; i++) {
                        if (pageData.getByte(i) != buffer[i]) {
                            return false;
                        }
                    }

                    offset += length;
                    remain -= length;
                    pageIndex++;
                }

                return true;
            }

            return false;
        }

        final BufferPagedData other = (BufferPagedData) obj;
        long dataSize = getDataSize();
        if (other.getDataSize() != dataSize) {
            return false;
        }

        int pageIndex = 0;
        int otherPageIndex = 0;
        long offset = 0;
        long remain = dataSize;
        while (remain > 0) {
            int pageOffset = (int) (offset % pageSize);
            int otherPageOffset = (int) (offset % other.pageSize);

            int length = remain > pageSize - pageOffset ? pageSize - pageOffset : (int) remain;
            if (length > other.pageSize - otherPageOffset) {
                length = other.pageSize - otherPageOffset;
            }

            BufferData pageData = data.get(pageIndex);
            BufferData otherPageData = other.data.get(otherPageIndex);
            int pageTestPos = pageOffset;
            int otherPageTestPos = otherPageOffset;
            for (int i = 0; i < length; i++) {
                if (pageData.getByte(pageTestPos) != otherPageData.getByte(otherPageTestPos)) {
                    return false;
                }
                pageTestPos++;
                otherPageTestPos++;
            }

            offset += length;
            remain -= length;
            pageIndex++;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDataSize());
    }

    @Override
    public void dispose() {
    }

    private static void put(ByteBuffer target, int position, ByteBuffer source, int offset, int length) throws IndexOutOfBoundsException {
        BufferEditableData.put(target, position, source, offset, length);
    }
}
