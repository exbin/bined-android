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

import java.io.IOException;
import java.io.InputStream;
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
    @Nonnull
    private final DeltaDataPageWindow window;
    private final List<CacheClearListener> listeners = new ArrayList<>();
    private boolean closed = false;

    public ContentDataSource(ContentResolver contentResolver, Uri fileUri) {
        this.contentResolver = contentResolver;
        this.fileUri = fileUri;
        window = new DeltaDataPageWindow(this);
    }

    @Nonnull
    public ContentResolver getContentResolver() {
        return contentResolver;
    }

    @Nonnull
    public Uri getFileUri() {
        return fileUri;
    }

    @Override
    public long getDataLength() throws IOException {
        try (AssetFileDescriptor descriptor = contentResolver.openAssetFileDescriptor(fileUri, "r")) {
            return descriptor.getLength();
        }
    }

    @Override
    public void setDataLength(long dataLength) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(long position) throws IOException {
        checkClosed();
        return window.getByte(position);
        /* try (InputStream stream = contentResolver.openInputStream(fileUri)) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        try (InputStream stream = contentResolver.openInputStream(fileUri)) {
            stream.skip(position);
            return stream.read(buffer, offset, length);
        }
    }

    @Override
    public void write(long position, byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearCache() {
        listeners.forEach(listener -> {
            listener.clearCache();
        });
    }

    @Override
    public void close() throws IOException {
        checkClosed();
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

    public static interface CacheClearListener {

        void clearCache();
    }
}
