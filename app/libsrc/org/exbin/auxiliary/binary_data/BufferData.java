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
package org.exbin.auxiliary.binary_data;

import com.sun.jna.Memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Implementation of binary data interface using byte buffer.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BufferData implements BinaryData {

    private static final int BUFFER_SIZE = 4096;
    private BufferAllocationType bufferAllocationType = BufferAllocationType.DIRECT;

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
    public byte getByte(long position) {
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
        data.rewind();
        copy.put(data);
        return new BufferData(copy);
    }

    @Nonnull
    @Override
    public BufferData copy(long startFrom, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new OutOfBoundsException("Buffer data is limited by integer length");
        }
        if (startFrom + length > data.capacity()) {
            throw new OutOfBoundsException("Attemt to copy outside of data");
        }

        ByteBuffer copy = allocateBuffer((int) length);
        data.position((int) startFrom);
        data.limit((int) (startFrom + length));
        copy.put(data);
        data.limit(data.capacity());
        return new BufferData(copy);
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
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
            data.position(position);
            data.get(buffer, 0, length);
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
    public int hashCode() {
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
        other.data.rewind();
        data.rewind();
        return other.data.compareTo(data) == 0;
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
    public BufferAllocationType getBufferAllocationType() {
        return bufferAllocationType;
    }

    public void setBufferAllocationType(BufferAllocationType bufferAllocationType) {
        this.bufferAllocationType = bufferAllocationType;
    }

    @Nonnull
    protected ByteBuffer allocateBuffer(int capacity) {
        switch (bufferAllocationType) {
            case HEAP:
                return ByteBuffer.allocate(capacity);
            case DIRECT:
                if (capacity == 0) {
                    return ByteBuffer.allocateDirect(capacity);
                }

                // TODO Check for memory limit:
                // https://stackoverflow.com/questions/2298208/how-do-i-discover-memory-usage-of-my-application-in-android
                // possibly? long nativeHeapFreeSize = Debug.getNativeHeapFreeSize();
                try {
                    return new Memory(capacity).getByteBuffer(0, capacity);
                } catch (Throwable tw) {
                    // Fallback to regular byte buffer
                    return ByteBuffer.allocateDirect(capacity);
                }
                /* long lPtr = Native.malloc(capacity);
                if (lPtr == 0)
                    throw new Error("Failed to allocate direct byte buffer memory");
                return Memory.getByteBuffer(lPtr, capacity);

                buffer.clear();
                Pointer javaPointer = Native.getDirectBufferPointer(buffer);
                long lPtr = Pointer.nativeValue(javaPointer);
                Native.free(lPtr); */
            default:
                throw new IllegalStateException();
        }
    }
}