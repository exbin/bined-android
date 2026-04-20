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
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Input stream for binary data range.
 */
@ParametersAreNonnullByDefault
public class BinaryDataRangeInputStream extends InputStream implements SeekableStream, FinishableStream {

    @Nonnull
    protected final BinaryData data;
    protected final long startPosition;
    protected final long length;
    protected long position = 0;
    protected long mark = 0;

    public BinaryDataRangeInputStream(BinaryData data) {
        this.data = data;
        this.startPosition = 0;
        this.position = 0;
        this.length = data.getDataSize();
    }

    public BinaryDataRangeInputStream(BinaryData data, DataRange dataRange) {
        this(data, dataRange.startPosition, dataRange.getLength());
    }

    public BinaryDataRangeInputStream(BinaryData data, long startPosition, long length) {
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
    public int read() throws IOException {
        if (position > startPosition + length) {
            return -1;
        }

        try {
            return data.getByte(position++) & 0xFF;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    @Override
    public int read(byte[] output, int off, int len) throws IOException {
        if (output.length == 0 || len == 0) {
            return 0;
        }

        if (position > startPosition + length - len) {
            if (position >= startPosition + length) {
                return -1;
            }
            len = (int) (startPosition + length - position);
        }

        data.copyToArray(position, output, off, len);
        position += len;
        return len;
    }

    @Override
    public void close() throws IOException {
        finish();
    }

    @Override
    public int available() throws IOException {
        return (int) ((startPosition + length) - position);
    }

    @Override
    public void seek(long position) throws IOException {
        if (position < 0 || position > length) {
            throw new OutOfBoundsException("Position is outside of available range");
        }

        this.position = startPosition + position;
    }

    @Override
    public long finish() throws IOException {
        position = startPosition + length;
        return length;
    }

    @Override
    public long getProcessedSize() {
        return position - startPosition;
    }

    @Override
    public long getStreamSize() {
        return length;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {
        position = mark;
    }

    @Override
    public synchronized void mark(int readlimit) {
        mark = position;
    }

    @Override
    public long skip(long n) throws IOException {
        if (position - startPosition + n < length) {
            position += n;
            return n;
        }

        long skipped = startPosition + length - position;
        position = startPosition + length;
        return skipped;
    }
}
