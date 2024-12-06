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
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for editable binary data - sequence of bytes.
 * <p>
 * Provides methods to read/write whole or part of the data to array or stream.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface EditableBinaryData extends BinaryData {

    /**
     * Sets data size.
     * <p>
     * If size is bigger than current size, it will fill it with zeros,
     * otherwise it will shrink current data.
     *
     * @param size target size
     */
    void setDataSize(long size);

    /**
     * Sets byte to given position.
     *
     * @param position position
     * @param value byte value to be set
     */
    void setByte(long position, byte value);

    /**
     * Inserts data space of given length to given position without setting any
     * data to it.
     *
     * @param startFrom position to insert to
     * @param length length of data
     */
    void insertUninitialized(long startFrom, long length);

    /**
     * Inserts empty data of given length to given position.
     *
     * @param startFrom position to insert to
     * @param length length of data
     */
    void insert(long startFrom, long length);

    /**
     * Inserts given data to given position.
     *
     * @param startFrom position to insert to
     * @param insertedData data to insert
     */
    void insert(long startFrom, byte[] insertedData);

    /**
     * Inserts given data to given position.
     *
     * @param startFrom position to insert to
     * @param insertedData data to insert
     * @param insertedDataOffset inserted data offset
     * @param insertedDataLength inserted data length
     */
    void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength);

    /**
     * Inserts given data to given position.
     *
     * @param startFrom position to insert to
     * @param insertedData data to insert
     */
    void insert(long startFrom, BinaryData insertedData);

    /**
     * Inserts given data to given position.
     *
     * @param startFrom position to insert to
     * @param insertedData data to insert
     * @param insertedDataOffset inserted data offset
     * @param insertedDataLength inserted data length
     */
    void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength);

    /**
     * Loads data from given stream expecting provided size.
     * <p>
     * Preserves original data outside loaded range. Extends data if needed.
     *
     * @param startFrom start position to insert data
     * @param inputStream input stream
     * @param maximumDataSize size of data to load or -1 for all data
     * @return length of loaded data
     * @throws java.io.IOException if input/output error
     */
    long insert(long startFrom, InputStream inputStream, long maximumDataSize) throws IOException;

    /**
     * Replaces data in given area with provided data.
     * <p>
     * If sourceData are the same instance, data are replaced as it would be
     * copied to buffer first and replaced then.
     *
     * @param targetPosition target position to write to
     * @param replacingData data to read from
     */
    void replace(long targetPosition, BinaryData replacingData);

    /**
     * Replaces data in given area with provided data.
     * <p>
     * If sourceData are the same instance, data are replaced as it would be
     * copied to buffer first and replaced then.
     *
     * @param targetPosition target position to write to
     * @param replacingData data to read from
     * @param startFrom position to start copy from
     * @param length length of data to copy
     */
    void replace(long targetPosition, BinaryData replacingData, long startFrom, long length);

    /**
     * Replaces data in given area with provided data.
     * <p>
     * If sourceData are the same instance, data are replaced as it would be
     * copied to buffer first and replaced then.
     *
     * @param targetPosition target position to write to
     * @param replacingData data to read from
     */
    void replace(long targetPosition, byte[] replacingData);

    /**
     * Replaces data in given area with provided data.
     * <p>
     * If sourceData are the same instance, data are replaced as it would be
     * copied to buffer first and replaced then.
     *
     * @param targetPosition target position to write to
     * @param replacingData data to read from
     * @param replacingDataOffset position to start copy from
     * @param length length of data to copy
     */
    void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length);

    /**
     * Fills given area with empty data.
     *
     * @param startFrom position to fill data to
     * @param length length of area
     */
    void fillData(long startFrom, long length);

    /**
     * Fills given area with bytes of given value.
     *
     * @param startFrom position to fill data to
     * @param length length of area
     * @param fill value to fill with
     */
    void fillData(long startFrom, long length, byte fill);

    /**
     * Removes area of data.
     *
     * @param startFrom position to start removal from
     * @param length length of area
     */
    void remove(long startFrom, long length);

    /**
     * Removes all existing data.
     * <p>
     * Simply releases all references to data pages.
     */
    void clear();

    /**
     * Replaces all data by data red from given stream.
     *
     * @param inputStream input stream
     * @throws java.io.IOException if input/output error
     */
    void loadFromStream(InputStream inputStream) throws IOException;

    /**
     * Provides handler for output stream generation.
     * <p>
     * Received data are appended to existing data.
     *
     * @return new instance of output stream
     */
    @Nonnull
    OutputStream getDataOutputStream();
}
