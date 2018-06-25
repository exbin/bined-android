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
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationModeChangedListener;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.Font;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.android.CodeArea;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.CodeAreaWorker;
import org.exbin.bined.android.MovementDirection;
import org.exbin.bined.android.ScrollingDirection;
import org.exbin.bined.android.capability.BackgroundPaintCapable;
import org.exbin.bined.android.capability.FontCapable;
import org.exbin.bined.android.capability.ScrollingCapable;

/**
 * Code area component default worker.
 *
 * @version 0.2.0 2018/06/24
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaWorker implements CodeAreaWorker, SelectionCapable, CaretCapable, ScrollingCapable, ViewModeCapable,
        CodeTypeCapable, EditationModeCapable, CharsetCapable, CodeCharactersCaseCapable, FontCapable,
        BackgroundPaintCapable, RowWrappingCapable, ClipboardCapable {

    @Nonnull
    protected final CodeArea codeArea;

    @Nonnull
    private CodeAreaPainter painter;

    @Nonnull
    private final DefaultCodeAreaCaret caret;
    @Nonnull
    private final SelectionRange selection = new SelectionRange();
    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

    @Nonnull
    private Charset charset = Charset.defaultCharset();
    private boolean handleClipboard = true;

    @Nonnull
    private EditationMode editationMode = EditationMode.OVERWRITE;
    @Nonnull
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    @Nullable
    private Font font;
    @Nonnull
    private BasicBackgroundPaintMode borderPaintMode = BasicBackgroundPaintMode.STRIPED;
    @Nonnull
    private CodeType codeType = CodeType.HEXADECIMAL;
    @Nonnull
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private boolean showMirrorCursor = true;
    private boolean lineWrapping = false;
    private int wrappingBytesGroupSize = 0;
    private int maxBytesPerLine = 16;

    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();

    public DefaultCodeAreaWorker(@Nonnull CodeArea codeArea) {
        this.codeArea = codeArea;

        caret = new DefaultCodeAreaCaret(codeArea);
        painter = new DefaultCodeAreaPainter(this);
    }

    @Nonnull
    public static CodeAreaWorker.CodeAreaWorkerFactory createDefaultCodeAreaWorkerFactory() {
        return new CodeAreaWorkerFactory() {
            @Nonnull
            @Override
            public CodeAreaWorker createWorker(@Nonnull CodeArea codeArea) {
                return new DefaultCodeAreaWorker(codeArea);
            }
        };
    }

    @Nonnull
    @Override
    public CodeArea getCodeArea() {
        return codeArea;
    }

    @Nonnull
    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(@Nonnull CodeAreaPainter painter) {
        CodeAreaUtils.requireNonNull(painter);

        this.painter = painter;
        repaint();
    }

    @Override
    public boolean isInitialized() {
        return painter.isInitialized();
    }

    @Override
    public void paintComponent(@Nonnull Canvas g) {
        painter.paintComponent(g);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        codeArea.getCommandHandler().moveCaret((int) event.getX(), (int) event.getY(), false);
        ((CaretCapable) codeArea.getWorker()).revealCursor();

        return true;
    }

    @Nonnull
    @Override
    public DefaultCodeAreaCaret getCaret() {
        return caret;
    }

    @Override
    public boolean isShowMirrorCursor() {
        return showMirrorCursor;
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        this.showMirrorCursor = showMirrorCursor;
        repaint();
    }

    public long getDataPosition() {
        return caret.getDataPosition();
    }

    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    public int getActiveSection() {
        return caret.getSection();
    }

    @Nonnull
    public CaretPosition getCaretPosition() {
        return caret.getCaretPosition();
    }

    public void setCaretPosition(@Nonnull CaretPosition caretPosition) {
        caret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    public void setCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        notifyCaretMoved();
    }

    public void setCaretPosition(long dataPosition, int codeOffset) {
        caret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        return painter.getMouseCursorShape(positionX, positionY);
    }

    @NonNull
    @Override
    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        return painter.getPositionZone(positionX, positionY);
    }

    @Nonnull
    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    @Override
    public void setCodeCharactersCase(@Nonnull CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        repaint();
    }

    @Override
    public void resetColors() {
    }

    @Nonnull
    @Override
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public void setViewMode(@Nonnull CodeAreaViewMode viewMode) {
        this.viewMode = viewMode;
        if (viewMode == CodeAreaViewMode.CODE_MATRIX) {
            getCaret().setSection(BasicCodeAreaSection.CODE_MATRIX.getSection());
            notifyCaretMoved();
        } else if (viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            getCaret().setSection(BasicCodeAreaSection.TEXT_PREVIEW.getSection());
            notifyCaretMoved();
        }
        repaint();
    }

    @Override
    @Nonnull
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void setCodeType(@Nonnull CodeType codeType) {
        this.codeType = codeType;
        painter.reset();
        repaint();
    }

    @Override
    public void revealCursor() {
        revealPosition(caret.getCaretPosition());
    }

    @Override
    public void revealPosition(@Nonnull CaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        CodeAreaScrollPosition revealScrollPosition = painter.computeRevealScrollPosition(caretPosition);
        if (revealScrollPosition != null) {
            setScrollPosition(revealScrollPosition);
            codeArea.resetPainter();
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void revealPosition(long dataPosition, int dataOffset, int section) {
        revealPosition(new CodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Override
    public void centerOnCursor() {
        centerOnPosition(caret.getCaretPosition());
    }

    @Override
    public void centerOnPosition(@Nonnull CaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        CodeAreaScrollPosition centerOnScrollPosition = painter.computeCenterOnScrollPosition(caretPosition);
        if (centerOnScrollPosition != null) {
            setScrollPosition(centerOnScrollPosition);
            codeArea.resetPainter();
            updateScrollBars();
            notifyScrolled();
        }
    }

    public void centerOnPosition(long dataPosition, int dataOffset, int section) {
        centerOnPosition(new CodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Nullable
    @Override
    public CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY);
    }

    @Nonnull
    @Override
    public CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction) {
        return painter.computeMovePosition(position, direction);
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection scrollingShift) {
        return painter.computeScrolling(startPosition, scrollingShift);
    }

    @Override
    public void updateScrollBars() {
        painter.updateScrollBars();
        repaint();
    }

    @Nonnull
    @Override
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public void setScrollPosition(@Nonnull CodeAreaScrollPosition scrollPosition) {
        this.scrollPosition.setScrollPosition(scrollPosition);
    }

    @Nonnull
    @Override
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        codeArea.resetPainter();
        updateScrollBars();
    }

    @Nonnull
    @Override
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Override
    public void setVerticalScrollUnit(@Nonnull VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long linePosition = scrollPosition.getScrollRowPosition();
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            scrollPosition.setScrollRowOffset(0);
        }
        codeArea.resetPainter();
        scrollPosition.setScrollRowPosition(linePosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Nonnull
    @Override
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Override
    public void setHorizontalScrollBarVisibility(@Nonnull ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        codeArea.resetPainter();
        updateScrollBars();
    }

    @Nonnull
    @Override
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Override
    public void setHorizontalScrollUnit(@Nonnull HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int bytePosition = scrollPosition.getScrollCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setScrollCharOffset(0);
        }
        codeArea.resetPainter();
        scrollPosition.setScrollCharPosition(bytePosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void reset() {
        painter.reset();
    }

    @Override
    public void updateLayout() {
        painter.updateLayout();
    }

    private void repaint() {
        codeArea.resetPainter();
        codeArea.repaint();
    }

    @Override
    public void notifyCaretChanged() {
        codeArea.repaint();
    }

    @Nonnull
    @Override
    public SelectionRange getSelection() {
        return selection;
    }

    @Override
    public void setSelection(@Nonnull SelectionRange selection) {
        CodeAreaUtils.requireNonNull(selection);

        this.selection.setSelection(selection);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void setSelection(long start, long end) {
        this.selection.setSelection(start, end);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void clearSelection() {
        this.selection.clearSelection();
        notifySelectionChanged();
        repaint();
    }

    @Override
    public boolean hasSelection() {
        return !selection.isEmpty();
    }

    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(@Nonnull Charset charset) {
        CodeAreaUtils.requireNonNull(charset);

        this.charset = charset;
        painter.reset();
        repaint();
    }

    @Nonnull
    @Override
    public EditationMode getEditationMode() {
        return editationMode;
    }

    @Override
    public boolean isEditable() {
        return editationMode != EditationMode.READ_ONLY;
    }

    @Override
    public void setEditationMode(@Nonnull EditationMode editationMode) {
        boolean changed = editationMode != this.editationMode;
        this.editationMode = editationMode;
        if (changed) {
            for (EditationModeChangedListener listener : editationModeChangedListeners) {
                listener.editationModeChanged(editationMode);
            }
            caret.resetBlink();
            repaint();
        }
    }

    @Override
    public boolean isHandleClipboard() {
        return handleClipboard;
    }

    @Override
    public void setHandleClipboard(boolean handleClipboard) {
        this.handleClipboard = handleClipboard;
    }

    @Nonnull
    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(@Nonnull Font font) {
        this.font = font;
        painter.reset();
        repaint();
    }

    @Nonnull
    @Override
    public BasicBackgroundPaintMode getBackgroundPaintMode() {
        return borderPaintMode;
    }

    @Override
    public void setBackgroundPaintMode(@Nonnull BasicBackgroundPaintMode borderPaintMode) {
        this.borderPaintMode = borderPaintMode;
        repaint();
    }

    @Override
    public boolean isRowWrapping() {
        return lineWrapping;
    }

    @Override
    public void setLineWrapping(boolean lineWrapping) {
        this.lineWrapping = lineWrapping;
        updateLayout();
    }

    @Override
    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    @Override
    public void setWrappingBytesGroupSize(int wrappingBytesGroupSize) {
        this.wrappingBytesGroupSize = wrappingBytesGroupSize;
        updateLayout();
    }

    @Override
    public int getMaxBytesPerRow() {
        return maxBytesPerLine;
    }

    @Override
    public void setMaxBytesPerLine(int maxBytesPerLine) {
        this.maxBytesPerLine = maxBytesPerLine;
    }

    public void notifySelectionChanged() {
        for (SelectionChangedListener listener : selectionChangedListeners) {
            listener.selectionChanged(selection);
        }
    }

    @Override
    public void notifyCaretMoved() {
        for (CaretMovedListener listener: caretMovedListeners) {
            listener.caretMoved(caret.getCaretPosition());
        }
    }

    @Override
    public void notifyScrolled() {
        for (ScrollingListener listener : scrollingListeners) {
            listener.scrolled();
        }
    }

    @Override
    public void addSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    @Override
    public void removeSelectionChangedListener(@Nullable SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    @Override
    public void addCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    @Override
    public void removeCaretMovedListener(@Nullable CaretMovedListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    @Override
    public void addScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    @Override
    public void removeScrollingListener(@Nullable ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    @Override
    public void addEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.add(editationModeChangedListener);
    }

    @Override
    public void removeEditationModeChangedListener(@Nullable EditationModeChangedListener editationModeChangedListener) {
        editationModeChangedListeners.remove(editationModeChangedListener);
    }
}
