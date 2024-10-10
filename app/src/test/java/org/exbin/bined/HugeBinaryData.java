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
package org.exbin.bined;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayData;

/**
 * Simulation of huge binary data source.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class HugeBinaryData implements BinaryData {

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getDataSize() {
        return Long.MAX_VALUE / 2;
    }

    @Override
    public byte getByte(long longValue) {
        return (byte) (longValue % 0xff);
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        return new HugeBinaryData();
    }

    @Nonnull
    @Override
    public BinaryData copy(long startFrom, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Unable to copy too huge memory segment");
        }
        byte[] dataArray = new byte[(int) length];
        copyToArray(startFrom, dataArray, 0, (int) length);
        return new ByteArrayData(dataArray);
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        for (int position = 0; position < length; position++) {
            target[offset + position] = getByte(startFrom + position);
        }
    }

    @Override
    public void saveToStream(OutputStream out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispose() {
    }
}
