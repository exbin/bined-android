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

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.IdentityScope;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.bined.android.CodeArea;
import org.exbin.bined.android.CodeAreaCommandHandler;
import org.exbin.bined.android.CodeAreaWorker;
import org.exbin.bined.android.MovementDirection;
import org.exbin.bined.android.ScrollingDirection;
import org.exbin.bined.android.capability.ScrollingCapable;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Default hexadecimal editor command handler.
 *
 * @version 0.2.0 2018/05/08
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int NO_MODIFIER = 0;

    @Nonnull
    private final CodeArea codeArea;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private ClipboardManager clipboard;
    private boolean canPaste = false;
//    private Handler binaryDataFlavor;
    private CodeAreaUtils.ClipboardData currentClipboardData = null;

    public DefaultCodeAreaCommandHandler(Context context, @Nonnull CodeArea codeArea) {
        this.codeArea = codeArea;
        CodeAreaWorker worker = codeArea.getWorker();
        codeTypeSupported = worker instanceof CodeTypeCapable;
        viewModeSupported = worker instanceof ViewModeCapable;

        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

//        try {
//            clipboard.addFlavorListener((FlavorEvent e) -> {
//                updateCanPaste();
//            });
//            try {
//                binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
//            } catch (ClassNotFoundException ex) {
//                Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            updateCanPaste();
//        } catch (IllegalStateException ex) {
//            canPaste = false;
//        } catch (java.awt.HeadlessException ex) {
//            Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Nonnull
    public static CodeAreaCommandHandler.CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory(final Context context) {
        return new CodeAreaCommandHandlerFactory() {
            @Nonnull
            @Override
            public CodeAreaCommandHandler createCommandHandler(@Nonnull CodeArea codeArea) {
                return new DefaultCodeAreaCommandHandler(context, codeArea);
            }
        };
    }

    @Override
    public void undoSequenceBreak() {
        // Do nothing
    }

    @Override
    public void keyPressed(@Nonnull KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                move(keyEvent.getModifiers(), MovementDirection.LEFT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                move(keyEvent.getModifiers(), MovementDirection.RIGHT);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                move(keyEvent.getModifiers(), MovementDirection.UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                move(keyEvent.getModifiers(), MovementDirection.DOWN);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                if ((keyEvent.getModifiers() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    move(keyEvent.getModifiers(), MovementDirection.DOC_START);
                } else {
                    move(keyEvent.getModifiers(), MovementDirection.ROW_START);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                if ((keyEvent.getModifiers() & KeyEvent.CTRL_DOWN_MASK) > 0) {
                    move(keyEvent.getModifiers(), MovementDirection.DOC_END);
                } else {
                    move(keyEvent.getModifiers(), MovementDirection.ROW_END);
                }
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(keyEvent.getModifiers(), MovementDirection.PAGE_UP);
                undoSequenceBreak();
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(keyEvent.getModifiers(), MovementDirection.PAGE_DOWN);
                undoSequenceBreak();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_INSERT: {
                EditationMode editationMode = ((EditationModeCapable) codeArea.getWorker()).getEditationMode();
                switch (editationMode) {
                    case INSERT: {
                        ((EditationModeCapable) codeArea.getWorker()).setEditationMode(EditationMode.OVERWRITE);
                        keyEvent.consume();
                        break;
                    }
                    case OVERWRITE: {
                        ((EditationModeCapable) codeArea.getWorker()).setEditationMode(EditationMode.INSERT);
                        keyEvent.consume();
                        break;
                    }
                }
                break;
            }
            case KeyEvent.KEYCODE_TAB: {
                if (viewModeSupported && ((ViewModeCapable) codeArea.getWorker()).getViewMode() == CodeAreaViewMode.DUAL) {
                    move(keyEvent.getModifiers(), MovementDirection.SWITCH_SECTION);
                    undoSequenceBreak();
                    revealCursor();
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.KEYCODE_DEL: {
                deletePressed();
                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_BACK: {
                backSpacePressed();
                keyEvent.consume();
                break;
            }
            default: {
                if (((ClipboardCapable) codeArea.getWorker()).isHandleClipboard()) {
                    if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_C) {
                        copy();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_X) {
                        cut();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_V) {
                        paste();
                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_A) {
                        codeArea.selectAll();
                        keyEvent.consume();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(@Nonnull KeyEvent keyEvent) {
        char keyValue = keyEvent.getKeyChar();
        // TODO Add support for high unicode codes
        if (keyValue == KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getSection() == BasicCodeAreaSection.CODE_MATRIX.getSection()) {
            long dataPosition = caretPosition.getDataPosition();
            int codeOffset = caretPosition.getCodeOffset();
            CodeType codeType = getCodeType();
            boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyValue, codeOffset, codeType);
            if (validKey) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                }

                int value;
                if (keyValue >= '0' && keyValue <= '9') {
                    value = keyValue - '0';
                } else {
                    value = Character.toLowerCase(keyValue) - 'a' + 10;
                }

                BinaryData data = codeArea.getContentData();
                if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
                    if (dataPosition == codeArea.getDataSize()) {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setCodeValue(value);
                } else {
                    if (codeOffset > 0) {
                        byte byteRest = data.getByte(dataPosition);
                        switch (codeType) {
                            case BINARY: {
                                byteRest = (byte) (byteRest & (0xff >> codeOffset));
                                break;
                            }
                            case DECIMAL: {
                                byteRest = (byte) (byteRest % (codeOffset == 1 ? 100 : 10));
                                break;
                            }
                            case OCTAL: {
                                byteRest = (byte) (byteRest % (codeOffset == 1 ? 64 : 8));
                                break;
                            }
                            case HEXADECIMAL: {
                                byteRest = (byte) (byteRest & 0xf);
                                break;
                            }
                            default:
                                throw new IllegalStateException("Unexpected code type " + codeType.name());
                        }
                        if (byteRest > 0) {
                            ((EditableBinaryData) data).insert(dataPosition + 1, 1);
                            ((EditableBinaryData) data).setByte(dataPosition, (byte) (data.getByte(dataPosition) - byteRest));
                            ((EditableBinaryData) data).setByte(dataPosition + 1, byteRest);
                        }
                    } else {
                        ((EditableBinaryData) data).insert(dataPosition, 1);
                    }
                    setCodeValue(value);
                }
                codeArea.notifyDataChanged();
                move(NO_MODIFIER, MovementDirection.RIGHT);
                revealCursor();
            }
        } else {
            char keyChar = keyValue;
            if (keyChar > 31 && isValidChar(keyValue)) {
                BinaryData data = codeArea.getContentData();
                long dataPosition = caretPosition.getDataPosition();
                byte[] bytes = charToBytes(keyChar);
                if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
                    if (dataPosition < codeArea.getDataSize()) {
                        int length = bytes.length;
                        if (dataPosition + length > codeArea.getDataSize()) {
                            length = (int) (codeArea.getDataSize() - dataPosition);
                        }
                        ((EditableBinaryData) data).remove(dataPosition, length);
                    }
                }
                ((EditableBinaryData) data).insert(dataPosition, bytes);
                codeArea.notifyDataChanged();
                ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(dataPosition + bytes.length - 1);
                move(NO_MODIFIER, MovementDirection.RIGHT);
                revealCursor();
            }
        }
    }

    private void setCodeValue(int value) {
        CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).getCaret().getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData data = codeArea.getContentData();
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
    }

    @Override
    public void backSpacePressed() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                move(NO_MODIFIER, MovementDirection.LEFT);
                caret.setCodeOffset(0);
                ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                revealCursor();
                updateScrollBars();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            updateScrollBars();
            notifyCaretMoved();
            revealCursor();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                updateScrollBars();
                notifyCaretMoved();
                revealCursor();
            }
        }
    }

    private void deleteSelection() {
        BinaryData data = codeArea.getContentData();
        if (data == null) {
            return;
        }
        if (!(data instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (selection.isEmpty()) {
            return;
        }

        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) data).remove(first, last - first + 1);
        codeArea.clearSelection();
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        caret.setCaretPosition(first);
        revealCursor();
        updateScrollBars();
    }

    @Override
    public void delete() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        deleteSelection();
        codeArea.notifyDataChanged();
    }

    @Override
    public void copy() {
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                return;
            }

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeAreaUtils.BinaryDataClipboardData binaryData = new CodeAreaUtils.BinaryDataClipboardData(copy, binaryDataFlavor);
            setClipboardContent(binaryData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                return;
            }

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea.getWorker()).getCodeType();
            CodeCharactersCase charactersCase = ((CodeCharactersCaseCapable) codeArea.getWorker()).getCodeCharactersCase();
            CodeAreaUtils.CodeDataClipboardData binaryData = new CodeAreaUtils.CodeDataClipboardData(copy, binaryDataFlavor, codeType, charactersCase);
            setClipboardContent(binaryData);
        }
    }

    private void setClipboardContent(@Nonnull CodeAreaUtils.ClipboardData content) {
        clearClipboardData();
        try {
            currentClipboardData = content;
            clipboard.setContents(content, content);
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore and clear
            clearClipboardData();
        }
    }

    private void clearClipboardData() {
        if (currentClipboardData != null) {
            currentClipboardData.dispose();
            currentClipboardData = null;
        }
    }

    @Override
    public void cut() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (!selection.isEmpty()) {
            copy();
            deleteSelection();
            codeArea.notifyDataChanged();
        }
    }

    @Override
    public void paste() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                try {
                    Object object = clipboard.getData(binaryDataFlavor);
                    if (object instanceof BinaryData) {
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
                        long dataPosition = caret.getDataPosition();

                        BinaryData clipboardData = (BinaryData) object;
                        long dataSize = clipboardData.getDataSize();
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = dataSize;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getContentData()).insert(dataPosition, clipboardData);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + dataSize);
                        caret.setCodeOffset(0);
                        updateScrollBars();
                        notifyCaretMoved();
                        revealCursor();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                Object insertedData;
                try {
                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
                    if (insertedData instanceof String) {
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
                        long dataPosition = caret.getDataPosition();

                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(CodeAreaUtils.DEFAULT_ENCODING));
                        int length = bytes.length;
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = length;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getContentData()).insert(dataPosition, bytes);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        updateScrollBars();
                        notifyCaretMoved();
                        revealCursor();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    @Override
    public void pasteFromCode() {
        if (!((EditationModeCapable) codeArea.getWorker()).isEditable()) {
            return;
        }

        try {
            if (!clipboard.isDataFlavorAvailable(binaryDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                paste();
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                Object insertedData;
                try {
                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
                    if (insertedData instanceof String) {
                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
                        long dataPosition = caret.getDataPosition();

                        CodeType codeType = getCodeType();
                        ByteArrayEditableData pastedData = new ByteArrayEditableData();
                        CodeAreaUtils.insertHexStringIntoData((String) insertedData, pastedData, codeType);

                        long length = pastedData.getDataSize();
                        if (((EditationModeCapable) codeArea.getWorker()).getEditationMode() == EditationMode.OVERWRITE) {
                            long toRemove = length;
                            if (dataPosition + toRemove > codeArea.getDataSize()) {
                                toRemove = codeArea.getDataSize() - dataPosition;
                            }
                            ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, toRemove);
                        }
                        ((EditableBinaryData) codeArea.getContentData()).insert(caret.getDataPosition(), pastedData);
                        codeArea.notifyDataChanged();

                        caret.setCaretPosition(caret.getDataPosition() + length);
                        caret.setCodeOffset(0);
                        updateScrollBars();
                        notifyCaretMoved();
                        revealCursor();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    @Override
    public void selectAll() {
        long dataSize = codeArea.getDataSize();
        if (dataSize > 0) {
            ((SelectionCapable) codeArea.getWorker()).setSelection(0, dataSize - 1);
        }
    }

    @Override
    public void clearSelection() {
        ((SelectionCapable) codeArea.getWorker()).clearSelection();
    }

    public void updateSelection(boolean selecting, @Nonnull CaretPosition caretPosition) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        SelectionRange selection = ((SelectionCapable) codeArea.getWorker()).getSelection();
        if (selecting) {
            ((SelectionCapable) codeArea.getWorker()).setSelection(selection.getStart(), caret.getDataPosition());
        } else {
            ((SelectionCapable) codeArea.getWorker()).setSelection(caret.getDataPosition(), caret.getDataPosition());
        }
    }

    private void updateCanPaste() {
        canPaste = CodeAreaUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void moveCaret(int positionX, int positionY, boolean selecting) {
        CaretPosition caretPosition = ((CaretCapable) codeArea.getWorker()).mousePositionToClosestCaretPosition(positionX, positionY);
        if (caretPosition != null) {
            ((CaretCapable) codeArea.getWorker()).getCaret().setCaretPosition(caretPosition);
            updateSelection(selecting, caretPosition);

            notifyCaretMoved();
            undoSequenceBreak();
            codeArea.repaint();
        }
    }

    public void move(int modifiers, @Nonnull MovementDirection direction) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea.getWorker()).getCaret();
        CaretPosition caretPosition = caret.getCaretPosition();
        CaretPosition movePosition = codeArea.getWorker().computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            caret.setCaretPosition(movePosition);
            updateSelection((modifiers & KeyEvent.SHIFT_DOWN_MASK) > 0, movePosition);
            notifyCaretMoved();
        }
    }

    public void scroll(@Nonnull ScrollingDirection direction) {
        CodeAreaScrollPosition sourcePosition = ((ScrollingCapable) codeArea.getWorker()).getScrollPosition();
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea.getWorker()).computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            ((ScrollingCapable) codeArea.getWorker()).setScrollPosition(scrollPosition);
            codeArea.resetPainter();
            notifyScrolled();
            updateScrollBars();
        }
    }

    @Override
    public void wheelScroll(int scrollSize, @Nonnull ScrollbarOrientation direction) {
        // TODO
        scroll(ScrollingDirection.UP);

//        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
//
//        if (e.isShiftDown() && codeArea.getPainter().isHorizontalScrollBarVisible()) {
//            if (e.getWheelRotation() > 0) {
//                if (codeArea.getBytesPerRectangle() < codeArea.getCharactersPerLine()) {
//                    int maxScroll = codeArea.getCharactersPerLine() - codeArea.getBytesPerRectangle();
//                    if (scrollPosition.getScrollCharPosition() < maxScroll - MOUSE_SCROLL_LINES) {
//                        scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() + MOUSE_SCROLL_LINES);
//                    } else {
//                        scrollPosition.setScrollCharPosition(maxScroll);
//                    }
//                    codeArea.getPainter().updateScrollBars();
//                    codeArea.notifyScrolled();
//                }
//            } else if (scrollPosition.getScrollCharPosition() > 0) {
//                if (scrollPosition.getScrollCharPosition() > MOUSE_SCROLL_LINES) {
//                    scrollPosition.setScrollCharPosition(scrollPosition.getScrollCharPosition() - MOUSE_SCROLL_LINES);
//                } else {
//                    scrollPosition.setScrollCharPosition(0);
//                }
//                codeArea.getPainter().updateScrollBars();
//                codeArea.notifyScrolled();
//            }
//        } else if (e.getWheelRotation() > 0) {
//            long lines = (codeArea.getDataSize() + scrollPosition.getLineDataOffset()) / codeArea.getBytesPerLine();
//            if (lines * codeArea.getBytesPerLine() < codeArea.getDataSize()) {
//                lines++;
//            }
//            lines -= codeArea.getLinesPerRectangle();
//            if (scrollPosition.getScrollLinePosition() < lines) {
//                if (scrollPosition.getScrollLinePosition() < lines - MOUSE_SCROLL_LINES) {
//                    scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() + MOUSE_SCROLL_LINES);
//                } else {
//                    scrollPosition.setScrollLinePosition(lines);
//                }
//                codeArea.getPainter().updateScrollBars();
//                codeArea.notifyScrolled();
//            }
//        } else if (scrollPosition.getScrollLinePosition() > 0) {
//            if (scrollPosition.getScrollLinePosition() > MOUSE_SCROLL_LINES) {
//                scrollPosition.setScrollLinePosition(scrollPosition.getScrollLinePosition() - MOUSE_SCROLL_LINES);
//            } else {
//                scrollPosition.setScrollLinePosition(0);
//            }
//            codeArea.getPainter().updateScrollBars();
//            codeArea.notifyScrolled();
//        }
    }

    public boolean isValidChar(char value) {
        return ((CharsetCapable) codeArea.getWorker()).getCharset().canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = ((CharsetCapable) codeArea.getWorker()).getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Nonnull
    private CodeType getCodeType() {
        if (codeTypeSupported) {
            return ((CodeTypeCapable) codeArea.getWorker()).getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    private void revealCursor() {
        ((CaretCapable) codeArea.getWorker()).revealCursor();
        codeArea.repaint();
    }

    private void notifyCaretMoved() {
        ((CaretCapable) codeArea.getWorker()).notifyCaretMoved();
    }

    private void notifyScrolled() {
        ((ScrollingCapable) codeArea.getWorker()).notifyScrolled();
    }

    private void updateScrollBars() {
        ((ScrollingCapable) codeArea.getWorker()).updateScrollBars();
    }
}
