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
package org.exbin.auxiliary.binary_data;

import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Output stream for binary data.
 */
@ParametersAreNonnullByDefault
public class BinaryDataRengeOutputStream extends OutputStream implements SeekableStream, FinishableStream {

    @Nonnull
    protected final EditableBinaryData data;
    protected final long startPosition;
    protected final long length;
    protected long position = 0;

    public BinaryDataRengeOutputStream(EditableBinaryData data) {
        this.data = data;
        this.startPosition = 0;
        this.length = data.getDataSize();
    }

    public BinaryDataRengeOutputStream(EditableBinaryData data, DataRange dataRange) {
        this(data, dataRange.startPosition, dataRange.getLength());
    }

    public BinaryDataRengeOutputStream(EditableBinaryData data, long startPosition, long length) {
        if (startPosition < 0) {
            throw new IllegalArgumentException("Negative position not allowed");
        }
        if (length < 0) {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        long sourceDataSize = data.getDataSize();
        if (startPosition + length > sourceDataSize) {
            throw new OutOfBoundsException("Target area is outside of available data");
        }

        this.data = data;
        this.startPosition = startPosition;
        this.position = startPosition;
        this.length = length;
    }

    @Override
    public void write(int value) throws IOException {
        if (position >= startPosition + length) {
            throw new OutOfBoundsException("Position is outside of available range");
        }
        data.setByte(position++, (byte) value);
    }

    @Override
    public void write(byte[] input, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }

        if (position + len > startPosition + length) {
            throw new OutOfBoundsException("Target area is outside of available data");
        }

        data.replace(position, input, off, len);
        position += len;
    }

    @Override
    public void seek(long position) throws IOException {
        if (position < 0 || position > length) {
            throw new OutOfBoundsException("Position is outside of available range");
        }

        this.position = startPosition + position;
    }

    @Override
    public long getStreamSize() {
        return length;
    }

    @Override
    public long getProcessedSize() {
        return position - startPosition;
    }

    @Override
    public void close() throws IOException {
        finish();
    }

    @Override
    public long finish() throws IOException {
        position = startPosition + length;
        return length;
    }
}
