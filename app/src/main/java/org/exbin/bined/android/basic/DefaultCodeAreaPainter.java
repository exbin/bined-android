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

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.constraintlayout.solver.widgets.Rectangle;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.CodeAreaCharAssessor;
import org.exbin.bined.android.CodeAreaColorAssessor;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.android.CodeAreaPaintState;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.Font;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.android.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.android.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.android.capability.CharAssessorPainterCapable;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;
import org.exbin.bined.android.capability.FontCapable;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaLayout;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollViewDimension;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.BackgroundPaintCapable;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Code area component default painter.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaPainter implements CodeAreaPainter, BasicColorsCapableCodeAreaPainter, CodeAreaPaintState, ColorAssessorPainterCapable, CharAssessorPainterCapable {

    @Nonnull
    protected final CodeAreaCore codeArea;
    protected volatile boolean initialized = false;
    protected volatile boolean adjusting = false;

    protected volatile boolean fontChanged = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean resetColors = true;
    protected volatile boolean caretChanged = true;

    @Nullable
    protected Paint paint = null;
    @Nonnull
    protected final View dataView;
    @Nonnull
    protected final DefaultCodeAreaScrollPane scrollPanel;
    @Nonnull
    protected final DefaultCodeAreaMouseListener codeAreaMouseListener;

    protected int dataViewOffsetX = 0;
    protected int dataViewOffsetY = 0;
    protected int scrollOffsetX = 0;
    protected int scrollOffsetY = 0;

    @Nonnull
    protected final DataChangedListener codeAreaDataChangeListener;

    protected final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    protected final BasicCodeAreaStructure structure = new BasicCodeAreaStructure();
    protected final BasicCodeAreaScrolling scrolling = new BasicCodeAreaScrolling();
    protected final BasicCodeAreaDimensions dimensions = new BasicCodeAreaDimensions();
    protected final BasicCodeAreaVisibility visibility = new BasicCodeAreaVisibility();

    protected final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    @Nonnull
    protected BasicCodeAreaColorsProfile colorsProfile = new BasicCodeAreaColorsProfile();

    @Nullable
    protected CodeCharactersCase codeCharactersCase;
    @Nullable
    protected EditOperation editOperation;
    @Nullable
    protected BasicBackgroundPaintMode backgroundPaintMode;
    protected boolean showMirrorCursor;

    protected int rowPositionLength;
    protected int minRowPositionLength;
    protected int maxRowPositionLength;

    @Nullable
    protected Font font;
    @Nonnull
    protected Charset charset;
    @Nonnull
    protected CodeAreaColorAssessor colorAssessor = null;
    @Nonnull
    protected CodeAreaCharAssessor charAssessor = null;

    @Nullable
    protected RowDataCache rowDataCache = null;
    @Nullable
    protected CursorDataCache cursorDataCache = null;

    public DefaultCodeAreaPainter(final CodeAreaCore codeArea) {
        this.codeArea = codeArea;

        colorAssessor = new DefaultCodeAreaColorAssessor();
        charAssessor = new DefaultCodeAreaCharAssessor();

        scrollPanel = new DefaultCodeAreaScrollPane(codeArea.getContext()) {
            @Override
            protected void onScrollChanged(int horizontal, int vertical, int oldHorizontal, int oldVertical) {
                super.onScrollChanged(horizontal, vertical, oldHorizontal, oldVertical);

                scrolling.updateHorizontalScrollBarValue(horizontal, metrics.getCharacterWidth());

                int maxValue = Integer.MAX_VALUE; // - scrollPanel.getVerticalScrollBar().getVisibleAmount();
                long rowsPerDocumentToLastPage = structure.getRowsPerDocument() - dimensions.getRowsPerRect();
                scrolling.updateVerticalScrollBarValue(vertical, metrics.getRowHeight(), maxValue, rowsPerDocumentToLastPage);

                scrollOffsetX = horizontal;
                scrollOffsetY = vertical;
                dataViewOffsetX = scrollOffsetX - dimensions.getScrollPanelX();
                dataViewOffsetY = scrollOffsetY - dimensions.getScrollPanelY();

                ((ScrollingCapable) codeArea).setScrollPosition(scrolling.getScrollPosition());
            }
        };
        scrollPanel.setLongClickable(true);

        dataView = new View(DefaultCodeAreaPainter.this.codeArea.getContext()) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                paintMainArea(canvas);
            }
        };
        dataView.setLongClickable(true);
        scrollPanel.addView(dataView);

        codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
        codeAreaDataChangeListener = this::dataChanged;
    }

    @Override
    public void attach() {
        RelativeLayout.LayoutParams wrapLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        codeArea.addView(scrollPanel, wrapLayout);
        dataView.setOnTouchListener(codeAreaMouseListener);
        dataView.setOnLongClickListener(codeAreaMouseListener);
        dataView.setOnContextClickListener(codeAreaMouseListener);
        codeArea.addDataChangedListener(codeAreaDataChangeListener);
    }

    @Override
    public void detach() {
        this.codeArea.removeView(scrollPanel);
        dataView.setOnTouchListener(null);
        dataView.setOnLongClickListener(null);
        dataView.setOnContextClickListener(null);
        codeArea.removeDataChangedListener(codeAreaDataChangeListener);
    }

    @Override
    public void reset() {
        resetColors();
        resetFont();
        resetLayout();
        resetCaret();
        recomputeScrollState(); // TODO ?
    }

    @Override
    public void resetColors() {
        resetColors = true;
    }

    @Override
    public void resetFont() {
        fontChanged = true;
        resetLayout();
    }

    @Override
    public void resetLayout() {
        layoutChanged = true;
    }

    @Override
    public void resetCaret() {
        caretChanged = true;
    }

    @Override
    public void rebuildColors() {
        colorsProfile.reinitialize();
    }

    public void recomputeLayout() {
        rowPositionLength = getRowPositionLength();
        recomputeDimensions();
        dataViewOffsetX = scrollOffsetX - dimensions.getScrollPanelX();
        dataViewOffsetY = scrollOffsetY - dimensions.getScrollPanelY();

        Rect mainAreaRectangle = dimensions.getMainAreaRectangle();
        scrollPanel.layout(
                mainAreaRectangle.left,
                mainAreaRectangle.top,
                mainAreaRectangle.right,
                mainAreaRectangle.bottom);

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        backgroundPaintMode = ((BackgroundPaintCapable) codeArea).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
        minRowPositionLength = ((RowWrappingCapable) codeArea).getMinRowPositionLength();
        maxRowPositionLength = ((RowWrappingCapable) codeArea).getMaxRowPositionLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        updateScrollBars();
        layoutChanged = false;
    }

    private void updateCaret() {
        editOperation = ((EditModeCapable) codeArea).getActiveOperation();

        caretChanged = false;
    }

    private void validateCaret() {
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCodeAreaCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() > codeArea.getDataSize()) {
            caret.setCaretPosition(null);
        }
    }

    private void validateSelection() {
        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
        if (!selectionHandler.isEmpty()) {
            long dataSize = codeArea.getDataSize();
            if (dataSize == 0) {
                ((SelectionCapable) codeArea).clearSelection();
            } else {
                boolean selectionChanged = false;
                long start = selectionHandler.getStart();
                long end = selectionHandler.getEnd();
                if (start >= dataSize) {
                    start = dataSize;
                    selectionChanged = true;
                }
                if (end >= dataSize) {
                    end = dataSize;
                    selectionChanged = true;
                }

                if (selectionChanged) {
                    ((SelectionCapable) codeArea).setSelection(start, end);
                }
            }
        }
    }

    private void recomputeDimensions() {
        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        int componentWidth = codeArea.getWidth();
        int componentHeight = codeArea.getHeight();
        dimensions.recomputeSizes(metrics, 0, 0, componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
    }

    public void recomputeCharPositions() {
        visibility.recomputeCharPositions(metrics, structure, dimensions, layout, scrolling);
        updateRowDataCache();
    }

    private void updateRowDataCache() {
        if (rowDataCache == null) {
            rowDataCache = new RowDataCache();
        }

        rowDataCache.headerChars = new char[visibility.getCharactersPerCodeSection()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + metrics.getMaxBytesPerChar() - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[structure.getCharactersPerRow()];
    }

    public void fontChanged() {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) codeArea).getCharset();
        font = ((FontCapable) codeArea).getCodeFont();
        paint = new Paint();
        if (font != null) {
            paint.setTextSize(font.getSize());
        } else {
            paint.setTextSize(30);
        }
        metrics.recomputeMetrics(paint, charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;
    }

    private void recomputeScrollState() {
        scrolling.setScrollPosition(((ScrollingCapable) codeArea).getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();

        if (characterWidth > 0) {
            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            recomputeCharPositions();
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void paintComponent(Canvas g) {
        if (!initialized) {
            reset();
        }

        updateCache();

        if (font == null) {
            fontChanged();
            fontChanged = false;
        }
        if (rowDataCache == null) {
            return;
        }
        if (layoutChanged) {
            recomputeLayout();
        }

        paintOutsideArea(g);
        paintHeader(g);
        paintRowPosition(g);
//        paintMainArea(g);
        scrollPanel.invalidate();
        dataView.invalidate();
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            colorsProfile.reinitialize();
        }
    }

    public void paintOutsideArea(Canvas g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rect componentRect = dimensions.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        paint.setColor(colorsProfile.getTextBackground());
        g.drawRect(componentRect.left, componentRect.top, componentRect.width(), headerAreaHeight, paint);

        // Decoration lines
        paint.setColor(colorsProfile.getDecorationLine());
        g.drawLine(0, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1, paint);

        {
            int lineX = rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= 0) {
                g.drawLine(lineX, 0, lineX, headerAreaHeight, paint);
            }
        }
    }

    public void paintHeader(Canvas g) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        g.save();
        Rect clipBounds = g.getClipBounds();
        Rect headerArea = dimensions.getHeaderAreaRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(headerArea, clipBounds) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();
//        int headerAreaHeight = dimensions.getHeaderAreaHeight();
//        int componentWidth = dimensions.getComponentWidth();
        paint.setColor(colorsProfile.getTextBackground());
        g.drawRect(headerArea.left, headerArea.top, headerArea.right, headerArea.bottom, paint);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = rowHeight - metrics.getSubFontSpace();

            paint.setColor(colorsProfile.getTextColor());
            Arrays.fill(rowDataCache.headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            for (int index = skipToCode; index < skipRestFromCode; index++) {
                int codePos = structure.computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int skipToChar = visibility.getSkipToChar();
            int skipRestFromChar = visibility.getSkipRestFromChar();
            int codeCharEnd = Math.min(skipRestFromChar, visibility.getCharactersPerCodeSection());
            int renderOffset = skipToChar;
            Integer renderColor = null;
            for (int characterOnRow = skipToChar; characterOnRow < codeCharEnd; characterOnRow++) {
                boolean sequenceBreak = false;

                char currentChar = rowDataCache.headerChars[characterOnRow];
                if (currentChar == ' ' && renderOffset == characterOnRow) {
                    renderOffset++;
                    continue;
                }

                Integer color = colorsProfile.getTextColor();

                if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        paint.setColor(color);
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }

        // Decoration lines
        paint.setColor(colorsProfile.getDecorationLine());
        g.drawLine(headerArea.left, headerArea.top + headerArea.height() - 1, headerArea.left + headerArea.width(), headerArea.top + headerArea.height() - 1, paint);
        int lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, headerArea.top, lineX, headerArea.top + headerArea.height(), paint);
        }

        g.restore();
    }

    public void paintRowPosition(Canvas g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        g.save();
        Rect dataViewRectangle = dimensions.getDataViewRectangle();
        Rect clipBounds = g.getClipBounds();
        Rect rowPositionsArea = dimensions.getRowPositionAreaRectangle();
        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(rowPositionsArea, clipBounds) : rowPositionsArea);

        paint.setColor(colorsProfile.getTextBackground());
        g.drawRect(rowPositionsArea.left, rowPositionsArea.top, rowPositionsArea.right, rowPositionsArea.bottom, paint);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            paint.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.drawRect(0, stripePositionY, rowPositionAreaWidth, stripePositionY + rowHeight, paint);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        int positionY = headerAreaHeight + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        paint.setColor(colorsProfile.getTextColor());
        Rectangle compRect = new Rectangle();
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, structure.getCodeType().getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
//            if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
//            } else {
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, compRect.x, positionY);
//            }

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        paint.setColor(colorsProfile.getDecorationLine());
        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, dataViewRectangle.top, lineX, dataViewRectangle.top + dataViewRectangle.height(), paint);
        }
        g.drawLine(dataViewRectangle.left, dataViewRectangle.top - 1, dataViewRectangle.left + dataViewRectangle.width(), dataViewRectangle.top - 1, paint);

        g.restore();
    }

    @Override
    public void paintMainArea(Canvas g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            fontChanged();
            fontChanged = false;
        }

        g.save();
//        Rect clipBounds = g.getClipBounds();
//        Rect mainArea = dimensions.getMainAreaRectangle();
//        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(mainArea, clipBounds) : mainArea);
        colorAssessor.startPaint(this);
        charAssessor.startPaint(this);

        paintBackground(g);

        Rect dataViewRectangle = dimensions.getDataViewRectangle();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        paint.setColor(colorsProfile.getDecorationLine());
        int lineX = dataViewRectangle.left + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2;
        if (lineX >= dataViewRectangle.left) {
            g.drawLine(dataViewOffsetX + lineX, dataViewOffsetY + dataViewRectangle.top, dataViewOffsetX + lineX, dataViewOffsetY + dataViewRectangle.top + dataViewRectangle.height(), paint);
        }

        paintRows(g);
        g.restore();

        paintCursor(g);

//        paintDebugInfo(g, dataViewRectangle, scrollPosition);
    }

    // Debugging counter
    private long paintDebugCounter = 0;

    private void paintDebugInfo(Canvas g, Rect dataViewRectangle, CodeAreaScrollPosition scrollPosition) {
        Rect componentRect = dimensions.getComponentRectangle();
        int rowHeight = metrics.getRowHeight();
        int x = componentRect.width() - dataViewRectangle.left - 500;
        int y = componentRect.height() - dataViewRectangle.top - 200;
        paint.setColor(Color.YELLOW);
        g.drawRect(dataViewOffsetX + x, dataViewOffsetY + y, dataViewOffsetX + x + 400, dataViewOffsetY + y + rowHeight, paint);
        paint.setColor(Color.BLACK);
        char[] headerCode = (scrollPosition.getCharPosition() + "+" + scrollPosition.getCharOffset() + " : " + scrollPosition.getRowPosition() + "+" + scrollPosition.getRowOffset() + " P: " + paintDebugCounter).toCharArray();
        g.drawText(headerCode, 0, headerCode.length, dataViewOffsetX + x, dataViewOffsetY + y + rowHeight, paint);
        paintDebugCounter++;
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(Canvas g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int rowsPerRect = dimensions.getRowsPerRect();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowPositionX = rowPositionAreaWidth;
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        paint.setColor(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.drawRect(dataViewOffsetX + rowPositionX, dataViewOffsetY + headerAreaHeight, dataViewOffsetX + rowPositionX + dataViewWidth, dataViewOffsetY + headerAreaHeight + dataViewHeight, paint);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight - scrollPosition.getRowOffset() + (int) ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            paint.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.drawRect(dataViewOffsetX + rowPositionX, dataViewOffsetY + stripePositionY, dataViewOffsetX + rowPositionX + dataViewWidth, dataViewOffsetY + stripePositionY + rowHeight, paint);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(Canvas g) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
        int rowsPerRect = dimensions.getRowsPerRect();
        long dataSize = codeArea.getDataSize();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = dataViewY - scrollPosition.getRowOffset();
        paint.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }
            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            dataPosition += bytesPerRow;
        }
    }

    private void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = structure.getDataSize();
        int previewCharPos = visibility.getPreviewCharPos();
        CodeType codeType = structure.getCodeType();
        int rowBytesLimit = bytesPerRow;
        int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxBytesPerChar - 1;
            if (dataPosition + rowDataSize > dataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                throw new IllegalStateException("Missing data on nonzero data size");
            }
            data.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
            if (dataPosition + rowBytesLimit > dataSize) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            int endCode = Math.min(skipRestFromCode, rowBytesLimit);
            for (int byteOnRow = Math.max(skipToCode, rowStart); byteOnRow < endCode; byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];

                int byteRowPos = structure.computeFirstCodeCharacterPos(byteOnRow);
                if (byteRowPos > 0) {
                    rowDataCache.rowCharacters[byteRowPos - 1] = ' ';
                }
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, byteRowPos, codeCharactersCase);
            }

            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
            }
        }

        if (previewCharPos > 0) {
            rowDataCache.rowCharacters[previewCharPos - 1] = ' ';
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int skipToPreview = visibility.getSkipToPreview();
            int skipRestFromPreview = visibility.getSkipRestFromPreview();
            int endPreview = Math.min(skipRestFromPreview, rowBytesLimit);
            for (int byteOnRow = skipToPreview; byteOnRow < endPreview; byteOnRow++) {
                rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charAssessor.getPreviewCharacter(dataPosition, byteOnRow, previewCharPos, BasicCodeAreaSection.TEXT_PREVIEW);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, previewCharPos + rowBytesLimit, previewCharPos + bytesPerRow, ' ');
            }
        }
    }

    /**
     * Paints row background.
     *
     * @param g               graphics
     * @param rowDataPosition row data position
     * @param rowPositionX    row position X
     * @param rowPositionY    row position Y
     */
    public void paintRowBackground(Canvas g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();
        int rowHeight = metrics.getRowHeight();
        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();

        int positionY = rowPositionY + rowHeight;

        int renderOffset = skipToChar;
        Integer renderColor = null;
        for (int charOnRow = skipToChar; charOnRow < skipRestFromChar; charOnRow++) {
            CodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
            Integer color = colorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
            if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, positionY);
                    }
                }

                if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        paint.setColor(color);
                    }
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, positionY);
            }
        }
    }

    @Nonnull
    @Override
    public PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computePositionScrollVisibility(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaScrollPosition> computeRevealScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        int charactersPerRect = dimensions.getCharactersPerRect();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerRect, charactersPerRect, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    /**
     * Paints row text.
     *
     * @param g               graphics
     * @param rowDataPosition row data position
     * @param rowPositionX    row position X
     * @param rowPositionY    row position Y
     */
    public void paintRowText(Canvas g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();

        int positionY = rowPositionY + rowHeight - subFontSpace;

        Integer lastColor = null;
        Integer renderColor = null;

        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();
        int renderOffset = skipToChar;
        for (int charOnRow = skipToChar; charOnRow < skipRestFromChar; charOnRow++) {
            CodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX;
            }

            boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
            Integer color = colorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
            if (color == null) {
                color = colorsProfile.getTextColor();
            }

            boolean sequenceBreak = false;
            if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaAndroidUtils.areSameColors(lastColor, renderColor)) {
                    paint.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, dataViewOffsetX + rowPositionX + renderOffset * characterWidth, dataViewOffsetY + positionY);
                }

                renderColor = color;
                if (!CodeAreaAndroidUtils.areSameColors(lastColor, renderColor)) {
                    paint.setColor(renderColor);
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaAndroidUtils.areSameColors(lastColor, renderColor)) {
                paint.setColor(renderColor);
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, dataViewOffsetX + rowPositionX + renderOffset * characterWidth, dataViewOffsetY + positionY);
        }
    }

    @Nonnull
    @Override
    public CodeAreaColorAssessor getColorAssessor() {
        return colorAssessor;
    }

    @Override
    public void setColorAssessor(CodeAreaColorAssessor colorAssessor) {
        this.colorAssessor = CodeAreaUtils.requireNonNull(colorAssessor);
    }

    @Nonnull
    @Override
    public CodeAreaCharAssessor getCharAssessor() {
        return charAssessor;
    }

    @Override
    public void setCharAssessor(CodeAreaCharAssessor charAssessor) {
        this.charAssessor = charAssessor;
    }

    @Override
    public void paintCursor(Canvas g) {
//        if (!codeArea.hasFocus()) {
//            return;
//        }

        if (caretChanged) {
            updateCaret();
        }

        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        if (cursorDataCache == null) {
            cursorDataCache = new CursorDataCache();
        }
        int cursorCharsLength = codeType.getMaxDigitsForByte();
        if (cursorDataCache.cursorCharsLength != cursorCharsLength) {
            cursorDataCache.cursorCharsLength = cursorCharsLength;
            cursorDataCache.cursorChars = new char[cursorCharsLength];
        }
        int cursorDataLength = maxBytesPerChar;
        if (cursorDataCache.cursorDataLength != cursorDataLength) {
            cursorDataCache.cursorDataLength = cursorDataLength;
            cursorDataCache.cursorData = new byte[cursorDataLength];
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
        Rect cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rect scrolledCursorRect = new Rect(cursorRect.left + dataViewOffsetX, cursorRect.top + dataViewOffsetY, cursorRect.right + dataViewOffsetX, cursorRect.bottom + dataViewOffsetY);

        g.save();
        Rect clipBounds = g.getClipBounds();
        Rect mainAreaRect = dimensions.getMainAreaRectangle();
        Rect mainAreaRectAdj = new Rect(mainAreaRect.left + dataViewOffsetX, mainAreaRect.top + dataViewOffsetY, mainAreaRect.right + dataViewOffsetX, mainAreaRect.bottom + dataViewOffsetY);

        Rect intersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRectAdj, scrolledCursorRect);
        boolean cursorVisible = caret.isCursorVisible() && (intersection == null || !intersection.isEmpty());

        if (cursorVisible) {
            if (intersection == null) {
                intersection = scrolledCursorRect;
            }
            g.clipRect(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            paint.setColor(colorsProfile.getCursorColor());
            paintCursorRect(g, intersection, scrolledCursorRect, cursorRect, renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            updateMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            Rect mirrorCursorRect = cursorDataCache.mirrorCursorRect;
            if (mirrorCursorRect != null) {
                Rect mirrorCursorRectAdj = new Rect(mirrorCursorRect.left + dataViewOffsetX, mirrorCursorRect.top + dataViewOffsetY, mirrorCursorRect.right + dataViewOffsetX, mirrorCursorRect.bottom + dataViewOffsetY);
                intersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRectAdj, mirrorCursorRectAdj);
                boolean mirrorCursorVisible = intersection != null && !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.restore();
                    g.save();
                    g.clipRect(intersection);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(colorsProfile.getCursorColor());
                    paint.setPathEffect(cursorDataCache.dashedStroke);
                    g.drawRect(mirrorCursorRectAdj.left, mirrorCursorRectAdj.top, mirrorCursorRectAdj.right - 1, mirrorCursorRectAdj.bottom - 1, paint);
                    paint.setPathEffect(null);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                }
            }
        }
        g.restore();
    }

    private void paintCursorRect(Canvas g, Rect intersection, Rect scrolledCursorRect, Rect cursorRect, DefaultCodeAreaCaret.CursorRenderingMode renderingMode, DefaultCodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.drawRect(intersection, paint);
                break;
            }
            case XOR: {
//                    g.setXORMode(colorsProfile.getBackground();
//                    g.drawRect(intersection.x, intersection.y, intersection.width, intersection.height, paint);
//                    g.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int maxBytesPerChar = metrics.getMaxBytesPerChar();
                int subFontSpace = metrics.getSubFontSpace();
                int dataViewX = dimensions.getScrollPanelX();
                int dataViewY = dimensions.getScrollPanelY();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = structure.getDataSize();
                CodeType codeType = structure.getCodeType();

                g.drawRect(scrolledCursorRect, paint);
                paint.setColor(colorsProfile.getCursorNegativeColor());
                BinaryData contentData = codeArea.getContentData();
                int row = (cursorRect.top + scrollPosition.getRowOffset() - dataViewY) / rowHeight;
                int scrolledX = cursorRect.left + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    int byteOnRow = (int) (dataPosition % structure.getBytesPerRow());
                    int previewCharPos = visibility.getPreviewCharPos();

                    if (contentData.isEmpty()) {
                        cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 0, BasicCodeAreaSection.TEXT_PREVIEW);
                    } else {
                        if (maxBytesPerChar > 1) {
                            int charDataLength = maxBytesPerChar;
                            if (dataPosition + maxBytesPerChar > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, charDataLength, BasicCodeAreaSection.TEXT_PREVIEW);
                        } else {
                            cursorDataCache.cursorData[0] = contentData.getByte(dataPosition);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 1, BasicCodeAreaSection.TEXT_PREVIEW);
                        }
                    }
                    int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    drawCenteredChars(g, cursorDataCache.cursorChars, 0, 1, characterWidth, dataViewOffsetX + posX, dataViewOffsetY + posY);
//                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                            g.drawChars(previewChars, 0, 1, posX, posY);
//                        } else {
//                        }
                } else {
                    int charPos = (scrolledX - dataViewX) / characterWidth;
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, codeCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(g, cursorDataCache.cursorChars, charsOffset, 1, characterWidth, dataViewOffsetX + posX + (charsOffset * characterWidth), dataViewOffsetY + posY);
                }
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(renderingMode);
        }
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        DefaultCodeAreaCaretPosition caret = new DefaultCodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffX = 1;
            }
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getCharOffset()) / characterWidth + scrollPosition.getCharPosition() - diffX;
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        int diffY = 0;
        if (positionY < headerAreaHeight) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffY = 1;
            }
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition() - diffY;
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        int previewCharPos = visibility.getPreviewCharPos();
        int bytesPerRow = structure.getBytesPerRow();
        CodeType codeType = structure.getCodeType();
        long dataSize = structure.getDataSize();
        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(BasicCodeAreaSection.CODE_MATRIX);
            byteOnRow = structure.computePositionByte(cursorCharX);
            if (byteOnRow >= bytesPerRow) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - structure.computeFirstCodeCharacterPos(byteOnRow);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(BasicCodeAreaSection.TEXT_PREVIEW);
            byteOnRow = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnRow -= previewCharPos;
            }
        }

        if (byteOnRow >= bytesPerRow) {
            byteOnRow = bytesPerRow - 1;
        }

        dataPosition = byteOnRow + (cursorRowY * bytesPerRow);
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        caret.setDataPosition(dataPosition);
        caret.setCodeOffset(codeOffset);
        return caret;
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return structure.computeMovePosition(position, direction, dimensions.getRowsPerPage());
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction) {
        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        return scrolling.computeScrolling(startPosition, direction, rowsPerPage, rowsPerDocument);
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param dataPosition data position
     * @param codeOffset   code offset
     * @param section      section
     * @return cursor position or null
     */
    @Nullable
    public Point getPositionPoint(long dataPosition, int codeOffset, CodeAreaSection section) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowsPerRect = dimensions.getRowsPerRect();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rect dataViewRect = dimensions.getDataViewRectangle();
        int caretY = (int) (dataViewRect.top + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.left + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.left + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point(caretX, caretY);
    }

    private void updateMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            cursorDataCache.mirrorCursorRect.setEmpty();
        } else {
            cursorDataCache.mirrorCursorRect = new Rect(mirrorCursorPoint.x, mirrorCursorPoint.y, mirrorCursorPoint.x + metrics.getCharacterWidth() * (section == BasicCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), mirrorCursorPoint.y + metrics.getRowHeight());
        }
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
        int scrollPanelWidth = dimensions.getScrollPanelWidth();
        int scrollPanelHeight = dimensions.getScrollPanelHeight();
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return 1; // Cursor.TEXT_CURSOR;
        }

        return 0; // Cursor.DEFAULT_CURSOR;
    }

    @Nonnull
    @Override
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        return dimensions.getPositionZone(positionX, positionY);
    }

    @Nonnull
    @Override
    public BasicCodeAreaColorsProfile getBasicColors() {
        return colorsProfile;
    }

    @Override
    public void setBasicColors(BasicCodeAreaColorsProfile colors) {
        this.colorsProfile = colors;
    }

    /**
     * Draws characters centering it to cells of the same width.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param length number of characters to draw
     * @param cellWidth width of cell to center into
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChars(Canvas g, char[] drawnChars, int charOffset, int length, int cellWidth, int positionX, int positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            char drawnChar = drawnChars[charOffset + pos];
            int charWidth = metrics.getCharWidth(drawnChar);

            boolean groupable;
            if (metrics.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                int charsWidth = metrics.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
                groupable = charsWidth == cellWidth * (group + 1);
            }

            switch (Character.getDirectionality(drawnChar)) {
                case Character.DIRECTIONALITY_UNDEFINED:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                case Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT:
                case Character.DIRECTIONALITY_BOUNDARY_NEUTRAL:
                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                    groupable = false;
            }

            if (groupable) {
                group++;
            } else {
                if (group > 0) {
                    drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
                    group = 0;
                }
                drawShiftedChars(g, drawnChars, charOffset + pos, 1, positionX + pos * cellWidth + ((cellWidth - charWidth) / 2), positionY);
            }
            pos++;
        }
        if (group > 0) {
            drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
        }
    }

    protected void drawShiftedChars(Canvas g, char[] drawnChars, int charOffset, int length, int positionX, int positionY) {
        g.drawText(drawnChars, charOffset, length, positionX, positionY, paint);
    }

    private int getRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = codeArea.getDataSize();
        if (dataSize == 0) {
            return 1;
        }

        double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
        int positionLength = (int) Math.ceil(natLog / PositionCodeType.HEXADECIMAL.getBaseLog());
        if (minRowPositionLength > 0 && positionLength < minRowPositionLength) {
            positionLength = minRowPositionLength;
        }
        if (maxRowPositionLength > 0 && positionLength > maxRowPositionLength) {
            positionLength = maxRowPositionLength;
        }

        return positionLength == 0 ? 1 : positionLength;
    }

    /**
     * Returns cursor rectangle.
     *
     * @param dataPosition data position
     * @param codeOffset   code offset
     * @param section      section
     * @return cursor rectangle or null
     */
    @Nullable
    public Rect getPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            return null;
        }

        DefaultCodeAreaCaret.CursorShape cursorShape = editOperation == EditOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
        return new Rect(cursorPoint.x, cursorPoint.y, cursorPoint.x + cursorThickness, cursorPoint.y + rowHeight);
    }

    /**
     * Renders sequence of background rectangles.
     * <p>
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Canvas g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.drawRect(dataViewOffsetX + rowPositionX + startOffset * characterWidth, dataViewOffsetY + 1 + positionY - rowHeight, dataViewOffsetX + rowPositionX + endOffset * characterWidth, dataViewOffsetY + 1 + positionY, paint);
    }

    @Override
    public void updateScrollBars() {
//        int verticalScrollBarPolicy = CodeAreaAndroidUtils.getVerticalScrollBarPolicy(scrolling.getVerticalScrollBarVisibility());
//        if (scrollPanel.getVerticalScrollBarPolicy() != verticalScrollBarPolicy) {
//            scrollPanel.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
//        }
//        int horizontalScrollBarPolicy = CodeAreaSwingUtils.getHorizontalScrollBarPolicy(scrolling.getHorizontalScrollBarVisibility());
//        if (scrollPanel.getHorizontalScrollBarPolicy() != horizontalScrollBarPolicy) {
//            scrollPanel.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
//        }

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        long rowsPerDocument = structure.getRowsPerDocument();

        recomputeScrollState();

        adjusting = true;
        int verticalScrollBar = scrollPanel.getVerticalScrollbarPosition();
        // scrollPanel.setVerticalScrollBarEnabled(CodeAreaAndroidUtils.getVerticalScrollBarPolicy(scrolling.getVerticalScrollBarVisibility()));
        // int horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        // scrollPanel.setHorizontalScrollBarPolicy(CodeAreaAndroidUtils.getHorizontalScrollBarPolicy(scrolling.getHorizontalScrollBarVisibility()));

        int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
        // scrollPanel.setVerticalScrollbarPosition(verticalScrollValue);

        int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
        // scrollPanel.setHorizontalScrollBar.setValue(horizontalScrollValue);
        scrollPanel.updateScrollBars(verticalScrollValue, horizontalScrollValue);

        adjusting = false;

        scrolling.setScrollPosition(((ScrollingCapable) codeArea).getScrollPosition());

        if (characterWidth > 0) {
            recomputeCharPositions();
        }

        Rect scrollPanelRect = dimensions.getScrollPanelRectangle();
//        dataView.layout(scrollPanelRect.left, scrollPanelRect.top, scrollPanelRect.right, scrollPanelRect.bottom);
        dataView.layout(0, 0, scrollPanelRect.width(), scrollPanelRect.height());

        if (rowHeight > 0 && characterWidth > 0) {
            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());
            ScrollViewDimension viewDimension = scrolling.computeViewDimension(codeArea.getWidth(), codeArea.getHeight(), layout, structure, characterWidth, rowHeight);
            dataView.setMinimumWidth(viewDimension.getWidth());
            dataView.setMinimumHeight(viewDimension.getHeight());

            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            int documentDataWidth = structure.getCharactersPerRow() * characterWidth;
            long rowsPerData = (structure.getDataSize() / structure.getBytesPerRow()) + 1;

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.SCALED);
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.NORMAL);
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

            recomputeDimensions();
            scrollPanelRect = dimensions.getScrollPanelRectangle();
            // dataView.layout(scrollPanelRect.left, scrollPanelRect.top, scrollPanelRect.right, scrollPanelRect.bottom);
            // dataView.layout(0, 0, scrollPanelRect.width(), scrollPanelRect.height());
            dataView.layout(0, 0, documentDataWidth, documentDataHeight);
            dataView.setMinimumWidth(documentDataWidth);
            dataView.setMinimumHeight(documentDataHeight);
        }

        // TODO on resize only
        final Rect scrollPanelRectangle = dimensions.getScrollPanelRectangle();
        Activity activity = (Activity) codeArea.getContext();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(scrollPanelRectangle.width(), scrollPanelRectangle.height());
                layoutParams.setMargins(scrollPanelRectangle.left, scrollPanelRectangle.top, 0, 0);
                scrollPanel.setLayoutParams(layoutParams);
                scrollPanel.invalidate();
            }
        });
    }

    @Override
    public void scrollPositionModified() {
        scrolling.clearLastVerticalScrollingValue();
        recomputeScrollState();
    }

    @Override
    public void scrollPositionChanged() {
        recomputeScrollState();
        updateScrollBars();
    }

    private void dataChanged() {
        validateCaret();
        validateSelection();
        recomputeLayout();
    }

    @Override
    public int getCharactersPerRow() {
        return structure.getCharactersPerRow();
    }

    @Override
    public int getBytesPerRow() {
        return structure.getBytesPerRow();
    }

    @Nonnull
    @Override
    public CodeAreaSection getActiveSection() {
        return ((CaretCapable) codeArea).getActiveSection();
    }

    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public int getMaxBytesPerChar() {
        return metrics.getMaxBytesPerChar();
    }

    @Nonnull
    @Override
    public CodeAreaColorsProfile getColorsProfile() {
        return colorsProfile;
    }

    @Nonnull
    @Override
    public byte[] getRowData() {
        return rowDataCache.rowData;
    }

    @Override
    public int getCodeLastCharPos() {
        return visibility.getCodeLastCharPos();
    }

    @Override
    public long getDataSize() {
        return structure.getDataSize();
    }

    @Nonnull
    @Override
    public BinaryData getContentData() {
        return codeArea.getContentData();
    }

    @Nullable
    @Override
    public CodeAreaSelection getSelectionHandler() {
        return ((SelectionCapable) codeArea).getSelectionHandler();
    }

    private int getHorizontalScrollBarSize() {
//        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
//        int size;
//        if (horizontalScrollBar.isVisible()) {
//            size = horizontalScrollBar.getHeight();
//        } else {
//            size = 0;
//        }
//
//        return size;
        return 10;
    }

    private int getVerticalScrollBarSize() {
//        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
//        int size;
//        if (verticalScrollBar.isVisible()) {
//            size = verticalScrollBar.getWidth();
//        } else {
//            size = 0;
//        }
//
//        return size;
        return 10;
    }

    private static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }

    private static class CursorDataCache {

        Rect caretRect = new Rect();
        Rect mirrorCursorRect = new Rect();
        final DashPathEffect dashedStroke = new DashPathEffect(new float[] {4f, 4f}, 0f);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }
}
