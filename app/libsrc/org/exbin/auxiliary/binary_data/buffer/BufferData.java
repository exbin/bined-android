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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.BinaryDataInputStream;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;

/**
 * Implementation of binary data interface using byte buffer.
 * <p>
 * To allow parallel reading, read operations must be synchronized due to split
 * nature of the ByteBuffer position and depending operation.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BufferData implements BinaryData {

    protected static final int BUFFER_SIZE = 4096;

    @Nonnull
    protected ByteBuffer data;

    public BufferData() {
        this((ByteBuffer) null);
    }

    /**
     * Creates instance directly wrapping provided byte buffer.
     *
     * @param data byte buffer
     */
    public BufferData(@Nullable ByteBuffer data) {
        this.data = data != null ? data : BufferData.this.allocateBuffer(0);
    }

    /**
     * Creates instance setting value to provided byte array.
     *
     * @param data byte array
     */
    public BufferData(@Nullable byte[] data) {
        if (data == null) {
            this.data = BufferData.this.allocateBuffer(0);
        } else {
            this.data = BufferData.this.allocateBuffer(data.length);
            this.data.put(data);
            this.data.clear();
        }
    }

    /**
     * Creates instance with specified size.
     *
     * @param dataSize data size
     */
    public BufferData(int dataSize) {
        this.data = BufferData.this.allocateBuffer(dataSize);
    }

    /**
     * Returns internal data.
     *
     * @return byte array
     */
    @Nonnull
    public ByteBuffer getData() {
        return data;
    }

    @Override
    public boolean isEmpty() {
        return data.capacity() == 0;
    }

    @Override
    public long getDataSize() {
        return data.capacity();
    }

    @Override
    public synchronized byte getByte(long position) {
        try {
            return data.get((int) position);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Nonnull
    @Override
    public BufferData copy() {
        ByteBuffer copy = allocateBuffer(data.capacity());
        synchronized (this) {
            data.rewind();
            copy.put(data);
        }
        return new BufferData(copy);
    }

    @Nonnull
    @Override
    public BufferData copy(long startFrom, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new OutOfBoundsException("Buffer data is limited by integer length");
        }
        if (startFrom + length > data.capacity()) {
            throw new OutOfBoundsException("Attempt to copy outside of data");
        }

        ByteBuffer copy = allocateBuffer((int) length);
        synchronized (this) {
            data.position((int) startFrom);
            data.limit((int) (startFrom + length));
            copy.put(data);
            data.clear();
            return new BufferData(copy);
        }
    }

    @Override
    public synchronized void copyToArray(long startFrom, byte[] target, int offset, int length) {
        try {
            data.position((int) startFrom);
            data.get(target, offset, length);
        } catch (IndexOutOfBoundsException | BufferUnderflowException | IllegalArgumentException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int remaining = data.capacity();
        int position = 0;
        while (remaining > 0) {
            int length = remaining > BUFFER_SIZE ? BUFFER_SIZE : remaining;
            synchronized (this) {
                data.position(position);
                data.get(buffer, 0, length);
            }
            outputStream.write(buffer, 0, length);
            position += length;
            remaining -= length;
        }
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        return new BinaryDataInputStream(this);
    }

    @Override
    public synchronized int hashCode() {
        return data.hashCode();
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
                return compareTo(other);
            }

            return false;
        }

        final BufferData other = (BufferData) obj;
        synchronized (this) {
            other.data.rewind();
            data.rewind();
            return other.data.compareTo(data) == 0;
        }
    }

    public boolean compareTo(BinaryData other) {
        long dataSize = getDataSize();
        if (other.getDataSize() != dataSize) {
            return false;
        }

        int bufferSize = dataSize > BUFFER_SIZE ? BUFFER_SIZE : (int) dataSize;
        byte[] buffer = new byte[bufferSize];
        int offset = 0;
        int remain = (int) dataSize;
        while (remain > 0) {
            int length = remain > bufferSize ? bufferSize : remain;
            other.copyToArray(offset, buffer, 0, length);
            for (int i = 0; i < length; i++) {
                if (data.get(offset + i) != buffer[i]) {
                    return false;
                }
            }

            offset += length;
            remain -= length;
        }

        return true;
    }

    @Override
    public void dispose() {
    }

    @Nonnull
    protected ByteBuffer allocateBuffer(int capacity) {
        return ByteBuffer.allocate(capacity);
    }
}
