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
package org.exbin.auxiliary.binary_data.jna;

import com.sun.jna.Memory;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.buffer.BufferData;

/**
 * Implementation of binary data interface using JNA byte buffer.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class JnaBufferData extends BufferData {

    public JnaBufferData() {
        this((ByteBuffer) null);
    }

    /**
     * Creates instance directly wrapping provided byte buffer.
     *
     * @param data byte buffer
     */
    public JnaBufferData(@Nullable ByteBuffer data) {
        super(data != null ? data : JnaBufferData.allocateBufferInt(0));
    }

    /**
     * Creates instance setting value to provided byte array.
     *
     * @param data byte array
     */
    public JnaBufferData(@Nullable byte[] data) {
        super(JnaBufferData.allocateBufferInt(data));
    }

    /**
     * Creates instance with specified size.
     *
     * @param dataSize data size
     */
    public JnaBufferData(int dataSize) {
        super(JnaBufferData.allocateBufferInt(dataSize));
    }

    @Nonnull
    @Override
    protected ByteBuffer allocateBuffer(int capacity) {
        return JnaBufferData.allocateBufferInt(capacity);
    }

    @Nonnull
    private static ByteBuffer allocateBufferInt(int capacity) {
        try {
            return new Memory(capacity).getByteBuffer(0, capacity);
        } catch (Throwable tw) {
            // Fallback to regular byte buffer
            return ByteBuffer.allocateDirect(capacity);
        }
    }

    @Nonnull
    private static ByteBuffer allocateBufferInt(@Nullable byte[] data) {
        if (data == null) {
            return JnaBufferData.allocateBufferInt(0);
        } else {
            ByteBuffer buffer = JnaBufferData.allocateBufferInt(data.length);
            buffer.put(data);
            buffer.clear();
            return buffer;
        }
    }
}
