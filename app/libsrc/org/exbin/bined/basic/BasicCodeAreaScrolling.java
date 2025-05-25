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
package org.exbin.bined.basic;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.DataProvider;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.capability.BasicScrollingCapable;

/**
 * Code area scrolling.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaScrolling {

    @Nonnull
    protected final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    protected ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
    protected int horizontalExtentDifference;
    protected int verticalExtentDifference;
    protected int horizontalScrollBarHeight;
    protected int verticalScrollBarWidth;

    protected int lastVerticalScrollingValue = -1;

    @Nonnull
    protected VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    @Nonnull
    protected ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    protected HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;
    @Nonnull
    protected ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    protected final CodeAreaScrollPosition maximumScrollPosition = new CodeAreaScrollPosition();

    protected int maximumScrollBarHeight = Integer.MAX_VALUE;

    @Nullable
    private Runnable verticalExtentChangeListener = null;
    @Nullable
    private Runnable horizontalExtentChangeListener = null;

    public BasicCodeAreaScrolling() {
    }

    public void updateCache(DataProvider codeArea, int horizontalScrollBarHeight, int verticalScrollBarWidth) {
        verticalScrollUnit = ((BasicScrollingCapable) codeArea).getVerticalScrollUnit();
        verticalScrollBarVisibility = ((BasicScrollingCapable) codeArea).getVerticalScrollBarVisibility();
        horizontalScrollUnit = ((BasicScrollingCapable) codeArea).getHorizontalScrollUnit();
        horizontalScrollBarVisibility = ((BasicScrollingCapable) codeArea).getHorizontalScrollBarVisibility();
        this.horizontalScrollBarHeight = horizontalScrollBarHeight;
        this.verticalScrollBarWidth = verticalScrollBarWidth;
    }

    public void computeViewDimension(ScrollViewDimension outputDimension, int dataViewWidth, int dataViewHeight, BasicCodeAreaLayout layout, BasicCodeAreaStructure structure, int characterWidth, int rowHeight) {
        int charsPerRow = structure.getCharactersPerRow();
        int dataWidth = layout.computePositionX(charsPerRow, characterWidth);
        boolean fitsHorizontally = computeFitsHorizontally(dataViewWidth, dataWidth);

        long rowsPerData = structure.getRowsPerDocument();
        boolean fitsVertically = computeFitsVertically(dataViewHeight, rowsPerData, rowHeight);

        if (!fitsVertically) {
            fitsHorizontally = computeFitsHorizontally(dataViewWidth - verticalScrollBarWidth, dataWidth);
        }
        if (!fitsHorizontally) {
            fitsVertically = computeFitsVertically(dataViewHeight - horizontalScrollBarHeight, rowsPerData, rowHeight);
        }

        int width;
        if (fitsHorizontally) {
            width = dataWidth;
            changeVerticalExtentDifference(0);
        } else {
            width = recomputeScrollViewWidth(dataViewWidth, characterWidth, dataWidth, charsPerRow);
        }

        int height;
        if (fitsVertically) {
            height = (int) (rowsPerData * rowHeight);
            changeHorizontalExtentDifference(0);
        } else {
            height = recomputeScrollViewHeight(dataViewHeight, rowHeight, rowsPerData);
        }

        outputDimension.setDimension(dataViewWidth, dataViewHeight, width, height);
    }

    protected boolean computeFitsHorizontally(int dataViewWidth, int dataWidth) {
        return dataWidth <= dataViewWidth;
    }

    protected boolean computeFitsVertically(int dataViewHeight, long rowsPerData, int rowHeight) {
        int availableRows = (dataViewHeight + rowHeight - 1) / rowHeight;
        if (rowsPerData > availableRows) {
            return false;
        }

        return rowsPerData * rowHeight <= dataViewHeight;
    }

    protected int recomputeScrollViewWidth(int dataViewWidth, int characterWidth, int dataWidth, int charsPerRow) {
        int scrollViewWidth = 0;
        switch (horizontalScrollUnit) {
            case PIXEL: {
                scrollViewWidth = dataWidth;
                changeHorizontalExtentDifference(0);
                break;
            }
            case CHARACTER: {
                int charsPerDataView = dataViewWidth / characterWidth;
                scrollViewWidth = dataViewWidth + (charsPerRow - charsPerDataView);
                changeHorizontalExtentDifference(dataViewWidth - charsPerDataView);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(horizontalScrollUnit);
        }

        return scrollViewWidth;
    }

    protected int recomputeScrollViewHeight(int dataViewHeight, int rowHeight, long rowsPerData) {
        int scrollViewHeight = 0;
        switch (verticalScrollUnit) {
            case PIXEL: {
                if (rowsPerData > maximumScrollBarHeight / rowHeight) {
                    scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                    scrollViewHeight = maximumScrollBarHeight;
                    changeVerticalExtentDifference(0);
                } else {
                    scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                    scrollViewHeight = (int) (rowsPerData * rowHeight);
                    changeVerticalExtentDifference(0);
                }
                break;
            }
            case ROW: {
                if (rowsPerData > (maximumScrollBarHeight - dataViewHeight)) {
                    scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                    scrollViewHeight = maximumScrollBarHeight;
                    changeVerticalExtentDifference(0);
                } else {
                    scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                    int rowsPerDataView = dataViewHeight / rowHeight;
                    scrollViewHeight = (int) (dataViewHeight + (rowsPerData - rowsPerDataView));
                    changeVerticalExtentDifference(dataViewHeight - rowsPerDataView);
                }
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(verticalScrollUnit);
        }

        return scrollViewHeight;
    }

    public void updateHorizontalScrollBarValue(int scrollBarValue, int characterWidth) {
        if (characterWidth == 0) {
            return;
        }

        switch (horizontalScrollUnit) {
            case PIXEL: {
                scrollPosition.setCharPosition(scrollBarValue / characterWidth);
                scrollPosition.setCharOffset(scrollBarValue % characterWidth);
                break;
            }
            case CHARACTER: {
                scrollPosition.setCharPosition(scrollBarValue);
                scrollPosition.setCharOffset(0);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(horizontalScrollUnit);
        }
    }

    public void updateVerticalScrollBarValue(int scrollBarValue, int rowHeight, int maxValue, long rowsPerDocumentToLastPage) {
        if (rowHeight == 0) {
            scrollPosition.setRowPosition(0);
            scrollPosition.setRowOffset(0);
            return;
        }

        switch (verticalScrollUnit) {
            case PIXEL: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    if (scrollBarValue == maxValue) {
                        scrollPosition.setRowPosition(maximumScrollPosition.getRowPosition());
                        scrollPosition.setRowOffset(maximumScrollPosition.getRowOffset());
                    } else {
                        long targetRow;
                        if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
                            targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
                            long rest = rowsPerDocumentToLastPage % maxValue;
                            targetRow += (rest * scrollBarValue) / maxValue;
                        } else {
                            targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / maximumScrollBarHeight;
                        }
                        scrollPosition.setRowPosition(targetRow);
                    }
                    if (verticalScrollUnit != VerticalScrollUnit.ROW) {
                        scrollPosition.setRowOffset(0);
                    }
                    return;
                }

                scrollPosition.setRowPosition(scrollBarValue / rowHeight);
                scrollPosition.setRowOffset(scrollBarValue % rowHeight);
                break;
            }
            case ROW: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    if (scrollBarValue == maxValue) {
                        scrollPosition.setRowPosition(maximumScrollPosition.getRowPosition());
                        scrollPosition.setRowOffset(maximumScrollPosition.getRowOffset());
                    } else {
                        long targetRow;
                        if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
                            targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
                            long rest = rowsPerDocumentToLastPage % maxValue;
                            targetRow += (rest * scrollBarValue) / maxValue;
                        } else {
                            targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / maximumScrollBarHeight;
                        }
                        scrollPosition.setRowPosition(targetRow);
                    }
                    if (verticalScrollUnit != VerticalScrollUnit.ROW) {
                        scrollPosition.setRowOffset(0);
                    }
                    return;
                }

                int rowPosition = scrollBarValue;
                scrollPosition.setRowPosition(rowPosition);
                scrollPosition.setRowOffset(0);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(verticalScrollUnit);
        }
    }

    public int getVerticalScrollValue(int rowHeight, long rowsPerDocument) {
        switch (verticalScrollUnit) {
            case PIXEL: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    int scrollValue;
                    if (scrollPosition.getRowPosition() < Long.MAX_VALUE / maximumScrollBarHeight) {
                        scrollValue = (int) ((scrollPosition.getRowPosition() * maximumScrollBarHeight) / rowsPerDocument);
                    } else {
                        scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / maximumScrollBarHeight));
                    }
                    return scrollValue;
                }
                return (int) (scrollPosition.getRowPosition() * rowHeight + scrollPosition.getRowOffset());
            }
            case ROW: {
                if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    int scrollValue;
                    if (scrollPosition.getRowPosition() < Long.MAX_VALUE / maximumScrollBarHeight) {
                        scrollValue = (int) ((scrollPosition.getRowPosition() * maximumScrollBarHeight) / rowsPerDocument);
                    } else {
                        scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / maximumScrollBarHeight));
                    }
                    return scrollValue;
                }
                return (int) scrollPosition.getRowPosition();
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(verticalScrollUnit);
        }
    }

    public int getHorizontalScrollValue(int characterWidth) {
        switch (horizontalScrollUnit) {
            case PIXEL:
                return scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
            case CHARACTER:
                return scrollPosition.getCharPosition();
            default:
                throw CodeAreaUtils.getInvalidTypeException(horizontalScrollUnit);
        }
    }

    public void setVerticalExtentChangeListener(Runnable verticalExtentChangeListener) {
        this.verticalExtentChangeListener = verticalExtentChangeListener;
    }

    public void setHorizontalExtentChangeListener(Runnable horizontalExtentChangeListener) {
        this.horizontalExtentChangeListener = horizontalExtentChangeListener;
    }

    public int getMaximumScrollBarHeight() {
        return maximumScrollBarHeight;
    }

    public void setMaximumScrollBarHeight(int maximumScrollBarHeight) {
        this.maximumScrollBarHeight = maximumScrollBarHeight;
    }

    @Nonnull
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction, int rowsPerPage, long rowsPerDocument) {
        CodeAreaScrollPosition targetPosition = new CodeAreaScrollPosition();
        targetPosition.setScrollPosition(startPosition);

        switch (direction) {
            case UP: {
                if (startPosition.getRowPosition() == 0) {
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - 1);
                }
                break;
            }
            case DOWN: {
                if (maximumScrollPosition.isRowPositionGreaterThan(startPosition)) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + 1);
                }
                break;
            }
            case LEFT: {
                if (startPosition.getCharPosition() == 0) {
                    targetPosition.setCharOffset(0);
                } else {
                    targetPosition.setCharPosition(startPosition.getCharPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (maximumScrollPosition.isCharPositionGreaterThan(startPosition)) {
                    targetPosition.setCharPosition(startPosition.getCharPosition() + 1);
                }
                break;
            }
            case PAGE_UP: {
                if (startPosition.getRowPosition() < rowsPerPage) {
                    targetPosition.setRowPosition(0);
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - rowsPerPage);
                }
                break;
            }
            case PAGE_DOWN: {
                if (startPosition.getRowPosition() <= rowsPerDocument - rowsPerPage * 2) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + rowsPerPage);
                } else if (rowsPerDocument > rowsPerPage) {
                    targetPosition.setRowPosition(rowsPerDocument - rowsPerPage);
                } else {
                    targetPosition.setRowPosition(0);
                }
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(direction);
        }

        return targetPosition;
    }

    public void performScrolling(ScrollingDirection direction, int rowsPerPage, long rowsPerDocument) {
        setScrollPosition(computeScrolling(scrollPosition, direction, rowsPerPage, rowsPerDocument));
    }

    @Nonnull
    public PositionScrollVisibility computePositionScrollVisibility(long rowPosition, int charPosition, int bytesPerRow, int rowsPerPage, int charsPerPage, int charOffset, int rowOffset, int characterWidth, int rowHeight) {
        boolean partial = false;

        PositionScrollVisibility topVisibility = checkTopScrollVisibility(rowPosition);
        if (topVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= topVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility bottomVisibility = checkBottomScrollVisibility(rowPosition, rowsPerPage, rowOffset, rowHeight);
        if (bottomVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= bottomVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility leftVisibility = checkLeftScrollVisibility(charPosition);
        if (leftVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= leftVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility rightVisibility = checkRightScrollVisibility(charPosition, charsPerPage, charOffset, characterWidth);
        if (rightVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= rightVisibility == PositionScrollVisibility.PARTIAL;

        return partial ? PositionScrollVisibility.PARTIAL : PositionScrollVisibility.VISIBLE;
    }

    @Nonnull
    public Optional<CodeAreaScrollPosition> computeRevealScrollPosition(long rowPosition, int charsPosition, int bytesPerRow, int rowsPerPage, int charsPerPage, int charOffset, int rowOffset, int characterWidth, int rowHeight) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);

        boolean scrolled = false;
        if (checkBottomScrollVisibility(rowPosition, rowsPerPage, rowOffset, rowHeight) != PositionScrollVisibility.VISIBLE) {
            int bottomRowOffset;
            if (verticalScrollUnit != VerticalScrollUnit.PIXEL) {
                bottomRowOffset = 0;
            } else {
                if (rowsPerPage == 0) {
                    bottomRowOffset = 0;
                } else {
                    bottomRowOffset = rowHeight - rowOffset;
                }
            }

            long targetRowPosition = rowPosition - rowsPerPage;
            if (verticalScrollUnit == VerticalScrollUnit.ROW && rowOffset > 0) {
                targetRowPosition++;
            }
            targetScrollPosition.setRowPosition(targetRowPosition);
            targetScrollPosition.setRowOffset(bottomRowOffset);
            scrolled = true;
        }

        if (checkTopScrollVisibility(rowPosition) != PositionScrollVisibility.VISIBLE) {
            targetScrollPosition.setRowPosition(rowPosition);
            targetScrollPosition.setRowOffset(0);
            scrolled = true;
        }

        if (checkRightScrollVisibility(charsPosition, charsPerPage, charOffset, characterWidth) != PositionScrollVisibility.VISIBLE) {
            int rightCharOffset;
            if (horizontalScrollUnit != HorizontalScrollUnit.PIXEL) {
                rightCharOffset = 0;
            } else {
                if (charsPerPage < 1) {
                    rightCharOffset = 0;
                } else {
                    rightCharOffset = characterWidth - (charOffset % characterWidth);
                }
            }

            // Scroll character right
            setHorizontalScrollPosition(targetScrollPosition, charsPosition - charsPerPage, rightCharOffset, characterWidth);
            scrolled = true;
        }

        if (checkLeftScrollVisibility(charsPosition) != PositionScrollVisibility.VISIBLE) {
            setHorizontalScrollPosition(targetScrollPosition, charsPosition, 0, characterWidth);
            scrolled = true;
        }

        return scrolled ? Optional.of(targetScrollPosition) : Optional.empty();
    }

    @Nonnull
    protected PositionScrollVisibility checkTopScrollVisibility(long rowPosition) {
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            return rowPosition < scrollPosition.getRowPosition() ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (rowPosition > scrollPosition.getRowPosition() || (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    protected PositionScrollVisibility checkBottomScrollVisibility(long rowPosition, int rowsPerPage, int rowOffset, int rowHeight) {
        int sumOffset = scrollPosition.getRowOffset() + rowOffset;

        long lastFullRow = scrollPosition.getRowPosition() + rowsPerPage;
        if (rowOffset > 0) {
            lastFullRow--;
        }
        if (sumOffset >= rowHeight) {
            lastFullRow++;
        }

        if (rowPosition <= lastFullRow) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != rowHeight && rowPosition == lastFullRow + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    protected PositionScrollVisibility checkLeftScrollVisibility(int charsPosition) {
        int charPos = scrollPosition.getCharPosition();
        if (horizontalScrollUnit != HorizontalScrollUnit.PIXEL) {
            return charsPosition < charPos ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (charsPosition > charPos || (charsPosition == charPos && scrollPosition.getCharOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (charsPosition == charPos && scrollPosition.getCharOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    protected PositionScrollVisibility checkRightScrollVisibility(int charsPosition, int charsPerPage, int charOffset, int characterWidth) {
        int sumOffset = scrollPosition.getCharOffset() + charOffset;

        int lastFullChar = scrollPosition.getCharPosition() + charsPerPage;
        if (charOffset > 0) {
            lastFullChar--;
        }
        if (sumOffset >= characterWidth) {
            lastFullChar++;
        }

        if (charsPosition <= lastFullChar) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != characterWidth && charsPosition == lastFullChar + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    public Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(long rowPosition, int charPosition, int bytesPerRow, int rowsPerRect, int charactersPerRect, int dataViewWidth, int dataViewHeight, int characterWidth, int rowHeight) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);

        long centerRowPosition = rowPosition - rowsPerRect / 2;
        int rowCorrection = (rowsPerRect & 1) == 0 ? rowHeight : 0;
        int heightDiff = (rowsPerRect * rowHeight + rowCorrection - dataViewHeight) / 2;
        int rowOffset;
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            rowOffset = 0;
        } else {
            if (heightDiff > 0) {
                rowOffset = heightDiff;
            } else {
                rowOffset = 0;
            }
        }
        if (centerRowPosition < 0) {
            centerRowPosition = 0;
            rowOffset = 0;
        } else if (centerRowPosition > maximumScrollPosition.getRowPosition() || (centerRowPosition == maximumScrollPosition.getRowPosition() && rowOffset > maximumScrollPosition.getRowOffset())) {
            centerRowPosition = maximumScrollPosition.getRowPosition();
            rowOffset = maximumScrollPosition.getRowOffset();
        }
        targetScrollPosition.setRowPosition(centerRowPosition);
        targetScrollPosition.setRowOffset(rowOffset);

        int centerCharPosition = charPosition - charactersPerRect / 2;
        int charCorrection = (charactersPerRect & 1) == 0 ? rowHeight : 0;
        int widthDiff = (charactersPerRect * characterWidth + charCorrection - dataViewWidth) / 2;
        int charOffset;
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            charOffset = 0;
        } else {
            if (widthDiff > 0) {
                charOffset = widthDiff;
            } else {
                charOffset = 0;
            }
        }
        if (centerCharPosition < 0) {
            centerCharPosition = 0;
            charOffset = 0;
        } else if (centerCharPosition > maximumScrollPosition.getCharPosition() || (centerCharPosition == maximumScrollPosition.getCharPosition() && charOffset > maximumScrollPosition.getCharOffset())) {
            centerCharPosition = maximumScrollPosition.getCharPosition();
            charOffset = maximumScrollPosition.getCharOffset();
        }
        targetScrollPosition.setCharPosition(centerCharPosition);
        targetScrollPosition.setCharOffset(charOffset);
        return Optional.of(targetScrollPosition);
    }

    public void updateMaximumScrollPosition(long rowsPerDocument, int rowsPerPage, int charactersPerRow, int charactersPerPage, int lastCharOffset, int lastRowOffset) {
        maximumScrollPosition.reset();
        if (rowsPerDocument > rowsPerPage) {
            maximumScrollPosition.setRowPosition(rowsPerDocument - rowsPerPage);
        }
        if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
            maximumScrollPosition.setRowOffset(lastRowOffset);
        }

        if (charactersPerRow > charactersPerPage) {
            maximumScrollPosition.setCharPosition(charactersPerRow - charactersPerPage);
        }
        if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
            maximumScrollPosition.setCharOffset(lastCharOffset);
        }
    }

    public int getHorizontalScrollX(int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                return scrollPosition.getCharPosition() * characterWidth;
            }
            case PIXEL: {
                return scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(horizontalScrollUnit);
        }
    }

    protected void setHorizontalScrollPosition(CodeAreaScrollPosition scrollPosition, int charPos, int pixelOffset, int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                scrollPosition.setCharPosition(charPos);
                scrollPosition.setCharOffset(0);
                break;
            }
            case PIXEL: {
                if (pixelOffset > characterWidth) {
                    pixelOffset = pixelOffset % characterWidth;
                }
                scrollPosition.setCharPosition(charPos);
                scrollPosition.setCharOffset(pixelOffset);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(horizontalScrollUnit);
        }
    }

    @Nonnull
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(CodeAreaScrollPosition scrollPosition) {
        this.scrollPosition.setScrollPosition(scrollPosition);
        if (scrollPosition.isRowPositionGreaterThan(maximumScrollPosition)) {
            this.scrollPosition.setRowPosition(maximumScrollPosition.getRowPosition());
            this.scrollPosition.setRowOffset(maximumScrollPosition.getRowOffset());
        }
        if (scrollPosition.isCharPositionGreaterThan(maximumScrollPosition)) {
            this.scrollPosition.setCharPosition(maximumScrollPosition.getCharPosition());
            this.scrollPosition.setCharOffset(maximumScrollPosition.getCharOffset());
        }
    }

    public int getHorizontalExtentDifference() {
        return horizontalExtentDifference;
    }

    public int getVerticalExtentDifference() {
        return verticalExtentDifference;
    }

    @Nonnull
    public ScrollBarVerticalScale getScrollBarVerticalScale() {
        return scrollBarVerticalScale;
    }

    public void setScrollBarVerticalScale(ScrollBarVerticalScale scrollBarVerticalScale) {
        this.scrollBarVerticalScale = scrollBarVerticalScale;
    }

    @Nonnull
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Nonnull
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Nonnull
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Nonnull
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Nonnull
    public CodeAreaScrollPosition getMaximumScrollPosition() {
        return maximumScrollPosition;
    }

    public void setLastVerticalScrollingValue(int value) {
        lastVerticalScrollingValue = value;
    }

    public int getLastVerticalScrollingValue() {
        return lastVerticalScrollingValue;
    }

    public void clearLastVerticalScrollingValue() {
        lastVerticalScrollingValue = -1;
    }

    protected void changeVerticalExtentDifference(int newDifference) {
        if (verticalExtentDifference != newDifference) {
            verticalExtentDifference = newDifference;
            if (verticalExtentChangeListener != null) {
                verticalExtentChangeListener.run();
            }
        }
    }

    protected void changeHorizontalExtentDifference(int newDifference) {
        if (horizontalExtentDifference != newDifference) {
            horizontalExtentDifference = newDifference;
            if (horizontalExtentChangeListener != null) {
                horizontalExtentChangeListener.run();
            }
        }
    }
}
