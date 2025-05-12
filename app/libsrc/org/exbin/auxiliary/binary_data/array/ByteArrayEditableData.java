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
package org.exbin.auxiliary.binary_data.array;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.BinaryDataOutputStream;
import org.exbin.auxiliary.binary_data.DataOverflowException;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.OutOfBoundsException;

/**
 * Implementation of editable binary data interface using byte array.
 * <p>
 * This implementation is highly inefficient with resources and is not
 * recommended for frequently changed data as each change creates copy of data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ByteArrayEditableData extends ByteArrayData implements EditableBinaryData {

    public static final int BUFFER_SIZE = 1024;
    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 5;

    private static final String WRONG_INSERTION_POSITION_ERROR = "Data can be inserted only inside or at the end";
    private static final String WRONG_REPLACE_POSITION_ERROR = "Data can be replaced only inside or at the end";
    private static final String ARRAY_OVERFLOW_ERROR = "Maximum array size overflow";

    public ByteArrayEditableData() {
        this(null);
    }

    public ByteArrayEditableData(@Nullable byte[] data) {
        super(data);
    }

    @Override
    public void setDataSize(long size) {
        if (size < 0) {
            throw new InvalidParameterException("Size cannot be negative");
        }

        if (data.length != size) {
            if (size < data.length) {
                data = Arrays.copyOfRange(data, 0, (int) size);
            } else {
                byte[] newData = new byte[(int) size];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
        }
    }

    @Override
    public void setByte(long position, byte value) {
        try {
            data[(int) position] = value;
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        if (startFrom > data.length) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (length > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (length > 0) {
            byte[] newData = new byte[(int) (data.length + length)];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        if (startFrom > data.length) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (length > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (length > 0) {
            byte[] newData = new byte[(int) (data.length + length)];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        if (startFrom > data.length) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (insertedData.length > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        int length = insertedData.length;
        if (length > 0) {
            byte[] newData = new byte[data.length + length];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            try {
                System.arraycopy(insertedData, 0, newData, (int) startFrom, length);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int length) {
        if (startFrom > data.length) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (length > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (length > 0) {
            byte[] newData = new byte[data.length + length];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            try {
                System.arraycopy(insertedData, insertedDataOffset, newData, (int) startFrom, length);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
            data = newData;
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        if (startFrom > data.length) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (insertedData.getDataSize() > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (insertedData instanceof ByteArrayData) {
            insert(startFrom, ((ByteArrayData) insertedData).data);
        } else {
            insert(startFrom, insertedData, 0, insertedData.getDataSize());
        }
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        if (startFrom > data.length) {
            throw new OutOfBoundsException(WRONG_INSERTION_POSITION_ERROR);
        }
        if (insertedDataLength > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        if (insertedData instanceof ByteArrayData) {
            if (insertedDataOffset > Integer.MAX_VALUE || insertedDataLength > Integer.MAX_VALUE) {
                throw new OutOfBoundsException("Out of range");
            }
            insert(startFrom, ((ByteArrayData) insertedData).data, (int) insertedDataOffset, (int) insertedDataLength);
        } else {
            long length = insertedDataLength;
            if (length > 0) {
                byte[] newData = new byte[(int) (data.length + length)];
                System.arraycopy(data, 0, newData, 0, (int) startFrom);
                for (int i = 0; i < length; i++) {
                    newData[(int) (startFrom + i)] = insertedData.getByte(insertedDataOffset + i);
                }
                System.arraycopy(data, (int) (startFrom), newData, (int) (startFrom + length), (int) (data.length - startFrom));
                data = newData;
            }
        }
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long dataSize) throws IOException {
        if (dataSize > MAX_ARRAY_LENGTH - data.length) {
            throw new DataOverflowException(ARRAY_OVERFLOW_ERROR);
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            if (dataSize > 0) {
                int read;
                do {
                    int toRead = buffer.length;
                    if (toRead > dataSize) {
                        toRead = (int) dataSize;
                    }
                    read = inputStream.read(buffer, 0, toRead);
                    if (read > 0) {
                        output.write(buffer, 0, read);
                        dataSize -= read;
                    }
                } while (read > 0 && dataSize > 0);
            }
            byte[] newData = output.toByteArray();
            if (startFrom + newData.length > getDataSize()) {
                setDataSize(startFrom + newData.length);
            }
            replace(startFrom, newData);
            return newData.length;
        }
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        if (length > 0) {
            try {
                Arrays.fill(data, (int) startFrom, (int) (startFrom + length), fill);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
        }
    }

    @Override
    public void replace(long targetPosition, BinaryData sourceData) {
        replace(targetPosition, sourceData, 0, sourceData.getDataSize());
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long replacingLength) {
        if (targetPosition + replacingLength > getDataSize()) {
            throw new OutOfBoundsException(WRONG_REPLACE_POSITION_ERROR);
        }

        if (replacingData instanceof ByteArrayData) {
            replace(targetPosition, ((ByteArrayData) replacingData).data, (int) startFrom, (int) replacingLength);
        } else {
            while (replacingLength > 0) {
                setByte(targetPosition, replacingData.getByte(startFrom));
                targetPosition++;
                startFrom++;
                replacingLength--;
            }
        }
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException(WRONG_REPLACE_POSITION_ERROR);
        }

        try {
            System.arraycopy(replacingData, replacingDataOffset, data, (int) targetPosition, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void remove(long startFrom, long length) {
        if (startFrom + length > data.length) {
            throw new OutOfBoundsException("Cannot remove from " + startFrom + " with length " + length);
        }
        if (length > 0) {
            byte[] newData = new byte[(int) (data.length - length)];
            System.arraycopy(data, 0, newData, 0, (int) startFrom);
            System.arraycopy(data, (int) (startFrom + length), newData, (int) startFrom, (int) (data.length - startFrom - length));
            data = newData;
        }
    }

    @Nonnull
    @Override
    public ByteArrayEditableData copy() {
        byte[] copy = Arrays.copyOf(data, data.length);
        return new ByteArrayEditableData(copy);
    }

    @Nonnull
    @Override
    public ByteArrayEditableData copy(long startFrom, long length) {
        if (startFrom + length > data.length) {
            throw new OutOfBoundsException("Attempt to copy outside of data");
        }

        byte[] copy = Arrays.copyOfRange(data, (int) startFrom, (int) (startFrom + length));
        return new ByteArrayEditableData(copy);
    }

    @Override
    public void clear() {
        data = new byte[0];
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            do {
                read = inputStream.read(buffer);
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            } while (read > 0);
            data = output.toByteArray();
        }
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        return new BinaryDataOutputStream(this);
    }
}
