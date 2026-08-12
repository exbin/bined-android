/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.auxiliary.binary_data.android_jna;

import org.exbin.auxiliary.binary_data.buffer.BufferData;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Implementation of binary data interface using JNA byte buffer.
 */
@NullMarked
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
        super(data != null ? data : JnaBuffer.allocateBufferInt(0));
    }

    /**
     * Creates instance setting value to provided byte array.
     *
     * @param data byte array
     */
    public JnaBufferData(byte @Nullable [] data) {
        super(JnaBuffer.allocateBufferInt(data));
    }

    /**
     * Creates instance with specified size.
     *
     * @param dataSize data size
     */
    public JnaBufferData(int dataSize) {
        super(JnaBuffer.allocateBufferInt(dataSize));
    }

    @Override
    protected ByteBuffer allocateBuffer(int capacity) {
        return JnaBuffer.allocateBufferInt(capacity);
    }
}
