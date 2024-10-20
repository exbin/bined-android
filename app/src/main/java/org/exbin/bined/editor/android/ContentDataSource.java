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
package org.exbin.bined.editor.android;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import org.exbin.auxiliary.binary_data.delta.DataSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Data source using android data content.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ContentDataSource implements DataSource {

    private final ContentResolver contentResolver;
    private final Uri fileUri;

    private AssetFileDescriptor descriptor;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    @Nonnull
    private final DeltaDataPageWindow window;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private FileChannel fileChannel;
    private final List<CacheClearListener> listeners = new ArrayList<>();
    private long dataLength;
    private boolean closed = false;

    public ContentDataSource(ContentResolver contentResolver, Uri fileUri) throws IOException {
        this.contentResolver = contentResolver;
        this.fileUri = fileUri;
        descriptor = contentResolver.openAssetFileDescriptor(fileUri, "rwa");
        outputStream = descriptor.createOutputStream();
        inputStream = descriptor.createInputStream();
        fileChannel = outputStream.getChannel();
        dataLength = descriptor.getLength();
        window = new DeltaDataPageWindow(this);
    }

    @Nonnull
    public ContentResolver getContentResolver() {
        return contentResolver;
    }

    @Nonnull
    public FileInputStream getInputStream() {
        return inputStream;
    }

    @Nonnull
    public Uri getFileUri() {
        return fileUri;
    }

    @Override
    public long getDataLength() throws IOException {
        return descriptor.getLength();
    }

    @Override
    public void setDataLength(long dataLength) throws IOException {
        this.dataLength = dataLength;
        fileChannel.truncate(dataLength);
    }

    @Override
    public byte getByte(long position) throws IOException {
        checkClosed();
        return window.getByte(position);
        /*
        try (InputStream stream = contentResolver.openInputStream(fileUri)) {
            stream.skip(position);
            int value = stream.read();
            if (value == -1) {
                throw new IOException("Read exception");
            }
            return (byte) value;
        } */
    }

    @Override
    public void setByte(long position, byte value) throws IOException {
        byteBuffer.put(0, value);
        fileChannel.write(byteBuffer, position);
        if (position > dataLength) {
            dataLength = position + 1;
        }
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        checkClosed();
        return window.read(position, buffer, offset, length);
    }

    @Override
    public void write(long position, byte[] buffer, int offset, int length) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(buffer, offset, length);
        int written = fileChannel.write(writeBuffer, position);
        if (written == -1) {
            throw new IllegalStateException("Writing error at position " + position);
        }

        if (position + length > dataLength) {
            dataLength = position + length;
        }
    }

    @Override
    public void clearCache() {
        flush();
        listeners.forEach(listener -> {
            listener.clearCache();
        });
    }

    private void flush() {
        checkClosed();

        try {
            fileChannel.close();
            inputStream.close();
            outputStream.close();

            outputStream = descriptor.createOutputStream();
            inputStream = descriptor.createInputStream();
            fileChannel = outputStream.getChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        checkClosed();
        fileChannel.close();
        inputStream.close();
        outputStream.close();
        descriptor.close();
        closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("File was already closed");
        }
    }

    public void addCacheClearListener(CacheClearListener listener) {
        listeners.add(listener);
    }

    public void removeCacheClearListener(CacheClearListener listener) {
        listeners.remove(listener);
    }

    public interface CacheClearListener {

        void clearCache();
    }
}
