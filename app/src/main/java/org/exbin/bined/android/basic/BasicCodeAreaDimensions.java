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
package org.exbin.bined.android.basic;

import android.graphics.Rect;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.basic.BasicCodeAreaZone;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaDimensions {

    protected int scrollPanelX;
    protected int scrollPanelY;
    protected int scrollPanelWidth;
    protected int scrollPanelHeight;
    protected int verticalScrollBarSize;
    protected int horizontalScrollBarSize;
    protected int dataViewWidth;
    protected int dataViewHeight;
    protected int lastCharOffset;
    protected int lastRowOffset;

    protected int headerAreaHeight;
    protected int rowPositionAreaWidth;
    protected int rowsPerRect;
    protected int rowsPerPage;
    protected int charactersPerPage;
    protected int charactersPerRect;

    protected final Rect componentRectangle = new Rect();
    protected final Rect mainAreaRectangle = new Rect();
    protected final Rect headerAreaRectangle = new Rect();
    protected final Rect rowPositionAreaRectangle = new Rect();
    protected final Rect scrollPanelRectangle = new Rect();
    protected final Rect dataViewRectangle = new Rect();
    protected final Rect dataViewInnerRectangle = new Rect();

    public void recomputeSizes(BasicCodeAreaMetrics metrics, int componentX, int componentY, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        modifyRect(componentRectangle, componentX, componentY, componentWidth, componentHeight);
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;

        scrollPanelX = componentX + rowPositionAreaWidth;
        scrollPanelY = componentY + headerAreaHeight;
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
        boolean availableHeight = scrollPanelY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            modifyRect(mainAreaRectangle, componentX + rowPositionAreaWidth, scrollPanelY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - scrollPanelY - getHorizontalScrollBarSize());
        } else {
            mainAreaRectangle.setEmpty();
        }
        if (availableWidth) {
            modifyRect(headerAreaRectangle, componentX + rowPositionAreaWidth, componentY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        } else {
            headerAreaRectangle.setEmpty();
        }
        if (availableHeight) {
            modifyRect(rowPositionAreaRectangle, componentX, scrollPanelY, rowPositionAreaWidth, componentHeight - scrollPanelY - getHorizontalScrollBarSize());
        } else {
            rowPositionAreaRectangle.setEmpty();
        }

        modifyRect(scrollPanelRectangle, scrollPanelX, scrollPanelY, scrollPanelWidth, scrollPanelHeight);
        modifyRect(dataViewRectangle, scrollPanelX, scrollPanelY, Math.max(dataViewWidth, 0), Math.max(dataViewHeight, 0));
        modifyRect(dataViewInnerRectangle, 0, 0, Math.max(dataViewWidth, 0), Math.max(dataViewHeight, 0));
    }

    @Nonnull
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= scrollPanelY) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            if (positionY >= scrollPanelY + scrollPanelHeight) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.ROW_POSITIONS;
            }
        }

        if (positionX >= scrollPanelX + scrollPanelWidth && positionY < scrollPanelY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= scrollPanelY + scrollPanelHeight) {
            if (positionX >= scrollPanelX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            } else {
                return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
            }
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    protected int computeCharactersPerRectangle(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    protected int computeCharactersPerPage(BasicCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    protected int computeRowsPerRectangle(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    protected int computeRowsPerPage(BasicCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    public int getScrollPanelX() {
        return scrollPanelX;
    }

    public int getScrollPanelY() {
        return scrollPanelY;
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
    public Rect getComponentRectangle() {
        return componentRectangle;
    }

    @Nonnull
    public Rect getMainAreaRectangle() {
        return mainAreaRectangle;
    }

    @Nonnull
    public Rect getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }

    @Nonnull
    public Rect getDataViewRectangle() {
        return dataViewRectangle;
    }

    @Nonnull
    public Rect getDataViewInnerRectangle() {
        return dataViewInnerRectangle;
    }

    @Nonnull
    public Rect getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    @Nonnull
    public Rect getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }

    private static void modifyRect(Rect rect, int x, int y, int width, int height) {
        rect.set(x, y, x + width, y + height);
    }
}
