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
package org.exbin.auxiliary.binary_data.delta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;

/**
 * Data source for binary data stored in memory.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MemoryDataSource implements EditableBinaryData {

    @Nonnull
    private final EditableBinaryData data;

    public MemoryDataSource(EditableBinaryData data) {
        this.data = data;
    }

    @Override
    public void setDataSize(long size) {
        data.setDataSize(size);
    }

    @Override
    public void setByte(long position, byte value) {
        data.setByte(position, value);
    }

    @Override
    public void insert(long startFrom, long length) {
        data.insert(startFrom, length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        data.insert(startFrom, insertedData);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        data.insert(startFrom, insertedData);
    }

    @Override
    public void remove(long startFrom, long length) {
        data.remove(startFrom, length);
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public long getDataSize() {
        return data.getDataSize();
    }

    @Override
    public byte getByte(long position) {
        return data.getByte(position);
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        data.saveToStream(outputStream);
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        return data.copy();
    }

    @Nonnull
    @Override
    public BinaryData copy(long startFrom, long length) {
        return data.copy(startFrom, length);
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        data.insertUninitialized(startFrom, length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        data.insert(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        data.insert(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long maximumDataSize) throws IOException {
        return data.insert(startFrom, inputStream, maximumDataSize);
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        data.replace(targetPosition, replacingData);
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long length) {
        data.replace(targetPosition, replacingData, startFrom, length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        data.replace(targetPosition, replacingData);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        data.replace(targetPosition, replacingData, replacingDataOffset, length);
    }

    @Override
    public void fillData(long startFrom, long length) {
        data.fillData(startFrom, length);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        data.fillData(startFrom, length, fill);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        data.loadFromStream(inputStream);
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        return data.getDataOutputStream();
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        data.copyToArray(startFrom, target, offset, length);
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        return data.getDataInputStream();
    }

    @Override
    public void dispose() {
        data.dispose();
    }
}
