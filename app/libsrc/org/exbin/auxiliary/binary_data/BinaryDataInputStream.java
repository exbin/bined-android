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

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Input stream for binary data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryDataInputStream extends InputStream implements SeekableStream, FinishableStream {

    @Nonnull
    protected final BinaryData data;
    protected long position = 0;
    protected long mark = 0;

    public BinaryDataInputStream(BinaryData data) {
        this.data = data;
    }

    @Override
    public int read() throws IOException {
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

        long dataSize = data.getDataSize();
        if (position > dataSize - len) {
            if (position >= dataSize) {
                return -1;
            }
            len = (int) (dataSize - position);
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
        return (int) (data.getDataSize() - position);
    }

    @Override
    public void seek(long position) throws IOException {
        this.position = position;
    }

    @Override
    public long finish() throws IOException {
        position = data.getDataSize();
        return position;
    }

    @Override
    public long getProcessedSize() {
        return position;
    }

    @Override
    public long getStreamSize() {
        return data.getDataSize();
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
        long dataSize = data.getDataSize();
        if (position + n < dataSize) {
            position += n;
            return n;
        }

        long skipped = dataSize - position;
        position = dataSize;
        return skipped;
    }
}
