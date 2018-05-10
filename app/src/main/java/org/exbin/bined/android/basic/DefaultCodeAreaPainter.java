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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.constraint.solver.widgets.Rectangle;
import android.view.View;
import android.view.ViewGroup;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.Font;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.android.CodeArea;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.CodeAreaWorker;
import org.exbin.bined.android.MovementDirection;
import org.exbin.bined.android.ScrollingDirection;
import org.exbin.bined.android.capability.BackgroundPaintCapable;
import org.exbin.bined.android.capability.FontCapable;
import org.exbin.bined.android.capability.ScrollingCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Code area component default painter.
 *
 * @version 0.2.0 2018/05/08
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaPainter implements CodeAreaPainter {

    @Nonnull
    protected final CodeAreaWorker worker;
    private boolean initialized = false;
    private boolean fontChanged = false;

    @Nonnull
    private final View dataView;

    private CodeAreaViewMode viewMode;
    private final CodeAreaCaretPosition caretPosition = new CodeAreaCaretPosition();
    private SelectionRange selectionRange = null;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;

    private VerticalScrollUnit verticalScrollUnit;
    private HorizontalScrollUnit horizontalScrollUnit;
    private final Colors colors = new Colors();
    private long dataSize;

    private int componentWidth;
    private int componentHeight;
    private int dataViewX;
    private int dataViewY;
    private int scrollPanelWidth;
    private int scrollPanelHeight;
    private int dataViewWidth;
    private int dataViewHeight;

    private int rowPositionLength;
    private int rowPositionAreaWidth;
    private int headerAreaHeight;
    private int rowHeight;
    private int rowsPerPage;
    private int rowsPerRect;
    private int bytesPerRow;
    private int charactersPerPage;
    private int charactersPerRect;
    private int charactersPerRow;
    private CodeType codeType;
    private CodeCharactersCase hexCharactersCase;
    private EditationMode editationMode;
    private BasicBackgroundPaintMode backgroundPaintMode;
    private boolean showMirrorCursor;

    private int codeLastCharPos;
    private int previewCharPos;
    private int previewRelativeX;
    private int visibleCharStart;
    private int visibleCharEnd;
    private int visiblePreviewStart;
    private int visiblePreviewEnd;
    private int visibleCodeStart;
    private int visibleCodeEnd;

    @Nonnull
    private Charset charset;
    @Nullable
    private Font font;
    private int maxCharLength;

    private byte[] rowData;
    private char[] rowPositionCode;
    private char[] rowCharacters;

    // TODO replace with computation
    private int subFontSpace = 3;

    @Nullable
    private Charset charMappingCharset = null;
    private final char[] charMapping = new char[256];
    // Debug
    private long paintCounter = 0;

    @Nullable
    private Paint fontMetrics;
    private boolean monospaceFont;
    private int characterWidth;

    public DefaultCodeAreaPainter(@Nonnull CodeAreaWorker worker) {
        this.worker = worker;
        CodeArea codeArea = worker.getCodeArea();
        dataView = new View(codeArea.getContext());

        dataView.setScrollContainer(true);
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
    public void reset() {
        resetColors();
        resetFont();
        resetLayout();
    }

    @Override
    public void resetFont() {
        fontChanged = true;
    }

    @Override
    public void resetLayout() {
        resetSizes();

        viewMode = ((ViewModeCapable) worker).getViewMode();
        hexCharactersCase = ((CodeCharactersCaseCapable) worker).getCodeCharactersCase();
        editationMode = ((EditationModeCapable) worker).getEditationMode();
        caretPosition.setPosition(((CaretCapable) worker).getCaret().getCaretPosition());
        selectionRange = ((SelectionCapable) worker).getSelection();
        backgroundPaintMode = ((BackgroundPaintCapable) worker).getBackgroundPaintMode();
        showMirrorCursor = ((CaretCapable) worker).isShowMirrorCursor();
        dataSize = worker.getCodeArea().getDataSize();

        rowsPerRect = computeRowsPerRectangle();
        rowsPerPage = computeRowsPerPage();
        bytesPerRow = computeBytesPerRow();

        codeType = ((CodeTypeCapable) worker).getCodeType();
        hexCharactersCase = CodeCharactersCase.UPPER;

        charactersPerPage = computeCharactersPerPage();
        charactersPerRow = computeCharactersPerRow();

        resetScrollState();
    }

    private void resetCharPositions() {
        charactersPerRect = computeCharactersPerRectangle();

        // Compute first and last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            codeLastCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1) - 1;
        } else {
            codeLastCharPos = 0;
        }

        if (viewMode == CodeAreaViewMode.DUAL) {
            previewCharPos = bytesPerRow * (codeType.getMaxDigitsForByte() + 1);
        } else {
            previewCharPos = 0;
        }
        previewRelativeX = previewCharPos * characterWidth;

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            visibleCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            visibleCharEnd = ((scrollPosition.getScrollCharPosition() + charactersPerRect) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleCharEnd > charactersPerRow) {
                visibleCharEnd = charactersPerRow;
            }
            visibleCodeStart = computePositionByte(visibleCharStart);
            visibleCodeEnd = computePositionByte(visibleCharEnd - 1) + 1;
        } else {
            visibleCharStart = 0;
            visibleCharEnd = -1;
            visibleCodeStart = 0;
            visibleCodeEnd = -1;
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            visiblePreviewStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewStart < 0) {
                visiblePreviewStart = 0;
            }
            if (visibleCodeEnd < 0) {
                visibleCharStart = visiblePreviewStart + previewCharPos;
            }
            visiblePreviewEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + 1) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewEnd > bytesPerRow) {
                visiblePreviewEnd = bytesPerRow;
            }
            if (visiblePreviewEnd >= 0) {
                visibleCharEnd = visiblePreviewEnd + previewCharPos;
            }
        } else {
            visiblePreviewStart = 0;
            visiblePreviewEnd = -1;
        }

        rowData = new byte[bytesPerRow + maxCharLength - 1];
        rowPositionCode = new char[rowPositionLength];
        rowCharacters = new char[charactersPerRow];
    }

    public void resetFont(@Nonnull Canvas g) {
        if (font == null) {
            reset();
        }

        charset = ((CharsetCapable) worker).getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        maxCharLength = (int) encoder.maxBytesPerChar();

        font = ((FontCapable) worker).getFont();
        fontMetrics = new Paint(); // g.getFontMetrics(font);
        /**
         * Use small 'w' character to guess normal font width.
         */
        characterWidth = (int) fontMetrics.measureText("w");
        /**
         * Compare it to small 'i' to detect if font is monospaced.
         *
         * TODO: Is there better way?
         */
        monospaceFont = characterWidth == fontMetrics.measureText(" ") && characterWidth == fontMetrics.measureText("i");
        int fontSize = (int) fontMetrics.getTextSize();
        rowHeight = fontSize + subFontSpace;

        rowPositionLength = getRowPositionLength();
        resetSizes();
        resetCharPositions();
        initialized = true;
    }

    public void dataViewScrolled(@Nonnull Canvas g) {
        if (!isInitialized()) {
            return;
        }

        resetScrollState();
        if (characterWidth > 0) {
            resetCharPositions();
            paintComponent(g);
        }
    }

    private void resetScrollState() {
        scrollPosition.setScrollPosition(((ScrollingCapable) worker).getScrollPosition());

        if (characterWidth > 0) {
            resetCharPositions();
        }

        verticalScrollUnit = ((ScrollingCapable) worker).getVerticalScrollUnit();
        horizontalScrollUnit = ((ScrollingCapable) worker).getHorizontalScrollUnit();

        if (rowHeight > 0 && characterWidth > 0) {
            int documentDataWidth = charactersPerRow * characterWidth;
            long rowsPerData = (dataSize + bytesPerRow - 1) / bytesPerRow;

            int documentDataHeight;
            if (rowsPerData > Integer.MAX_VALUE / rowHeight) {
                scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                documentDataHeight = Integer.MAX_VALUE;
            } else {
                scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                documentDataHeight = (int) (rowsPerData * rowHeight);
            }

            dataView.setMinimumWidth(documentDataWidth);
            dataView.setMinimumHeight(documentDataHeight);
        }

        // TODO on resize only
        Rect scrollPanelRectangle = getScrollPanelRectangle();
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(scrollPanelRectangle.width(), scrollPanelRectangle.height());
        layoutParams.setMargins(scrollPanelRectangle.left, scrollPanelRectangle.top, 0, 0);
        dataView.setLayoutParams(layoutParams);
        dataView.invalidate();
    }

    private void resetSizes() {
        if (fontMetrics == null) {
            headerAreaHeight = 0;
        } else {
            int fontHeight = (int) fontMetrics.getTextSize();
            headerAreaHeight = fontHeight + fontHeight / 4;
        }

        componentWidth = worker.getCodeArea().getWidth();
        componentHeight = worker.getCodeArea().getHeight();
        rowPositionAreaWidth = characterWidth * (rowPositionLength + 1);
        dataViewX = rowPositionAreaWidth;
        dataViewY = headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - getVerticalScrollBarSize();
        dataViewHeight = scrollPanelHeight - getHorizontalScrollBarSize();
    }

    private void resetColors() {
        CodeArea codeArea = worker.getCodeArea();
//        colors.foreground = codeArea.getForeground();
//        if (colors.foreground == null) {
            colors.foreground = Color.BLACK;
//        }

//        colors.background = codeArea.getBackground();
//        if (colors.background == null) {
            colors.background = Color.WHITE;
//        }
//        colors.selectionForeground = UIManager.getColor("TextArea.selectionForeground");
//        if (colors.selectionForeground == null) {
            colors.selectionForeground = Color.WHITE;
//        }
//        colors.selectionBackground = UIManager.getColor("TextArea.selectionBackground");
//        if (colors.selectionBackground == null) {
            colors.selectionBackground = Color.rgb(96, 96, 255);
//        }
        colors.selectionMirrorForeground = colors.selectionForeground;
        colors.selectionMirrorBackground = CodeAreaAndroidUtils.computeGrayColor(colors.selectionBackground);
//        colors.cursor = UIManager.getColor("TextArea.caretForeground");
//        if (colors.cursor == null) {
            colors.cursor = Color.BLACK;
//        }
        colors.negativeCursor = CodeAreaAndroidUtils.createNegativeColor(colors.cursor);
        colors.decorationLine = Color.GRAY;

        colors.stripes = CodeAreaAndroidUtils.createOddColor(colors.background);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void paintComponent(@Nonnull Canvas g) {
        if (!initialized) {
            reset();
        }
        if (font == null) {
            resetFont(g);
        }

        paintOutsiteArea(g);
        paintHeader(g);
        paintRowPosition(g);
        paintMainArea(g);
//        scrollPanel.paintComponents(g);
        paintCounter++;
    }

    public void paintOutsiteArea(@Nonnull Canvas g) {
        fontMetrics.setColor(colors.background);
        g.drawRect(0, 0, componentWidth, headerAreaHeight, fontMetrics);

        // Decoration lines
        fontMetrics.setColor(colors.decorationLine);
        g.drawLine(0, headerAreaHeight - 1, rowPositionAreaWidth, headerAreaHeight - 1, fontMetrics);

        {
            int lineX = rowPositionAreaWidth - (characterWidth / 2);
            if (lineX >= 0) {
                g.drawLine(lineX, 0, lineX, headerAreaHeight, fontMetrics);
            }
        }
    }

    public void paintHeader(@Nonnull Canvas g) {
        Rect clipBounds = g.getClipBounds();
        Rect headerArea = new Rect(rowPositionAreaWidth, 0, componentWidth - getVerticalScrollBarSize(), headerAreaHeight);
        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(headerArea, clipBounds) : headerArea);

        fontMetrics.setColor(colors.background);
        g.drawRect(headerArea.left, headerArea.top, headerArea.right, headerArea.bottom, fontMetrics);

        // Decoration lines
        fontMetrics.setColor(colors.decorationLine);
        g.drawRect(0, headerAreaHeight - 1, componentWidth, 1, fontMetrics);
        int lineX = dataViewX + previewRelativeX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, 0, lineX, headerAreaHeight, fontMetrics);
        }

        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int charactersPerCodeArea = computeFirstCodeCharacterPos(bytesPerRow);
            int headerX = dataViewX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
            int headerY = rowHeight - subFontSpace;

            int visibleHeaderCharStart = (scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleHeaderCharStart < 0) {
                visibleHeaderCharStart = 0;
            }
            int visibleHeaderCharEnd = (dataViewWidth + (scrollPosition.getScrollCharPosition() + charactersPerCodeArea) * characterWidth + scrollPosition.getScrollCharOffset()) / characterWidth;
            if (visibleHeaderCharEnd > charactersPerCodeArea) {
                visibleHeaderCharEnd = charactersPerCodeArea;
            }
            int visibleStart = computePositionByte(visibleHeaderCharStart);
            int visibleEnd = computePositionByte(visibleHeaderCharEnd - 1) + 1;

            fontMetrics.setColor(colors.foreground);
            char[] headerChars = new char[charactersPerCodeArea];
            Arrays.fill(headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            for (int index = visibleStart; index < visibleEnd; index++) {
                int codePos = computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    CodeAreaUtils.longToBaseCode(headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, hexCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int renderOffset = visibleHeaderCharStart;
//            ColorsGroup.ColorType renderColorType = null;
            Integer renderColor = null;
            for (int characterOnRow = visibleHeaderCharStart; characterOnRow < visibleHeaderCharEnd; characterOnRow++) {
                int byteOnRow;
                byteOnRow = computePositionByte(characterOnRow);
                boolean sequenceBreak = false;
                boolean nativeWidth = true;

                int currentCharWidth = 0;
//                ColorsGroup.ColorType colorType = ColorsGroup.ColorType.TEXT;
//                if (characterRenderingMode != CharacterRenderingMode.LINE_AT_ONCE) {
                char currentChar = ' ';
//                    if (colorType == ColorsGroup.ColorType.TEXT) {
                currentChar = headerChars[characterOnRow];
//                    }
                if (currentChar == ' ' && renderOffset == characterOnRow) {
                    renderOffset++;
                    continue;
                }
                if (monospaceFont) { // characterRenderingMode == CharacterRenderingMode.AUTO && 
                    // Detect if character is in unicode range covered by monospace fonts
                    if (CodeAreaAndroidUtils.isMonospaceFullWidthCharater(currentChar)) {
                        currentCharWidth = characterWidth;
                    }
                }

                if (currentCharWidth == 0) {
                    currentCharWidth = (int) fontMetrics.measureText(String.valueOf(currentChar));
                    nativeWidth = currentCharWidth == characterWidth;
                }
//                } else {
//                currentCharWidth = characterWidth;
//                }

                int color = colors.foreground;
//                getHeaderPositionColor(byteOnRow, charOnRow);
//                if (renderColorType == null) {
//                    renderColorType = colorType;
//                    renderColor = color;
//                    g.setColor(color);
//                }

                if (!nativeWidth || !CodeAreaAndroidUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        g.drawText(headerChars, renderOffset, characterOnRow - renderOffset, headerX + renderOffset * characterWidth, headerY, fontMetrics);
                    }

//                    if (!colorType.equals(renderColorType)) {
//                        renderColorType = colorType;
//                    }
                    if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        fontMetrics.setColor(color);
                    }

                    if (!nativeWidth) {
                        renderOffset = characterOnRow + 1;
//                        if (characterRenderingMode == CharacterRenderingMode.TOP_LEFT) {
//                            g.drawChars(headerChars, characterOnRow, 1, headerX + characterOnRow * characterWidth, headerY);
//                        } else {
                        int positionX = headerX + characterOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
                        drawShiftedChar(g, headerChars, characterOnRow, characterWidth, positionX, headerY);
//                        }
                    } else {
                        renderOffset = characterOnRow;
                    }
                }
            }

            if (renderOffset < charactersPerCodeArea) {
                g.drawText(headerChars, renderOffset, charactersPerCodeArea - renderOffset, headerX + renderOffset * characterWidth, headerY, fontMetrics);
            }
        }

        g.clipRect(clipBounds);
    }

    public void paintRowPosition(@Nonnull Canvas g) {
        Rect clipBounds = g.getClipBounds();
        Rect rowPositionsArea = new Rect(0, headerAreaHeight, rowPositionAreaWidth, componentHeight - headerAreaHeight - getHorizontalScrollBarSize());
        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(rowPositionsArea, clipBounds) : rowPositionsArea);

        fontMetrics.setColor(colors.background);
        g.drawRect(rowPositionsArea.left, rowPositionsArea.top, rowPositionsArea.right, rowPositionsArea.bottom, fontMetrics);

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight + ((scrollPosition.getScrollRowPosition() & 1) > 0 ? 0 : rowHeight);
            fontMetrics.setColor(colors.stripes);
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.drawRect(0, stripePositionY, rowPositionAreaWidth, rowHeight, fontMetrics);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getScrollRowPosition();
        int positionY = headerAreaHeight + rowHeight - subFontSpace - scrollPosition.getScrollRowOffset();
        fontMetrics.setColor(colors.foreground);
        Rectangle compRect = new Rectangle();
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, codeType.getBase(), rowPositionLength, true, CodeCharactersCase.UPPER);
//            if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                g.drawChars(lineNumberCode, 0, lineNumberLength, compRect.x, positionY);
//            } else {
            for (int digitIndex = 0; digitIndex < rowPositionLength; digitIndex++) {
                drawCenteredChar(g, rowPositionCode, digitIndex, characterWidth, compRect.x + characterWidth * digitIndex, positionY);
            }
//            }

            positionY += rowHeight;
            dataPosition += bytesPerRow;
        }

        fontMetrics.setColor(colors.decorationLine);
        int lineX = rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= 0) {
            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight, fontMetrics);
        }
        g.drawLine(dataViewX, dataViewY - 1, dataViewX + dataViewWidth, dataViewY - 1, fontMetrics);

        g.clipRect(clipBounds);
    }

    @Override
    public void paintMainArea(@Nonnull Canvas g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            resetFont(g);
            fontChanged = false;
        }

        Rect clipBounds = g.getClipBounds();
        Rect mainArea = getMainAreaRect();
        g.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(mainArea, clipBounds) : mainArea);
        paintBackground(g);

        fontMetrics.setColor(colors.decorationLine);
        int lineX = dataViewX + previewRelativeX - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset() - characterWidth / 2;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, dataViewY, lineX, dataViewY + dataViewHeight, fontMetrics);
        }

        paintRows(g);
        g.clipRect(clipBounds);
        paintCursor(g);

        // TODO: Remove later
        int x = componentWidth - rowPositionAreaWidth - 220;
        int y = componentHeight - headerAreaHeight - 20;
        fontMetrics.setColor(Color.YELLOW);
        g.drawRect(x, y, 200, 16, fontMetrics);
        fontMetrics.setColor(Color.BLACK);
        char[] headerCode = (String.valueOf(scrollPosition.getScrollCharPosition()) + "+" + String.valueOf(scrollPosition.getScrollCharOffset()) + " : " + String.valueOf(scrollPosition.getScrollRowPosition()) + "+" + String.valueOf(scrollPosition.getScrollRowOffset()) + " P: " + String.valueOf(paintCounter)).toCharArray();
        g.drawText(headerCode, 0, headerCode.length, x, y + rowHeight, fontMetrics);
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(@Nonnull Canvas g) {
        int rowPositionX = rowPositionAreaWidth;
        fontMetrics.setColor(colors.background);
        if (backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            g.drawRect(rowPositionX, headerAreaHeight, dataViewWidth, dataViewHeight, fontMetrics);
        }

        if (backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
            int stripePositionY = headerAreaHeight + (int) ((scrollPosition.getScrollRowPosition() & 1) > 0 ? 0 : rowHeight);
            fontMetrics.setColor(colors.stripes);
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition >= dataSize) {
                    break;
                }

                g.drawRect(rowPositionX, stripePositionY, dataViewWidth, rowHeight, fontMetrics);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    public void paintRows(@Nonnull Canvas g) {
        long dataPosition = scrollPosition.getScrollRowPosition() * bytesPerRow;
        int rowPositionX = rowPositionAreaWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
        int rowPositionY = headerAreaHeight;
        fontMetrics.setColor(colors.foreground);
        for (int row = 0; row <= rowsPerRect; row++) {
            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            dataPosition += bytesPerRow;
        }
    }

    private void prepareRowData(long dataPosition) {
        int rowBytesLimit = bytesPerRow;
        int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxCharLength - 1;
            if (dataPosition + rowDataSize > dataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            worker.getCodeArea().getContentData().copyToArray(dataPosition + rowStart, rowData, rowStart, rowDataSize - rowStart);
            if (dataPosition + rowBytesLimit > dataSize) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            for (int byteOnRow = Math.max(visibleCodeStart, rowStart); byteOnRow < Math.min(visibleCodeEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowData[byteOnRow];
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowCharacters, computeFirstCodeCharacterPos(byteOnRow), hexCharactersCase);
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowCharacters, computeFirstCodeCharacterPos(rowBytesLimit), rowCharacters.length, ' ');
            }
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            for (int byteOnRow = visiblePreviewStart; byteOnRow < Math.min(visiblePreviewEnd, rowBytesLimit); byteOnRow++) {
                byte dataByte = rowData[byteOnRow];

                if (maxCharLength > 1) {
                    if (dataPosition + maxCharLength > dataSize) {
                        maxCharLength = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxCharLength;
                    if (byteOnRow + charDataLength > rowData.length) {
                        charDataLength = rowData.length - byteOnRow;
                    }
                    String displayString = new String(rowData, byteOnRow, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        rowCharacters[previewCharPos + byteOnRow] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    rowCharacters[previewCharPos + byteOnRow] = charMapping[dataByte & 0xFF];
                }
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowCharacters, previewCharPos + rowBytesLimit, previewCharPos + bytesPerRow, ' ');
            }
        }
    }

    /**
     * Paints row background.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowBackground(@Nonnull Canvas g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int renderOffset = visibleCharStart;
        Integer renderColor = null;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
            int section;
            int byteOnRow;
            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
            } else {
                byteOnRow = computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
            }
            boolean sequenceBreak = false;

            Integer color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        fontMetrics.setColor(color);
                    }
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
            }
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    @Nullable
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, int section) {
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX.getSection())) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == caretPosition.getSection() ? colors.selectionBackground : colors.selectionMirrorBackground;
        }

        return null;
    }

    @Nullable
    @Override
    public CodeAreaScrollPosition computeRevealScrollPosition(@Nonnull CaretPosition caretPosition) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);
        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        if (caretPosition.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        boolean scrolled = false;
        if (rowPosition < scrollPosition.getScrollRowPosition()) {
            // Scroll row up
            targetScrollPosition.setScrollRowPosition(rowPosition);
            targetScrollPosition.setScrollRowOffset(0);
            scrolled = true;
        } else if ((rowPosition == scrollPosition.getScrollRowPosition() && scrollPosition.getScrollRowOffset() > 0)) {
            // Scroll row offset up
            targetScrollPosition.setScrollRowOffset(0);
            scrolled = true;
        } else {
            int bottomRowOffset;
            if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                bottomRowOffset = 0;
            } else {
                if (dataViewHeight < rowHeight) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else {
                    bottomRowOffset = dataViewHeight % rowHeight;
                }
            }

            if (rowPosition > scrollPosition.getScrollRowPosition() + rowsPerPage) {
                // Scroll row down
                targetScrollPosition.setScrollRowPosition(rowPosition - rowsPerPage);
                targetScrollPosition.setScrollRowOffset(bottomRowOffset);
                scrolled = true;
            } else if (rowPosition == scrollPosition.getScrollRowPosition() + rowsPerPage && scrollPosition.getScrollRowOffset() > bottomRowOffset) {
                // Scroll row offset down
                targetScrollPosition.setScrollRowOffset(bottomRowOffset);
                scrolled = true;
            }
        }

        if (charPosition < scrollPosition.getScrollCharPosition()) {
            // Scroll characters left
            targetScrollPosition.setScrollCharPosition(charPosition);
            targetScrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else if (charPosition == scrollPosition.getScrollCharPosition() && scrollPosition.getScrollCharOffset() > 0) {
            // Scroll character offset left
            targetScrollPosition.setScrollCharOffset(0);
            scrolled = true;
        } else {
            int rightCharOffset;
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                rightCharOffset = 0;
            } else {
                if (dataViewWidth < characterWidth) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else {
                    rightCharOffset = dataViewWidth % characterWidth;
                }
            }

            if (charPosition > scrollPosition.getScrollCharPosition() + charactersPerPage) {
                // Scroll character right
                targetScrollPosition.setScrollCharPosition(charPosition - charactersPerPage);
                targetScrollPosition.setScrollCharOffset(rightCharOffset);
                scrolled = true;
            } else if (charPosition == scrollPosition.getScrollCharPosition() + charactersPerPage && scrollPosition.getScrollCharOffset() > rightCharOffset) {
                // Scroll row offset down
                targetScrollPosition.setScrollCharOffset(rightCharOffset);
                scrolled = true;
            }
        }
        return scrolled ? targetScrollPosition : null;
    }

    @Override
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CaretPosition caretPosition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    public void paintRowText(@Nonnull Canvas g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int positionY = rowPositionY + rowHeight - subFontSpace;

        Integer lastColor = null;
        Integer renderColor = null;

        int renderOffset = visibleCharStart;
        for (int charOnRow = visibleCharStart; charOnRow < visibleCharEnd; charOnRow++) {
            int section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = BasicCodeAreaSection.TEXT_PREVIEW.getSection();
            } else {
                byteOnRow = computePositionByte(charOnRow);
                section = BasicCodeAreaSection.CODE_MATRIX.getSection();
            }

            Integer color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (color == null) {
                color = colors.foreground;
            }

            boolean sequenceBreak = false;
            if (!CodeAreaAndroidUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            int currentCharWidth = 0;
            char currentChar = rowCharacters[charOnRow];
            if (currentChar == ' ' && renderOffset == charOnRow) {
                renderOffset++;
                continue;
            }

            if (monospaceFont) {
                // Detect if character is in unicode range covered by monospace fonts
                if (CodeAreaAndroidUtils.isMonospaceFullWidthCharater(currentChar)) {
                    currentCharWidth = characterWidth;
                }
            }

            boolean nativeWidth = true;
            if (currentCharWidth == 0) {
                currentCharWidth = (int) fontMetrics.measureText(String.valueOf(currentChar));
                nativeWidth = currentCharWidth == characterWidth;
            }

            if (!nativeWidth) {
                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaAndroidUtils.areSameColors(lastColor, renderColor)) {
                    fontMetrics.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    renderCharSequence(g, renderOffset, charOnRow, rowPositionX, positionY);
                }

                renderColor = color;
                if (!CodeAreaAndroidUtils.areSameColors(lastColor, renderColor)) {
                    fontMetrics.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (!nativeWidth) {
                    renderOffset = charOnRow + 1;
                    int positionX = rowPositionX + charOnRow * characterWidth + ((characterWidth + 1 - currentCharWidth) >> 1);
                    drawShiftedChar(g, rowCharacters, charOnRow, characterWidth, positionX, positionY);
                } else {
                    renderOffset = charOnRow;
                }
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaAndroidUtils.areSameColors(lastColor, renderColor)) {
                fontMetrics.setColor(renderColor);
            }

            renderCharSequence(g, renderOffset, charactersPerRow, rowPositionX, positionY);
        }
    }

    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    @Nullable
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, int section) {
        boolean inSelection = selectionRange != null && selectionRange.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == caretPosition.getSection() ? colors.selectionForeground : colors.selectionMirrorForeground;
        }

        return null;
    }

    @Override
    public void paintCursor(@Nonnull Canvas g) {
        if (!worker.getCodeArea().hasFocus()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) worker).getCaret();
        Rect cursorRect = getPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect == null) {
            return;
        }

        Rect clipBounds = g.getClipBounds();
        Rect mainAreaRect = getMainAreaRect();
        Rect intersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRect, cursorRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.clipRect(intersection);
            DefaultCodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            fontMetrics.setColor(colors.cursor);

            switch (renderingMode) {
                case PAINT: {
                    g.drawRect(intersection, fontMetrics);
                    break;
                }
                case XOR: {
//                    g.setXORMode(colors.background);
//                    g.drawRect(intersection.x, intersection.y, intersection.width, intersection.height, fontMetrics);
//                    g.setPaintMode();
                    break;
                }
                case NEGATIVE: {
                    g.drawRect(cursorRect, fontMetrics);
                    fontMetrics.setColor(colors.negativeCursor);
                    BinaryData codeAreaData = worker.getCodeArea().getContentData();
                    int row = (cursorRect.top + scrollPosition.getScrollRowOffset() - dataViewY) / rowHeight;
                    int scrolledX = cursorRect.left + scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();
                    int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getScrollRowOffset();
                    long dataPosition = caret.getDataPosition();
                    if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
                        int charPos = (scrolledX - previewRelativeX) / characterWidth;
                        if (dataPosition >= dataSize) {
                            break;
                        }

                        char[] previewChars = new char[1];
                        byte[] data = new byte[maxCharLength];

                        if (maxCharLength > 1) {
                            int charDataLength = maxCharLength;
                            if (dataPosition + maxCharLength > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            if (codeAreaData == null) {
                                previewChars[0] = ' ';
                            } else {
                                codeAreaData.copyToArray(dataPosition, data, 0, charDataLength);
                                String displayString = new String(data, 0, charDataLength, charset);
                                if (!displayString.isEmpty()) {
                                    previewChars[0] = displayString.charAt(0);
                                }
                            }
                        } else {
                            if (charMappingCharset == null || charMappingCharset != charset) {
                                buildCharMapping(charset);
                            }

                            if (codeAreaData == null) {
                                previewChars[0] = ' ';
                            } else {
                                previewChars[0] = charMapping[codeAreaData.getByte(dataPosition) & 0xFF];
                            }
                        }
                        int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
//                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                            g.drawChars(previewChars, 0, 1, posX, posY);
//                        } else {
                        drawCenteredChar(g, previewChars, 0, characterWidth, posX, posY);
//                        }
                    } else {
                        int charPos = (scrolledX - dataViewX) / characterWidth;
                        int byteOffset = computePositionByte(charPos);
                        int codeCharPos = computeFirstCodeCharacterPos(byteOffset);
                        char[] rowChars = new char[codeType.getMaxDigitsForByte()];

                        if (codeAreaData != null && dataPosition < dataSize) {
                            byte dataByte = codeAreaData.getByte(dataPosition);
                            CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowChars, 0, hexCharactersCase);
                        } else {
                            Arrays.fill(rowChars, ' ');
                        }
                        int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getScrollCharPosition() * characterWidth - scrollPosition.getScrollCharOffset();
                        int charsOffset = charPos - codeCharPos;
//                        if (characterRenderingMode == CharacterRenderingMode.LINE_AT_ONCE) {
//                            g.drawChars(lineChars, charsOffset, 1, posX + (charsOffset * characterWidth), posY);
//                        } else {
                        drawCenteredChar(g, rowChars, charsOffset, characterWidth, posX + (charsOffset * characterWidth), posY);
//                        }
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected rendering mode " + renderingMode.name());
            }
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            Rect mirrorCursorRect = getMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            if (mirrorCursorRect != null) {
                intersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRect, mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.clipRect(intersection);
                    fontMetrics.setColor(colors.cursor);
//                    Graphics2D g2d = (Graphics2D) g.create();
//                    Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
//                    g2d.setStroke(dashed);
                    g.drawRect(mirrorCursorRect.left, mirrorCursorRect.top, mirrorCursorRect.right - 1, mirrorCursorRect.bottom - 1, fontMetrics);
                }
            }
        }
        g.clipRect(clipBounds);
    }

    @Nonnull
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        if (positionX < rowPositionAreaWidth) {
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getScrollCharOffset()) / characterWidth + scrollPosition.getScrollCharPosition();
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        if (positionY < headerAreaHeight) {
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getScrollRowOffset()) / rowHeight + scrollPosition.getScrollRowPosition();
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(BasicCodeAreaSection.CODE_MATRIX.getSection());
            byteOnRow = computePositionByte(cursorCharX);
            if (byteOnRow >= bytesPerRow) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - computeFirstCodeCharacterPos(byteOnRow);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(BasicCodeAreaSection.TEXT_PREVIEW.getSection());
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
    public CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction) {
        CodeAreaCaretPosition target = new CodeAreaCaretPosition(position.getDataPosition(), position.getCodeOffset(), position.getSection());
        switch (direction) {
            case LEFT: {
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
                    int codeOffset = position.getCodeOffset();
                    if (codeOffset > 0) {
                        target.setCodeOffset(codeOffset - 1);
                    } else if (position.getDataPosition() > 0) {
                        target.setDataPosition(position.getDataPosition() - 1);
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                } else if (position.getDataPosition() > 0) {
                    target.setDataPosition(position.getDataPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
                    int codeOffset = position.getCodeOffset();
                    if (position.getDataPosition() < dataSize && codeOffset < codeType.getMaxDigitsForByte() - 1) {
                        target.setCodeOffset(codeOffset + 1);
                    } else if (position.getDataPosition() < dataSize) {
                        target.setDataPosition(position.getDataPosition() + 1);
                        target.setCodeOffset(0);
                    }
                } else if (position.getDataPosition() < dataSize) {
                    target.setDataPosition(position.getDataPosition() + 1);
                }
                break;
            }
            case UP: {
                if (position.getDataPosition() >= bytesPerRow) {
                    target.setDataPosition(position.getDataPosition() - bytesPerRow);
                }
                break;
            }
            case DOWN: {
                if (position.getDataPosition() + bytesPerRow < dataSize || (position.getDataPosition() + bytesPerRow == dataSize && position.getCodeOffset() == 0)) {
                    target.setDataPosition(position.getDataPosition() + bytesPerRow);
                }
                break;
            }
            case ROW_START: {
                long dataPosition = position.getDataPosition();
                dataPosition -= (dataPosition % bytesPerRow);
                target.setDataPosition(dataPosition);
                target.setCodeOffset(0);
                break;
            }
            case ROW_END: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow - 1 - (dataPosition % bytesPerRow);
                if (dataPosition > Long.MAX_VALUE - increment || dataPosition + increment > dataSize) {
                    target.setDataPosition(dataSize);
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                if (position.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
                    if (target.getDataPosition() == dataSize) {
                        target.setCodeOffset(0);
                    } else {
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                }
                break;
            }
            case PAGE_UP: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow * rowsPerPage;
                if (dataPosition < increment) {
                    target.setDataPosition(dataPosition % bytesPerRow);
                } else {
                    target.setDataPosition(dataPosition - increment);
                }
                break;
            }
            case PAGE_DOWN: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow * rowsPerPage;
                if (dataPosition > dataSize - increment) {
                    long positionOnRow = dataPosition % bytesPerRow;
                    long rowDataStart = dataSize / bytesPerRow;
                    if (rowDataStart == dataSize - positionOnRow) {
                        target.setDataPosition(dataSize);
                        target.setCodeOffset(0);
                    } else if (rowDataStart > dataSize - positionOnRow) {
                        if (rowDataStart > bytesPerRow) {
                            rowDataStart -= bytesPerRow;
                            target.setDataPosition(rowDataStart + positionOnRow);
                        }
                    } else {
                        target.setDataPosition(rowDataStart + positionOnRow);
                    }
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                break;
            }
            case DOC_START: {
                target.setDataPosition(0);
                target.setCodeOffset(0);
                break;
            }
            case DOC_END: {
                target.setDataPosition(dataSize);
                target.setCodeOffset(0);
                break;
            }
            case SWITCH_SECTION: {
                int activeSection = caretPosition.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection() ? BasicCodeAreaSection.TEXT_PREVIEW.getSection() : BasicCodeAreaSection.CODE_MATRIX.getSection();
                if (activeSection == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
                    target.setCodeOffset(0);
                }
                target.setSection(activeSection);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected movement direction " + direction.name());
            }
        }

        return target;
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection direction) {
        CodeAreaScrollPosition targetPosition = new CodeAreaScrollPosition();
        targetPosition.setScrollPosition(startPosition);

        switch (direction) {
            case UP: {
                if (startPosition.getScrollRowPosition() == 0) {
                    targetPosition.setScrollRowOffset(0);
                } else {
                    targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() - 1);
                }
                break;
            }
            case DOWN: {
//                if (startPosition.getScrollRowPosition() < rowsPerDocument) {
                targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() + 1);
//                }
                break;
            }
            case LEFT: {
                throw new UnsupportedOperationException("Not supported yet.");
                // break;
            }
            case RIGHT: {
                throw new UnsupportedOperationException("Not supported yet.");
                // break;
            }
            case PAGE_UP: {
                if (startPosition.getScrollRowPosition() < rowsPerPage) {
                    targetPosition.setScrollRowPosition(0);
                    targetPosition.setScrollRowOffset(0);
                } else {
                    targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() - rowsPerPage);
                }
                break;
            }
            case PAGE_DOWN: {
                long rowsPerDocument = dataSize / bytesPerRow;
                if (dataSize % bytesPerRow > 0) {
                    rowsPerDocument++;
                }
                if (startPosition.getScrollRowPosition() <= rowsPerDocument - rowsPerPage * 2) {
                    targetPosition.setScrollRowPosition(startPosition.getScrollRowPosition() + rowsPerPage);
                } else if (rowsPerDocument > rowsPerPage) {
                    targetPosition.setScrollRowPosition(rowsPerDocument - rowsPerPage);
                } else {
                    targetPosition.setScrollRowPosition(0);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling shift type: " + direction.name());
        }

        return targetPosition;
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor position or null
     */
    @Nullable
    public Point getPositionPoint(long dataPosition, int codeOffset, int section) {
        long row = dataPosition / bytesPerRow - scrollPosition.getScrollRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rect dataViewRect = getDataViewRectangle();
        int caretY = (int) (dataViewRect.top + row * rowHeight) - scrollPosition.getScrollRowOffset();
        int caretX;
        if (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection()) {
            caretX = dataViewRect.left + previewRelativeX + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.left + characterWidth * (computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset();

        return new Point(caretX, caretY);
    }

    @Nullable
    private Rect getMirrorCursorRect(long dataPosition, int section) {
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == BasicCodeAreaSection.CODE_MATRIX.getSection() ? BasicCodeAreaSection.TEXT_PREVIEW.getSection() : BasicCodeAreaSection.CODE_MATRIX.getSection());
        if (mirrorCursorPoint == null) {
            return null;
        }

        Rect mirrorCursorRect = new Rect(mirrorCursorPoint.x, mirrorCursorPoint.y, mirrorCursorPoint.x + characterWidth * (section == BasicCodeAreaSection.TEXT_PREVIEW.getSection() ? codeType.getMaxDigitsForByte() : 1), mirrorCursorPoint.y + rowHeight);
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
        if (positionY <= headerAreaHeight) {
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

    @Nonnull
    private Rect getScrollPanelRectangle() {
        return new Rect(dataViewX, dataViewY, dataViewX + scrollPanelWidth, dataViewY + scrollPanelHeight);
    }

    @Nonnull
    public Rect getDataViewRectangle() {
        return new Rect(dataViewX, dataViewY, dataViewX + dataViewWidth, dataViewY + dataViewHeight);
    }

    public int computePositionByte(int rowCharPosition) {
        return rowCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }

    public int computeLastCodeCharPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
    }

    /**
     * Draws char in array centering it in precomputed space.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param charWidthSpace default character width
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChar(@Nonnull Canvas g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        int charWidth = (int) fontMetrics.measureText(String.valueOf(drawnChars[charOffset]));
        drawShiftedChar(g, drawnChars, charOffset, charWidthSpace, positionX + ((charWidthSpace + 1 - charWidth) >> 1), positionY);
    }

    protected void drawShiftedChar(@Nonnull Canvas g, char[] drawnChars, int charOffset, int charWidthSpace, int positionX, int positionY) {
        g.drawText(drawnChars, charOffset, 1, positionX, positionY, fontMetrics);
    }

    private void buildCharMapping(@Nonnull Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getRowPositionLength() {
        return 8;
    }

    /**
     * Returns cursor rectangle.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor rectangle or null
     */
    @Nullable
    public Rect getPositionRect(long dataPosition, int codeOffset, int section) {
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            return null;
        }

        DefaultCodeAreaCaret.CursorShape cursorShape = editationMode == EditationMode.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE;
        int cursorThickness = DefaultCodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
        return new Rect(cursorPoint.x, cursorPoint.y, cursorPoint.x + cursorThickness, cursorPoint.y + rowHeight);
    }

    /**
     * Render sequence of characters.
     *
     * Doesn't include character at offset end.
     */
    private void renderCharSequence(@Nonnull Canvas g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.drawText(rowCharacters, startOffset, endOffset - startOffset, rowPositionX + startOffset * characterWidth, positionY, fontMetrics);
    }

    /**
     * Render sequence of background rectangles.
     *
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(@Nonnull Canvas g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        g.drawRect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight, fontMetrics);
    }

    private int computeRowsPerRectangle() {
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage() {
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    private int computeBytesPerRow() {
        boolean rowWrapping = ((RowWrappingCapable) worker).isRowWrapping();
        int maxBytesPerLine = ((RowWrappingCapable) worker).getMaxBytesPerRow();

        int computedBytesPerRow = 16;
        if (rowWrapping) {
            // TODO
        }

        return computedBytesPerRow;
    }

    private int computeCharactersPerRow() {
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos(bytesPerRow - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    private int computeCharactersPerRectangle() {
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage() {
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    @Override
    public void updateScrollBars() {
//        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
//        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
//
//        if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
//            long rowsPerDocument = (dataSize / bytesPerRow) + 1;
//            int scrollValue;
//            if (scrollPosition.getScrollCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
//                scrollValue = (int) ((scrollPosition.getScrollRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
//            } else {
//                scrollValue = (int) (scrollPosition.getScrollRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
//            }
//            verticalScrollBar.setValue(scrollValue);
//        } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
//            verticalScrollBar.setValue((int) scrollPosition.getScrollRowPosition() * rowHeight);
//        } else {
//            verticalScrollBar.setValue((int) (scrollPosition.getScrollRowPosition() * rowHeight + scrollPosition.getScrollRowOffset()));
//        }
//
//        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
//            horizontalScrollBar.setValue(scrollPosition.getScrollCharPosition() * characterWidth);
//        } else {
//            horizontalScrollBar.setValue(scrollPosition.getScrollCharPosition() * characterWidth + scrollPosition.getScrollCharOffset());
//        }
    }

    @Nonnull
    private Rect getMainAreaRect() {
        return new Rect(rowPositionAreaWidth, headerAreaHeight, componentWidth - getVerticalScrollBarSize(), componentHeight - getHorizontalScrollBarSize());
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
//            ((ScrollingCapable) worker).setScrollPosition(scrollPosition);
//            worker.getCodeArea().repaint();
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
//            ((ScrollingCapable) worker).setScrollPosition(scrollPosition);
//            notifyScrolled();
//            worker.getCodeArea().repaint();
////            dataViewScrolled(codeArea.getGraphics());
//        }
//    }

    private void notifyScrolled() {
        resetScrollState();
        // TODO
    }

    private static class Colors {

        int foreground;
        int background;
        int selectionForeground;
        int selectionBackground;
        int selectionMirrorForeground;
        int selectionMirrorBackground;
        int cursor;
        int negativeCursor;
        int cursorMirror;
        int negativeCursorMirror;
        int decorationLine;
        int stripes;
    }

    /**
     * Enumeration of vertical scalling modes.
     */
    public enum ScrollBarVerticalScale {
        /**
         * Normal ratio 1 on 1.
         */
        NORMAL,
        /**
         * Height is more than available range and scaled.
         */
        SCALED
    }
}
