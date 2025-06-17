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
package org.exbin.auxiliary.binary_data.buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.BinaryDataOutputStream;
import org.exbin.auxiliary.binary_data.DataOverflowException;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;

/**
 * Implementation of editable binary data interface using byte buffer.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BufferEditableData extends BufferData implements EditableBinaryData {

    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 5;

    private static final String WRONG_INSERTION_POSITION_ERROR = "Data can be inserted only inside or at the end";
    private static final String WRONG_REPLACE_POSITION_ERROR = "Data can be replaced only inside or at the end";
    private static final String ARRAY_OVERFLOW_ERROR = "Maximum array size overflow";

    public BufferEditableData() {
        this((ByteBuffer) null);
    }

    public BufferEditableData(int dataSize) {
        super(dataSize);
    }

    public BufferEditableData(@Nullable ByteBuffer data) {
        super(data);
    }

    public BufferEditableData(@Nullable byte[] data) {
        super(data);
    }

    @Override
    public void setDataSize(long size) {
        if (size < 0) {
            throw new InvalidParameterException("Size cannot be negative");
        }

        int oldSize = data.capacity();
        if (oldSize != size) {
            ByteBuffer newData = allocateBuffer((int) size);
            if (size < oldSize) {
                if (size > 0) {
                    data.rewind();
                    data.limit((int) size);
                    newData.put(data);
                    data.limit(oldSize);
                }
                data = newData;
            } else {
                if (oldSize > 0) {
                    data.rewind();
                    newData.put(data);
                }
                data = newData;
            }
        }
    }

    @Override
    public void setByte(long position, byte value) {
        try {
            data.put((int) position, value);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        if (startFrom > data.capacity()) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (length > MAX_ARRAY_LENGTH - data.capacity()) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (length > 0) {
            ByteBuffer newData = allocateBuffer((int) (data.capacity() + length));
            BufferEditableData.put(newData, 0, data, 0, (int) startFrom);
            BufferEditableData.put(newData, (int) (startFrom + length), data, (int) startFrom, (int) (data.capacity() - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        insertUninitialized(startFrom, length);
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        if (startFrom > data.capacity()) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (insertedData.length > MAX_ARRAY_LENGTH - data.capacity()) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        int length = insertedData.length;
        if (length > 0) {
            ByteBuffer newData = allocateBuffer((int) (data.capacity() + length));
            BufferEditableData.put(newData, 0, data, 0, (int) startFrom);
            try {
                newData.position((int) startFrom);
                newData.put(insertedData);
                BufferEditableData.put(newData, (int) (startFrom + length), data, (int) startFrom, (int) (data.capacity() - startFrom));
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int length) {
        if (startFrom > data.capacity()) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (length > MAX_ARRAY_LENGTH - data.capacity()) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (length > 0) {
            ByteBuffer newData = allocateBuffer((int) (data.capacity() + length));
            BufferEditableData.put(newData, 0, data, 0, (int) startFrom);
            try {
                newData.position((int) startFrom);
                newData.put(insertedData, insertedDataOffset, length);
                BufferEditableData.put(newData, (int) (startFrom + length), data, (int) startFrom, (int) (data.capacity() - startFrom));
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        if (startFrom > data.capacity()) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (insertedData.getDataSize() > MAX_ARRAY_LENGTH - data.capacity()) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        insert(startFrom, insertedData, 0, insertedData.getDataSize());
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        if (startFrom > data.capacity()) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (insertedDataLength > MAX_ARRAY_LENGTH - data.capacity()) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        long length = insertedDataLength;
        if (length > 0) {
            ByteBuffer newData = allocateBuffer((int) (data.capacity() + length));
            BufferEditableData.put(newData, 0, data, 0, (int) startFrom);
            for (int i = 0; i < length; i++) {
                newData.position((int) startFrom + i);
                newData.put(insertedData.getByte(insertedDataOffset + i));
            }
            BufferEditableData.put(newData, (int) (startFrom + length), data, (int) startFrom, (int) (data.capacity() - startFrom));
            data = newData;
        }
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long maximumDataSize) throws IOException {
        if (maximumDataSize > MAX_ARRAY_LENGTH - data.capacity()) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (maximumDataSize == -1 || maximumDataSize > 0) {
                int toRead = buffer.length;
                if (maximumDataSize >= 0 && toRead > maximumDataSize) {
                    toRead = (int) maximumDataSize;
                }
                int read = inputStream.read(buffer, 0, toRead);
                if (read == -1) {
                    break;
                }
                
                if (read > 0) {
                    output.write(buffer, 0, read);
                    if (maximumDataSize >= 0) {
                        maximumDataSize -= read;
                    }
                }
            }
            byte[] newData = output.toByteArray();
            if (startFrom + newData.length > getDataSize()) {
                setDataSize(startFrom + newData.length);
            }
            replace(startFrom, newData);
            return newData.length;
        }
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        if (length > 0) {
            try {
                for (int i = (int) startFrom; i < startFrom + length; i++) {
                    data.put(i, fill);
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
        }
    }

    @Override
    public void replace(long targetPosition, BinaryData sourceData) {
        replace(targetPosition, sourceData, 0, sourceData.getDataSize());
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long replacingLength) {
        if (targetPosition + replacingLength > getDataSize()) {
            throw new OutOfBoundsException(WRONG_REPLACE_POSITION_ERROR);
        }

        while (replacingLength > 0) {
            setByte(targetPosition, replacingData.getByte(startFrom));
            targetPosition++;
            startFrom++;
            replacingLength--;
        }
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException(WRONG_REPLACE_POSITION_ERROR);
        }

        try {
            data.position((int) targetPosition);
            data.put(replacingData, replacingDataOffset, length);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void remove(long startFrom, long length) {
        if (startFrom + length > data.capacity()) {
            throw new OutOfBoundsException("Cannot remove from " + startFrom + " with length " + length);
        }

        if (length > 0) {
            ByteBuffer newData = allocateBuffer((int) (data.capacity() - length));
            BufferEditableData.put(newData, 0, data, 0, (int) startFrom);
            BufferEditableData.put(newData, (int) startFrom, data, (int) (startFrom + length), (int) (data.capacity() - startFrom - length));
            data = newData;
        }
    }

    @Nonnull
    @Override
    public BufferEditableData copy() {
        ByteBuffer copy = allocateBuffer(data.capacity());
        synchronized (this) {
            data.rewind();
            copy.put(data);
        }
        return new BufferEditableData(copy);
    }

    @Nonnull
    @Override
    public BufferEditableData copy(long startFrom, long length) {
        if (startFrom + length > data.capacity()) {
            throw new OutOfBoundsException("Attempt to copy outside of data");
        }

        ByteBuffer copy = allocateBuffer((int) length);
        synchronized (this) {
            BufferEditableData.put(copy, 0, data, (int) startFrom, (int) length);
            return new BufferEditableData(copy);
        }
    }

    @Override
    public void clear() {
        data = allocateBuffer(0);
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            do {
                read = inputStream.read(buffer);
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            } while (read > 0);
            byte[] byteArray = output.toByteArray();
            data = allocateBuffer(byteArray.length);
            data.put(byteArray);
        }
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        return new BinaryDataOutputStream(this);
    }

    public static void put(ByteBuffer target, int position, ByteBuffer source, int offset, int length) throws IndexOutOfBoundsException {
        if (target == source) {
            byte[] buffer = new byte[BUFFER_SIZE];
            if (position < offset) {
                while (length > 0) {
                    int blockSize = length > BUFFER_SIZE ? BUFFER_SIZE : length;
                    source.position(offset);
                    source.get(buffer, 0, blockSize);
                    target.position(position);
                    target.put(buffer, 0, blockSize);
                    offset += blockSize;
                    position += blockSize;
                    length -= blockSize;
                }
            } else if (position > offset) {
                while (length > 0) {
                    int blockSize = length > BUFFER_SIZE ? BUFFER_SIZE : length;
                    source.position(offset + length - blockSize);
                    source.get(buffer, 0, blockSize);
                    target.position(position + length - blockSize);
                    target.put(buffer, 0, blockSize);
                    length -= blockSize;
                }
            }
            target.clear();
            return;
        }

        source.position(offset);
        source.limit(offset + length);
        target.position(position);
        target.put(source);
        target.clear();
        source.clear();
    }
}
