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

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.constraint.solver.widgets.Rectangle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.examples.customtouch.widget.TwoDimensionScrollView;

import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.Font;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.android.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.android.capability.BackgroundPaintCapable;
import org.exbin.bined.android.capability.FontCapable;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaLayout;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.utils.binary_data.BinaryData;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Code area component default painter.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2018/09/18
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaPainter implements CodeAreaPainter, BasicColorsCapableCodeAreaPainter {

    @Nonnull
    protected final CodeArea codeArea;
    private volatile boolean initialized = false;
    private volatile boolean adjusting = false;

    private volatile boolean fontChanged = false;
    private volatile boolean layoutChanged = true;
    private volatile boolean resetColors = true;
    private volatile boolean caretChanged = true;

    @Nullable
    private Paint paint = null;
    @Nonnull
    private final View dataView;
    @Nonnull
    private final TwoDimensionScrollView scrollPanel;

    private int dataViewOffsetX = 0;
    private int dataViewOffsetY = 0;
    private int scrollOffsetX = 0;
    private int scrollOffsetY = 0;

    @Nonnull
    private final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    @Nonnull
    private final BasicCodeAreaStructure structure = new BasicCodeAreaStructure();
    @Nonnull
    private final BasicCodeAreaScrolling scrolling = new BasicCodeAreaScrolling();
    @Nonnull
    private final BasicCodeAreaDimensions dimensions = new BasicCodeAreaDimensions();
    @Nonnull
    private final BasicCodeAreaVisibility visibility = new BasicCodeAreaVisibility();
    @Nonnull
    private volatile ScrollingState scrollingState = ScrollingState.NO_SCROLLING;

    @Nonnull
    private final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    private BasicCodeAreaColorsProfile colorsProfile = new BasicCodeAreaColorsProfile();

    @Nullable
    private CodeCharactersCase codeCharactersCase;
    @Nullable
    private EditationOperation editationOperation;
    @Nullable
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;

    private int rowPositionLength;
    private int minRowPositionLength;
    private int maxRowPositionLength;

    @Nullable
    private Font font;
    @Nonnull
    private Charset charset;

    @Nullable
    private RowDataCache rowDataCache = null;
    @Nullable
    private CursorDataCache cursorDataCache = null;

    @Nullable
    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];
    // Debug
    private long paintCounter = 0;

    public DefaultCodeAreaPainter(final CodeArea codeArea) {
        this.codeArea = codeArea;
        codeArea.addDataChangedListener(new DataChangedListener() {
            @Override
            public void dataChanged() {
                validateCaret();
                recomputeLayout();
            }
        });

        scrollPanel = new TwoDimensionScrollView(codeArea.getContext()) {
            @Override
            protected void onScrollChanged(int horizontal, int vertical, int oldHorizontal, int oldVertical) {
                super.onScrollChanged(horizontal, vertical, oldHorizontal, oldVertical);

                scrolling.updateHorizontalScrollBarValue(horizontal, metrics.getCharacterWidth());

                int maxValue = Integer.MAX_VALUE; // - scrollPanel.getVerticalScrollBar().getVisibleAmount();
                long rowsPerDocumentToLastPage = structure.getRowsPerDocument() - dimensions.getRowsPerRect();
                scrolling.updateVerticalScrollBarValue(vertical, metrics.getRowHeight(), maxValue, rowsPerDocumentToLastPage);

                scrollOffsetX = horizontal;
                scrollOffsetY = vertical;
                dataViewOffsetX = scrollOffsetX - dimensions.getDataViewX();
                dataViewOffsetY = scrollOffsetY - dimensions.getDataViewY();

                ((ScrollingCapable) codeArea).setScrollPosition(scrolling.getScrollPosition());
                notifyScrolled();
                codeArea.repaint();
            }
        };

        dataView = new View(DefaultCodeAreaPainter.this.codeArea.getContext()) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                paintMainArea(canvas);
            }
        };
        scrollPanel.addView(dataView);

        scrollPanel.setScrollContainer(true);

        //        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
//        verticalScrollBar.setIgnoreRepaint(true);
//        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListener());
//        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
//        horizontalScrollBar.setIgnoreRepaint(true);
//        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListener());
//        codeArea.add(scrollPanel);
//        scrollPanel.setOpaque(false);
//        scrollPanel.setViewportView(dataView);
//        scrollPanel.setViewportBorder(null);
//        scrollPanel.getViewport().setOpaque(false);

//        DefaultCodeAreaMouseListener codeAreaMouseListener = new DefaultCodeAreaMouseListener(codeArea, scrollPanel);
//        codeArea.addMouseListener(codeAreaMouseListener);
//        codeArea.addMouseMotionListener(codeAreaMouseListener);
//        codeArea.addMouseWheelListener(codeAreaMouseListener);
//        scrollPanel.addMouseListener(codeAreaMouseListener);
//        scrollPanel.addMouseMotionListener(codeAreaMouseListener);
//        scrollPanel.addMouseWheelListener(codeAreaMouseListener);
    }

    @Override
    public void attach() {
        RelativeLayout.LayoutParams wrapLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.codeArea.addView(scrollPanel, wrapLayout);
    }

    @Override
    public void detach() {
        this.codeArea.removeView(scrollPanel);
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
        dataViewOffsetX = scrollOffsetX - dimensions.getDataViewX();
        dataViewOffsetY = scrollOffsetY - dimensions.getDataViewY();

        scrollPanel.layout(
                dimensions.getRowPositionAreaWidth(),
                dimensions.getHeaderAreaHeight(),
                dimensions.getRowPositionAreaWidth() + dimensions.getDataViewWidth(),
                dimensions.getHeaderAreaHeight() + dimensions.getDataViewHeight());

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
        backgroundPaintMode = ((BackgroundPaintCapable) codeArea).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) codeArea).isShowMirrorCursor();
//        antialiasingMode = ((AntialiasingCapable) codeArea).getAntialiasingMode();
        minRowPositionLength = ((RowWrappingCapable) codeArea).getMinRowPositionLength();
        maxRowPositionLength = ((RowWrappingCapable) codeArea).getMaxRowPositionLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        recomputeScrollState();
        layoutChanged = false;
    }

    private void updateCaret() {
        editationOperation = ((EditationModeCapable) codeArea).getActiveOperation();

        caretChanged = false;
    }

    private void validateCaret() {
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() > codeArea.getDataSize()) {
            caret.setCaretPosition(null);
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

        int maxRowDataChars = visibility.getMaxRowDataChars();
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
        paint.setTextSize(30);
        metrics.recomputeMetrics(paint, charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;

//        int verticalScrollBarSize = getVerticalScrollBarSize();
//        int horizontalScrollBarSize = getHorizontalScrollBarSize();
//        dimensions.recomputeSizes(metrics, codeArea.getWidth(), codeArea.getHeight(), rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
//        dataViewOffsetX = scrollOffsetX - dimensions.getDataViewX();
//        dataViewOffsetY = scrollOffsetY - dimensions.getDataViewY();
//
//        resetCharPositions();
//        initialized = true;
    }

    public void dataViewScrolled(Canvas g) {
        if (!isInitialized()) {
            return;
        }

        recomputeScrollState();
        if (metrics.getCharacterWidth() > 0) {
            recomputeCharPositions();
            paintComponent(g);
        }
    }

    private void recomputeScrollState() {
        scrolling.setScrollPosition(((ScrollingCapable) codeArea).getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        if (characterWidth > 0) {
            recomputeCharPositions();
        }

        Rect scrollPanelRect = dimensions.getScrollPanelRect();
//        dataView.layout(scrollPanelRect.left, scrollPanelRect.top, scrollPanelRect.right, scrollPanelRect.bottom);
        dataView.layout(0, 0, scrollPanelRect.width(), scrollPanelRect.height());

        if (rowHeight > 0 && characterWidth > 0) {
            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());
            BasicCodeAreaScrolling.ScrollViewDimension viewDimension = scrolling.computeViewDimension(codeArea.getWidth(), codeArea.getHeight(), layout, structure, characterWidth, rowHeight);
            dataView.setMinimumWidth(viewDimension.getWidth());
            dataView.setMinimumHeight(viewDimension.getHeight());

//            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

//            int documentDataWidth = structure.getCharactersPerRow() * characterWidth;
//            long rowsPerData = (structure.getDataSize() / structure.getBytesPerRow()) + 1;
//
//            int documentDataHeight;
//            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
//                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.SCALED);
//                documentDataHeight = Integer.MAX_VALUE;
//            } else {
//                scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.NORMAL);
//                documentDataHeight = (int) (rowsPerData * rowHeight);
//            }

            recomputeDimensions();
            scrollPanelRect = dimensions.getScrollPanelRect();
            //dataView.layout(scrollPanelRect.left, scrollPanelRect.top, scrollPanelRect.right, scrollPanelRect.bottom);
            dataView.layout(0, 0, scrollPanelRect.width(), scrollPanelRect.height());
//            dataView.setMinimumWidth(documentDataWidth);
//            dataView.setMinimumHeight(documentDataHeight);
        }

        // TODO on resize only
        final Rect scrollPanelRectangle = dimensions.getScrollPanelRect();
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
        if (scrollingState == ScrollingState.SCROLLING_BY_SCROLLBAR) {
            return;
        }
        if (layoutChanged) {
            recomputeLayout();
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintRowPosition(g);
//        paintMainArea(g);
        scrollPanel.invalidate();
        dataView.invalidate();
        paintCounter++;
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            colorsProfile.reinitialize();
        }
    }

    public void paintOutsiteArea(Canvas g) {

        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rect componentRect = dimensions.getComponentRect();
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
        Rect headerArea = dimensions.getHeaderAreaRect();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(headerArea, clipBounds) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getDataViewX();
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
        g.drawRect(headerArea.left, headerArea.top + headerArea.height() - 1, headerArea.left + headerArea.width(), headerArea.top + headerArea.height() - 1, paint);
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
        Rect dataViewRectangle = dimensions.getDataViewRect();
        Rect clipBounds = g.getClipBounds();
        Rect rowPositionsArea = dimensions.getRowPositionAreaRect();
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
        }

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
        if (scrollingState == ScrollingState.SCROLLING_BY_SCROLLBAR)
            return;

        g.save();
//        Rect clipBounds = g.getClipBounds();
//        Rect mainArea = dimensions.getMainAreaRect();
//        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(mainArea, clipBounds) : mainArea);
        paintBackground(g);

        Rect dataViewRectangle = dimensions.getDataViewRect();
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

        // TODO: Remove later
        Rect componentRect = dimensions.getComponentRect();
        int rowHeight = metrics.getRowHeight();
        int x = componentRect.width() - dataViewRectangle.left - 500;
        int y = componentRect.height() - dataViewRectangle.top - 200;
        paint.setColor(Color.YELLOW);
        g.drawRect(dataViewOffsetX + x, dataViewOffsetY + y, dataViewOffsetX + x + 400, dataViewOffsetY + y + rowHeight, paint);
        paint.setColor(Color.BLACK);
        char[] headerCode = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawText(headerCode, 0, headerCode.length, dataViewOffsetX + x, dataViewOffsetY + y + rowHeight, paint);
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
        int dataViewX = dimensions.getDataViewX();
        int dataViewY = dimensions.getDataViewY();
        int rowsPerRect = dimensions.getRowsPerRect();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = dataViewY - scrollPosition.getRowOffset();
        paint.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
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
            BinaryData content = codeArea.getContentData();
            if (content == null) {
                throw new IllegalStateException("Missing data on nonzero data size");
            }
            content.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
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
                byte dataByte = rowDataCache.rowData[byteOnRow];

                if (maxBytesPerChar > 1) {
                    if (dataPosition + maxBytesPerChar > dataSize) {
                        maxBytesPerChar = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxBytesPerChar;
                    if (byteOnRow + charDataLength > rowDataCache.rowData.length) {
                        charDataLength = rowDataCache.rowData.length - byteOnRow;
                    }
                    String displayString = new String(rowDataCache.rowData, byteOnRow, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        rowDataCache.rowCharacters[previewCharPos + byteOnRow] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charMapping[dataByte & 0xFF];
                }
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

            Integer color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, dataViewOffsetX + renderOffset, dataViewOffsetY + charOnRow, dataViewOffsetX + rowPositionX, dataViewOffsetY + rowPositionY);
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
                renderBackgroundSequence(g, dataViewOffsetX + renderOffset, dataViewOffsetY + charactersPerRow, dataViewOffsetX + rowPositionX, dataViewOffsetY + rowPositionY);
            }
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow       byte on current row
     * @param charOnRow       character on current row
     * @param section         current section
     * @return color or null for default color
     */
    @Nullable
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        SelectionRange selectionRange = structure.getSelectionRange();
        int codeLastCharPos = visibility.getCodeLastCharPos();
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == caret.getSection() ? colorsProfile.getSelectionBackground() : colorsProfile.getSelectionMirrorBackground();
        }

        return null;
    }

    @Nullable
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computePositionScrollVisibility(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Nullable
    @Override
    public CodeAreaScrollPosition computeRevealScrollPosition(CodeAreaCaretPosition caretPosition) {
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
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
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
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

            Integer color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
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

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow       byte on current row
     * @param charOnRow       character on current row
     * @param section         current section
     * @return color or null for default color
     */
    @Nullable
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        SelectionRange selectionRange = structure.getSelectionRange();
        CodeAreaCaret caret = ((CaretCapable) codeArea).getCaret();
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == caret.getSection() ? colorsProfile.getSelectionColor() : colorsProfile.getSelectionMirrorColor();
        }

        return null;
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

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        Rect cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rect scrolledCursorRect = new Rect(cursorRect.left + dataViewOffsetX, cursorRect.top + dataViewOffsetY, cursorRect.right + dataViewOffsetX, cursorRect.bottom + dataViewOffsetY);

        g.save();
        Rect clipBounds = g.getClipBounds();
        Rect mainAreaRect = dimensions.getMainAreaRect();
        mainAreaRect.left += dataViewOffsetX;
        mainAreaRect.right += dataViewOffsetX;
        mainAreaRect.bottom += dataViewOffsetY;
        mainAreaRect.top += dataViewOffsetY;

        Rect intersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRect, scrolledCursorRect);
        boolean cursorVisible = caret.isCursorVisible() && intersection != null && !intersection.isEmpty();

        if (cursorVisible) {
            g.clipRect(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            paint.setColor(colorsProfile.getCursorColor());
            paintCursorRect(g, intersection, scrolledCursorRect, cursorRect, renderingMode, caret);
        }

//        // Paint mirror cursor
//        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
//            Rect mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
//            if (mirrorCursorRect != null) {
//                intersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRect, mirrorCursorRect);
//                boolean mirrorCursorVisible = !intersection.isEmpty();
//                if (mirrorCursorVisible) {
//                    g.clipRect(intersection);
//                    paint.setColor(colorsProfile.getCursor();
////                    Graphics2D g2d = (Graphics2D) g.create();
////                    Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
////                    g2d.setStroke(dashed);
//                    g.drawRect(mirrorCursorRect.left, mirrorCursorRect.top, mirrorCursorRect.right - 1, mirrorCursorRect.bottom - 1, paint);
//                }
//            }
//        }
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
                int dataViewX = dimensions.getDataViewX();
                int dataViewY = dimensions.getDataViewY();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = structure.getDataSize();
                CodeType codeType = structure.getCodeType();

                g.drawRect(scrolledCursorRect, paint);
                paint.setColor(colorsProfile.getCursorNegativeColor());
                BinaryData codeAreaData = codeArea.getContentData();
                int row = (cursorRect.top + scrollPosition.getRowOffset() - dataViewY) / rowHeight;
                int scrolledX = cursorRect.left + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    if (maxBytesPerChar > 1) {
                        int charDataLength = maxBytesPerChar;
                        if (dataPosition + maxBytesPerChar > dataSize) {
                            charDataLength = (int) (dataSize - dataPosition);
                        }

                        if (codeAreaData == null) {
                            cursorDataCache.cursorChars[0] = ' ';
                        } else {
                            codeAreaData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            String displayString = new String(cursorDataCache.cursorData, 0, charDataLength, charset);
                            if (!displayString.isEmpty()) {
                                cursorDataCache.cursorChars[0] = displayString.charAt(0);
                            }
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        if (codeAreaData == null) {
                            cursorDataCache.cursorChars[0] = ' ';
                        } else {
                            cursorDataCache.cursorChars[0] = charMapping[codeAreaData.getByte(dataPosition) & 0xFF];
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

                    if (codeAreaData != null && dataPosition < dataSize) {
                        byte dataByte = codeAreaData.getByte(dataPosition);
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
                throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
        }
    }

    @Nonnull
    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, PositionOverflowMode overflowMode) {
        DefaultCodeAreaCaretPosition caret = new DefaultCodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
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
            if (overflowMode == PositionOverflowMode.OVERFLOW) {
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

        Rect dataViewRect = dimensions.getDataViewRect();
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

    @Nullable
    private Rect getMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            return null;
        }

        Rect mirrorCursorRect = new Rect(mirrorCursorPoint.x, mirrorCursorPoint.y, mirrorCursorPoint.x + metrics.getCharacterWidth() * (section == BasicCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), mirrorCursorPoint.y + metrics.getRowHeight());
        return mirrorCursorRect;
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
//        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
//                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
//            return Cursor.TEXT_CURSOR;
//        }
//
        return 0; //Cursor.DEFAULT_CURSOR;
    }

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
     * @param length number of charaters to draw
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

    private void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
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

        DefaultCodeAreaCaret.CursorShape cursorShape = editationOperation == EditationOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
        return new Rect(cursorPoint.x, cursorPoint.y, cursorPoint.x + cursorThickness, cursorPoint.y + rowHeight);
    }

    /**
     * Render sequence of background rectangles.
     * <p>
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Canvas g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.drawRect(rowPositionX + startOffset * characterWidth, positionY, rowPositionX + endOffset * characterWidth, positionY + rowHeight, paint);
    }

    @Override
    public void updateScrollBars() {

//        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
//        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
//
//        if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
//            long rowsPerDocument = (dataSize / bytesPerRow) + 1;
//            int scrollValue;
//            if (scrollPosition.getCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
//                scrollValue = (int) ((scrollPosition.getRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
//            } else {
//                scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
//            }
//            verticalScrollBar.setValue(scrollValue);
//        } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
//            verticalScrollBar.setValue((int) scrollPosition.getRowPosition() * rowHeight);
//        } else {
//            verticalScrollBar.setValue((int) (scrollPosition.getRowPosition() * rowHeight + scrollPosition.getRowOffset()));
//        }
//
//        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
//            horizontalScrollBar.setValue(scrollPosition.getCharPosition() * characterWidth);
//        } else {
//            horizontalScrollBar.setValue(scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset());
//        }
    }

    protected int getCharactersPerRow() {
        return structure.getCharactersPerRow();
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

//    private class VerticalAdjustmentListener implements AdjustmentListener {
//
//        public VerticalAdjustmentListener() {
//        }
//
//        @Override
//        public void adjustmentValueChanged(AdjustmentEvent e) {
//            int scrollBarValue = scrollPanel.getVerticalScrollBar().getValue();
//            if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
//                int maxValue = Integer.MAX_VALUE - scrollPanel.getVerticalScrollBar().getVisibleAmount();
//                long rowsPerDocument = (dataSize / bytesPerRow) - computeRowsPerRectangle() + 1;
//                long targetRow;
//                if (scrollBarValue > 0 && rowsPerDocument > maxValue / scrollBarValue) {
//                    targetRow = scrollBarValue * (rowsPerDocument / maxValue);
//                    long rest = rowsPerDocument % maxValue;
//                    targetRow += (rest * scrollBarValue) / maxValue;
//                } else {
//                    targetRow = (scrollBarValue * rowsPerDocument) / Integer.MAX_VALUE;
//                }
//                scrollPosition.setScrollRowPosition(targetRow);
//                if (verticalScrollUnit != VerticalScrollUnit.ROW) {
//                    scrollPosition.setScrollRowOffset(0);
//                }
//            } else {
//                if (rowHeight == 0) {
//                    scrollPosition.setScrollRowPosition(0);
//                    scrollPosition.setScrollRowOffset(0);
//                } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
//                    scrollPosition.setScrollRowPosition(scrollBarValue / rowHeight);
//                    scrollPosition.setScrollRowOffset(0);
//                } else {
//                    scrollPosition.setScrollRowPosition(scrollBarValue / rowHeight);
//                    scrollPosition.setScrollRowOffset(scrollBarValue % rowHeight);
//                }
//            }
//
//            // TODO
//            ((ScrollingCapable) codeArea).setScrollPosition(scrollPosition);
//            codeArea.repaint();
////            dataViewScrolled(codeArea.getGraphics());
//            notifyScrolled();
//        }
//    }
//
//    private class HorizontalAdjustmentListener implements AdjustmentListener {
//
//        public HorizontalAdjustmentListener() {
//        }
//
//        @Override
//        public void adjustmentValueChanged(AdjustmentEvent e) {
//            int scrollBarValue = scrollPanel.getHorizontalScrollBar().getValue();
//            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
//                scrollPosition.setScrollCharPosition(scrollBarValue);
//            } else {
//                if (characterWidth == 0) {
//                    scrollPosition.setScrollCharPosition(0);
//                    scrollPosition.setScrollCharOffset(0);
//                } else if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
//                    scrollPosition.setScrollCharPosition(scrollBarValue / characterWidth);
//                    scrollPosition.setScrollCharOffset(0);
//                } else {
//                    scrollPosition.setScrollCharPosition(scrollBarValue / characterWidth);
//                    scrollPosition.setScrollCharOffset(scrollBarValue % characterWidth);
//                }
//            }
//
//            ((ScrollingCapable) codeArea).setScrollPosition(scrollPosition);
//            notifyScrolled();
//            codeArea.repaint();
////            dataViewScrolled(codeArea.getGraphics());
//        }
//    }

    private void notifyScrolled() {
        recomputeScrollState();
        ((ScrollingCapable) codeArea).notifyScrolled();
        // TODO
    }

    private static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }

    private static class CursorDataCache {

        Rectangle caretRect = new Rectangle();
        Rectangle mirrorCursorRect = new Rectangle();
//        final Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }

    protected enum ScrollingState {
        NO_SCROLLING,
        SCROLLING_BY_SCROLLBAR,
        SCROLLING_BY_MOVEMENT
    }
}
