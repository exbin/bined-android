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
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2019/08/19
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaDimensions {

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
    private final Rect componentRect = new Rect();
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

    public void recomputeSizes(BasicCodeAreaMetrics metrics, int componentX, int componentY, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        modifyRect(componentRect, componentX, componentY, componentWidth, componentHeight);
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;

        dataViewX = componentX + rowPositionAreaWidth;
        dataViewY = componentY + headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - verticalScrollBarSize;
        dataViewHeight = scrollPanelHeight - horizontalScrollBarSize;
        charactersPerRect = computeCharactersPerRectangle(metrics);
        charactersPerPage = computeCharactersPerPage(metrics);
        rowsPerRect = computeRowsPerRectangle(metrics);
        rowsPerPage = computeRowsPerPage(metrics);
        lastCharOffset = metrics.isInitialized() ? dataViewWidth % metrics.getCharacterWidth() : 0;
        lastRowOffset = metrics.isInitialized() ? dataViewHeight % metrics.getRowHeight() : 0;

        boolean availableWidth = rowPositionAreaWidth + verticalScrollBarSize <= componentWidth;
        boolean availableHeight = dataViewY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            modifyRect(mainAreaRect, componentX + rowPositionAreaWidth, dataViewY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - dataViewY - getHorizontalScrollBarSize());
        } else {
            mainAreaRect.setEmpty();
        }
        if (availableWidth) {
            modifyRect(headerAreaRect, componentX + rowPositionAreaWidth, componentY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        } else {
            headerAreaRect.setEmpty();
        }
        if (availableHeight) {
            modifyRect(rowPositionAreaRect, componentX, dataViewY, rowPositionAreaWidth, componentHeight - dataViewY - getHorizontalScrollBarSize());
        } else {
            rowPositionAreaRect.setEmpty();
        }

        modifyRect(scrollPanelRect, dataViewX, dataViewY, scrollPanelWidth, scrollPanelHeight);
        modifyRect(dataViewRect, dataViewX, dataViewY, dataViewWidth >= 0 ? dataViewWidth : 0, dataViewHeight >= 0 ? dataViewHeight : 0);
    }

    @Nonnull
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= dataViewY) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            if (positionY >= dataViewY + scrollPanelHeight) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.ROW_POSITIONS;
            }
        }

        if (positionX >= dataViewX + scrollPanelWidth && positionY < dataViewY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= dataViewY + scrollPanelHeight) {
            if (positionX >= dataViewX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            } else {
                return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
            }
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    private int computeCharactersPerRectangle(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    private int computeRowsPerRectangle(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
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
    public Rect getComponentRect() {
        return componentRect;
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

    private static void modifyRect(Rect rect, int x, int y, int width, int height) {
        rect.set(x, y, x + width, y + height);
    }
}
