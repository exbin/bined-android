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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Data source using android data content.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DeltaDataPageWindow {

    public static final int PAGE_SIZE = 1024;

    @Nonnull
    private final ContentDataSource data;
    private FileChannel fileChannel;
    @Nonnull
    private final DataPage[] dataPages = new DataPage[]{new DataPage(), new DataPage()};
    private int activeDataPage = 1;

    public DeltaDataPageWindow(ContentDataSource data) throws FileNotFoundException {
        this.data = data;
        fileChannel = data.getInputStream().getChannel();
        dataPages[0].pageIndex = 0;
        loadPage(0);
        data.addCacheClearListener(() -> {
            DeltaDataPageWindow.this.clearCache();
        });
    }

    private void loadPage(int index) {
        try {
            long pageIndex = dataPages[index].pageIndex;
            long pagePosition = pageIndex * PAGE_SIZE;
            long fileLength = data.getDataLength();
            byte[] page = dataPages[index].page;
            int offset = 0;
            int toRead = PAGE_SIZE;
            if (pagePosition + PAGE_SIZE > fileLength) {
                toRead = (int) (fileLength - pagePosition);
            }
            while (toRead > 0) {
                int red = fileChannel.read(ByteBuffer.wrap(page, offset, toRead), pagePosition);
                if (red == -1) {
                    throw new IOException("Unexpected read error ");
                }
                toRead -= red;
                offset += red;
                pagePosition += red;
            }
        } catch (IOException ex) {
            Logger.getLogger(DeltaDataPageWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte getByte(long position) {
        long targetPageIndex = position / PAGE_SIZE;
        int index = -1;
        long pageIndex1 = dataPages[0].pageIndex;
        long pageIndex2 = dataPages[1].pageIndex;
        if (pageIndex1 == targetPageIndex) {
            index = 0;
        } else if (pageIndex2 == targetPageIndex) {
            index = 1;
        }
        if (index == -1) {
            DataPage dataPage = dataPages[activeDataPage];
            dataPage.pageIndex = targetPageIndex;
            loadPage(activeDataPage);
            activeDataPage = (activeDataPage + 1) & 1;
            return dataPage.page[(int) (position % PAGE_SIZE)];
        }

        return dataPages[index].page[(int) (position % PAGE_SIZE)];
    }

    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        long targetPageIndex = position / PAGE_SIZE;
        int index = -1;
        long pageIndex1 = dataPages[0].pageIndex;
        long pageIndex2 = dataPages[1].pageIndex;
        if (pageIndex1 == targetPageIndex) {
            index = 0;
        } else if (pageIndex2 == targetPageIndex) {
            index = 1;
        }
        if (index == -1) {
            DataPage dataPage = dataPages[activeDataPage];
            dataPage.pageIndex = targetPageIndex;
            loadPage(activeDataPage);
            activeDataPage = (activeDataPage + 1) & 1;

            int pageOffset = (int) (position % PAGE_SIZE);
            int red = Math.min(PAGE_SIZE - pageOffset, length);
            System.arraycopy(dataPage.page, pageOffset, buffer, offset, red);
            return red;
        }

        int pageOffset = (int) (position % PAGE_SIZE);
        int red = Math.min(PAGE_SIZE - pageOffset, length);
        System.arraycopy(dataPages[index].page, pageOffset, buffer, offset, red);
        return red;
    }

    /**
     * Clears window cache.
     */
    public void clearCache() {
        fileChannel = data.getInputStream().getChannel();

        dataPages[0].pageIndex = -1;
        dataPages[1].pageIndex = -1;
    }

    /**
     * Simple structure for data page.
     */
    private static class DataPage {

        long pageIndex = -1;
        byte[] page;

        public DataPage() {
            page = new byte[PAGE_SIZE];
        }
    }
}
