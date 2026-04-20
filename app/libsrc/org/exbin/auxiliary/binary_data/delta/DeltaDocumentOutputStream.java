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
package org.exbin.auxiliary.binary_data.delta;

import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;
import org.exbin.auxiliary.binary_data.SeekableStream;

/**
 * Delta document output stream.
 * <p>
 * Data are expanded as needed.
 */
@ParametersAreNonnullByDefault
public class DeltaDocumentOutputStream extends OutputStream implements SeekableStream {

    @Nonnull
    private final DeltaDocumentWindow data;
    private long position = 0;

    public DeltaDocumentOutputStream(DeltaDocument document) {
        this.data = new DeltaDocumentWindow(document);
    }

    @Override
    public void write(int value) throws IOException {
        long dataSize = data.getDataSize();
        if (position == dataSize) {
            dataSize++;
            data.setDataSize(dataSize);
        }

        data.setByte(position++, (byte) value);
    }

    @Override
    public void write(byte[] input, int offset, int length) throws IOException {
        if (length == 0) {
            return;
        }

        data.insert(position, input, offset, length);
        position += length;
    }

    @Override
    public void seek(long position) throws IOException {
        if (position < 0) {
            throw new OutOfBoundsException("Position is outside of available range");
        }

        if (position > data.getDataSize()) {
            data.setDataSize(position);
        }

        this.position = position;
    }

    @Override
    public long getStreamSize() {
        return -1;
    }

    public long getProcessedSize() {
        return position;
    }

    @Override
    public void close() throws IOException {
        position = data.getDataSize();
    }
}
