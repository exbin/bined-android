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

import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Implementation of editable binary data interface using direct byte buffer.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DirectBufferEditableData extends BufferEditableData {

    public DirectBufferEditableData() {
        this((ByteBuffer) null);
    }

    /**
     * Creates instance directly wrapping provided byte buffer.
     *
     * @param data byte buffer
     */
    public DirectBufferEditableData(@Nullable ByteBuffer data) {
        super(data != null ? data : DirectBufferEditableData.allocateBufferInt(0));
    }

    /**
     * Creates instance setting value to provided byte array.
     *
     * @param data byte array
     */
    public DirectBufferEditableData(@Nullable byte[] data) {
        super(DirectBufferEditableData.allocateBufferInt(data));
    }

    /**
     * Creates instance with specified size.
     *
     * @param dataSize data size
     */
    public DirectBufferEditableData(int dataSize) {
        super(DirectBufferEditableData.allocateBufferInt(dataSize));
    }

    @Nonnull
    @Override
    protected ByteBuffer allocateBuffer(int capacity) {
        return DirectBufferEditableData.allocateBufferInt(capacity);
    }

    @Nonnull
    private static ByteBuffer allocateBufferInt(int capacity) {
        return ByteBuffer.allocateDirect(capacity);
    }

    @Nonnull
    private static ByteBuffer allocateBufferInt(@Nullable byte[] data) {
        if (data == null) {
            return DirectBufferEditableData.allocateBufferInt(0);
        } else {
            ByteBuffer buffer = DirectBufferEditableData.allocateBufferInt(data.length);
            buffer.put(data);
            buffer.clear();
            return buffer;
        }
    }
}
