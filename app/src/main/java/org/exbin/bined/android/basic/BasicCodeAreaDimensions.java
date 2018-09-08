/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.android.basic;

import android.graphics.Rect;

import javax.annotation.Nonnull;
import org.exbin.bined.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2018/09/07
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaDimensions {

    private int componentWidth;
    private int componentHeight;
    private int dataViewX;
    private int dataViewY;
    private int verticalScrollBarSize;
    private int horizontalScrollBarSize;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
    private int dataViewWidth;
    private int dataViewHeight;
    private int lastCharOffset;
    private int lastRowOffset;

    private int headerAreaHeight;
    private int rowPositionAreaWidth;
    private int rowsPerRect;
    private int rowsPerPage;
    private int charactersPerPage;
    private int charactersPerRect;

    @Nonnull
    private final Rect mainAreaRect = new Rect();
    @Nonnull
    private final Rect headerAreaRect = new Rect();
    @Nonnull
    private final Rect rowPositionAreaRect = new Rect();
    @Nonnull
    private final Rect scrollPanelRect = new Rect();
    @Nonnull
    private final Rect dataViewRect = new Rect();

    public void recomputeSizes(@Nonnull BasicCodeAreaMetrics metrics, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        this.componentWidth = componentWidth;
        this.componentHeight = componentHeight;
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;

        dataViewX = rowPositionAreaWidth;
        dataViewY = headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - verticalScrollBarSize;
        dataViewHeight = scrollPanelHeight - horizontalScrollBarSize;
        charactersPerRect = computeCharactersPerRect(metrics);
        charactersPerPage = computeCharactersPerPage(metrics);
        rowsPerRect = computeRowsPerRect(metrics);
        rowsPerPage = computeRowsPerPage(metrics);
        lastCharOffset = metrics.isInitialized() ? dataViewWidth % metrics.getCharacterWidth() : 0;
        lastRowOffset = metrics.isInitialized() ? dataViewHeight % metrics.getRowHeight() : 0;

        boolean availableWidth = rowPositionAreaWidth + verticalScrollBarSize <= componentWidth;
        boolean availableHeight = dataViewY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            mainAreaRect.set(rowPositionAreaWidth, dataViewY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - dataViewY - getHorizontalScrollBarSize());
        } else {
            mainAreaRect.set(0, 0, 0, 0);
        }
        if (availableWidth) {
            headerAreaRect.set(rowPositionAreaWidth, 0, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        } else {
            headerAreaRect.set(0, 0, 0, 0);
        }
        if (availableHeight) {
            rowPositionAreaRect.set(0, dataViewY, rowPositionAreaWidth, componentHeight - dataViewY - getHorizontalScrollBarSize());
        } else {
            rowPositionAreaRect.set(0, 0, 0, 0);
        }

        scrollPanelRect.set(dataViewX, dataViewY, scrollPanelWidth, scrollPanelHeight);
        dataViewRect.set(dataViewX, dataViewY, dataViewWidth >= 0 ? dataViewWidth : 0, dataViewHeight >= 0 ? dataViewHeight : 0);
    }

    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= dataViewY) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            return BasicCodeAreaZone.ROW_POSITIONS;
        }

        if (positionX >= dataViewX + scrollPanelWidth && positionY < dataViewY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= dataViewY + scrollPanelHeight) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else if (positionX >= dataViewX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            }

            return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    public int getComponentWidth() {
        return componentWidth;
    }

    public int getComponentHeight() {
        return componentHeight;
    }

    public int getDataViewX() {
        return dataViewX;
    }

    public int getDataViewY() {
        return dataViewY;
    }

    public int getVerticalScrollBarSize() {
        return verticalScrollBarSize;
    }

    public int getHorizontalScrollBarSize() {
        return horizontalScrollBarSize;
    }

    public int getScrollPanelWidth() {
        return scrollPanelWidth;
    }

    public int getScrollPanelHeight() {
        return scrollPanelHeight;
    }

    public int getDataViewWidth() {
        return dataViewWidth;
    }

    public int getDataViewHeight() {
        return dataViewHeight;
    }

    public int getHeaderAreaHeight() {
        return headerAreaHeight;
    }

    public int getRowPositionAreaWidth() {
        return rowPositionAreaWidth;
    }

    public int getRowsPerRect() {
        return rowsPerRect;
    }

    public int getCharactersPerRect() {
        return charactersPerRect;
    }

    public int getCharactersPerPage() {
        return charactersPerPage;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getLastCharOffset() {
        return lastCharOffset;
    }

    public int getLastRowOffset() {
        return lastRowOffset;
    }

    @Nonnull
    public Rect getMainAreaRect() {
        return mainAreaRect;
    }

    @Nonnull
    public Rect getScrollPanelRect() {
        return scrollPanelRect;
    }

    @Nonnull
    public Rect getDataViewRect() {
        return dataViewRect;
    }

    public Rect getHeaderAreaRect() {
        return headerAreaRect;
    }

    public Rect getRowPositionAreaRect() {
        return rowPositionAreaRect;
    }

    private int computeCharactersPerRect(@Nonnull BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage(@Nonnull BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    private int computeRowsPerRect(@Nonnull BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage(@Nonnull BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }
}
