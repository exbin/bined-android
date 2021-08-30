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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.view.KeyEvent;

import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaCommandHandler;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default binary editor command handler.
 *
 * @version 0.2.0 2018/08/17
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int NO_MODIFIER = 0;
    public static final int LAST_CONTROL_CODE = 31;
    private static final char DELETE_CHAR = (char) 0x7f;

    @Nonnull
    private final CodeAreaCore codeArea;
    @Nonnull
    private EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private ClipboardManager clipboard;
    private boolean canPaste = false;
    private ClipDescription binaryDataFlavor;
    private ClipData currentClipboardData = null;

    public DefaultCodeAreaCommandHandler(Context context, CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        codeTypeSupported = codeArea instanceof CodeTypeCapable;
        viewModeSupported = codeArea instanceof ViewModeCapable;

        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                updateCanPaste();
            }
        });

        binaryDataFlavor = new ClipDescription("Binary Data", new String[] { CodeAreaUtils.MIME_CLIPBOARD_BINARY });
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
        return new  CodeAreaCommandHandlerFactory() {
            @Nonnull
            @Override
            public CodeAreaCommandHandler createCommandHandler(@Nonnull CodeAreaCore codeArea) {
                return new DefaultCodeAreaCommandHandler(context, codeArea);
            }
        };
    }

    @Override
    public void undoSequenceBreak() {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                move(keyEvent.getModifiers(), MovementDirection.LEFT);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                move(keyEvent.getModifiers(), MovementDirection.RIGHT);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                move(keyEvent.getModifiers(), MovementDirection.UP);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                move(keyEvent.getModifiers(), MovementDirection.DOWN);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_MOVE_HOME: {
                if ((keyEvent.getModifiers() & KeyEvent.META_CTRL_MASK) > 0) {
                    move(keyEvent.getModifiers(), MovementDirection.DOC_START);
                } else {
                    move(keyEvent.getModifiers(), MovementDirection.ROW_START);
                }
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_MOVE_END: {
                if ((keyEvent.getModifiers() & KeyEvent.META_CTRL_MASK) > 0) {
                    move(keyEvent.getModifiers(), MovementDirection.DOC_END);
                } else {
                    move(keyEvent.getModifiers(), MovementDirection.ROW_END);
                }
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(keyEvent.getModifiers(), MovementDirection.PAGE_UP);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(keyEvent.getModifiers(), MovementDirection.PAGE_DOWN);
                undoSequenceBreak();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_INSERT: {
                EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
                if (editMode == EditMode.EXPANDING || editMode == EditMode.CAPPED) {
                    EditOperation editOperation = ((EditModeCapable) codeArea).getEditOperation();
                    switch (editOperation) {
                        case INSERT: {
                            ((EditModeCapable) codeArea).setEditOperation(EditOperation.OVERWRITE);
//                            keyEvent.consume();
                            break;
                        }
                        case OVERWRITE: {
                            ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
//                            keyEvent.consume();
                            break;
                        }
                    }
                }
                break;
            }
            case KeyEvent.KEYCODE_TAB: {
                if (viewModeSupported && ((ViewModeCapable) codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                    move(keyEvent.getModifiers(), MovementDirection.SWITCH_SECTION);
                    undoSequenceBreak();
                    revealCursor();
//                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.KEYCODE_ENTER: {
                enterPressed();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DEL: {
                EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
                if (editMode == EditMode.EXPANDING) {
                    deletePressed();
//                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.KEYCODE_BACK: {
                EditMode editnMode = ((EditModeCapable) codeArea).getEditMode();
                if (editnMode == EditMode.EXPANDING) {
                    backSpacePressed();
//                    keyEvent.consume();
                }
                break;
            }
            default: {
                if (((ClipboardCapable) codeArea).getClipboardHandlingMode() == ClipboardHandlingMode.PROCESS) {
                    if ((keyEvent.getModifiers() & KeyEvent.META_CTRL_MASK) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_C) {
                        copy();
//                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & KeyEvent.META_CTRL_MASK) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_X) {
                        cut();
//                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & KeyEvent.META_CTRL_MASK) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_V) {
                        paste();
//                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & KeyEvent.META_CTRL_MASK) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_A) {
                        codeArea.selectAll();
//                        keyEvent.consume();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(int keyCode, KeyEvent keyEvent) {
        char keyValue = (char) keyCode;
        // TODO Add support for high unicode codes
        if (keyValue == KeyEvent.KEYCODE_UNKNOWN) {
            return;
        }
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        if (caret.getSection() == BasicCodeAreaSection.CODE_MATRIX) {
            pressedCharAsCode(keyValue);
        } else {
            char keyChar = keyValue;
            if (keyChar > LAST_CONTROL_CODE && keyValue != DELETE_CHAR) {
                pressedCharInPreview(keyChar);
            }
        }
    }

    private void pressedCharAsCode(char keyChar) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyChar, codeOffset, codeType);
        if (validKey) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                deleteSelection();
            }

            int value;
            if (keyChar >= '0' && keyChar <= '9') {
                value = keyChar - '0';
            } else {
                value = Character.toLowerCase(keyChar) - 'a' + 10;
            }

            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.INSERT) {
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
            } else {
                if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE && dataPosition == codeArea.getDataSize()) {
                    ((EditableBinaryData) data).insert(dataPosition, 1);
                }
                setCodeValue(value);
            }
            codeArea.notifyDataChanged();
            move(NO_MODIFIER, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
            CodeAreaCaretPosition caretPosition = caret.getCaretPosition();

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if (editMode == EditMode.INPLACE) {
                int length = bytes.length;
                if (dataPosition + length > codeArea.getDataSize()) {
                    return;
                }
            }
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                deleteSelection();
            }

            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
            if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
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
            ((CaretCapable) codeArea).getCaret().setCaretPosition(dataPosition + bytes.length - 1);
            move(NO_MODIFIER, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void setCodeValue(int value) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getCaret().getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
        CodeType codeType = getCodeType();
        byte byteValue = data.getByte(dataPosition);
        byte outputValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) data).setByte(dataPosition, outputValue);
    }

    @Override
    public void enterPressed() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        if (caret.getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            String sequence = enterKeyHandlingMode.getSequence();
            if (!sequence.isEmpty()) {
                pressedCharInPreview(sequence.charAt(0));
                if (sequence.length() == 2) {
                    pressedCharInPreview(sequence.charAt(1));
                }
            }
        }
    }

    @Override
    public void backSpacePressed() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }
        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= codeArea.getDataSize()) {
                caret.setCodeOffset(0);
                move(NO_MODIFIER, MovementDirection.LEFT);
                caret.setCodeOffset(0);
                ((EditableBinaryData) data).remove(dataPosition - 1, 1);
                codeArea.notifyDataChanged();
                revealCursor();
                clearSelection();
            }
        }
    }

    @Override
    public void deletePressed() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            revealCursor();
        } else {
            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition < codeArea.getDataSize()) {
                ((EditableBinaryData) data).remove(dataPosition, 1);
                codeArea.notifyDataChanged();
                if (caret.getCodeOffset() > 0) {
                    caret.setCodeOffset(0);
                }
                ((CaretCapable) codeArea).setCaretPosition(caret.getCaretPosition());
                clearSelection();
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

        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selection.isEmpty()) {
            return;
        }

        long first = selection.getFirst();
        long last = selection.getLast();
        ((EditableBinaryData) data).remove(first, last - first + 1);
        codeArea.clearSelection();
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        caret.setCaretPosition(first);
        ((CaretCapable) codeArea).setCaretPosition(first);
        clearSelection();
        revealCursor();
    }

    @Override
    public void delete() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

        deleteSelection();
        codeArea.notifyDataChanged();
    }

    @Override
    public void copy() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                return;
            }

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            ClipData clipData = new ClipData(binaryDataFlavor, new ClipData.Item(new Intent() {
                @Nullable
                @Override
                public Uri getData() {
                    return super.getData();
                }
            }));
//            CodeAreaUtils.BinaryDataClipboardData binaryData = new CodeAreaUtils.BinaryDataClipboardData(copy, binaryDataFlavor);
            setClipboardContent(clipData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();
            if (data == null) {
                return;
            }

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea).getCodeType();
            CodeCharactersCase charactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
            ClipData clipData = null; //new ClipData("test");
//            CodeAreaUtils.CodeDataClipboardData binaryData = new CodeAreaUtils.CodeDataClipboardData(copy, binaryDataFlavor, codeType, charactersCase);
            setClipboardContent(clipData);
        }
    }

    private void setClipboardContent(ClipData content) {
        clearClipboardData();
        try {
            currentClipboardData = content;
            clipboard.setPrimaryClip(content);
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore and clear
            clearClipboardData();
        }
    }

    private void clearClipboardData() {
//        if (currentClipboardData != null) {
//            currentClipboardData.dispose();
//            currentClipboardData = null;
//        }
    }

    @Override
    public void cut() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            copy();
            deleteSelection();
            codeArea.notifyDataChanged();
        }
    }

    @Override
    public void paste() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

//        try {
//            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
//                if (codeArea.hasSelection()) {
//                    deleteSelection();
//                    codeArea.notifyDataChanged();
//                }
//
//                try {
//                    Object object = clipboard.getData(binaryDataFlavor);
//                    if (object instanceof BinaryData) {
//                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
//                        long dataPosition = caret.getDataPosition();
//
//                        BinaryData clipboardData = (BinaryData) object;
//                        long dataSize = clipboardData.getDataSize();
//                        if (((EditModeCapable) codeArea).getEditMode() == EditMode.OVERWRITE) {
//                            long toRemove = dataSize;
//                            if (dataPosition + toRemove > codeArea.getDataSize()) {
//                                toRemove = codeArea.getDataSize() - dataPosition;
//                            }
//                            ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, toRemove);
//                        }
//                        ((EditableBinaryData) codeArea.getContentData()).insert(dataPosition, clipboardData);
//                        codeArea.notifyDataChanged();
//
//                        caret.setCaretPosition(caret.getDataPosition() + dataSize);
//                        caret.setCodeOffset(0);
//                        updateScrollBars();
//                        notifyCaretMoved();
//                        revealCursor();
//                    }
//                } catch (UnsupportedFlavorException | IOException ex) {
//                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
//                if (codeArea.hasSelection()) {
//                    deleteSelection();
//                    codeArea.notifyDataChanged();
//                }
//
//                Object insertedData;
//                try {
//                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
//                    if (insertedData instanceof String) {
//                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
//                        long dataPosition = caret.getDataPosition();
//
//                        byte[] bytes = ((String) insertedData).getBytes(Charset.forName(CodeAreaUtils.DEFAULT_ENCODING));
//                        int length = bytes.length;
//                        if (((EditModeCapable) codeArea).getEditMode() == EditMode.OVERWRITE) {
//                            long toRemove = length;
//                            if (dataPosition + toRemove > codeArea.getDataSize()) {
//                                toRemove = codeArea.getDataSize() - dataPosition;
//                            }
//                            ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, toRemove);
//                        }
//                        ((EditableBinaryData) codeArea.getContentData()).insert(dataPosition, bytes);
//                        codeArea.notifyDataChanged();
//
//                        caret.setCaretPosition(caret.getDataPosition() + length);
//                        caret.setCodeOffset(0);
//                        updateScrollBars();
//                        notifyCaretMoved();
//                        revealCursor();
//                    }
//                } catch (UnsupportedFlavorException | IOException ex) {
//                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        } catch (IllegalStateException ex) {
//            // Clipboard not available - ignore
//        }
    }

    @Override
    public void pasteFromCode() {
        if (!((EditModeCapable) codeArea).isEditable()) {
            return;
        }

//        try {
//            if (!clipboard.isDataFlavorAvailable(binaryDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
//                return;
//            }
//        } catch (IllegalStateException ex) {
//            return;
//        }
//
//        try {
//            if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
//                paste();
//            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
//                if (codeArea.hasSelection()) {
//                    deleteSelection();
//                    codeArea.notifyDataChanged();
//                }
//
//                Object insertedData;
//                try {
//                    insertedData = clipboard.getData(DataFlavor.stringFlavor);
//                    if (insertedData instanceof String) {
//                        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
//                        long dataPosition = caret.getDataPosition();
//
//                        CodeType codeType = getCodeType();
//                        ByteArrayEditableData pastedData = new ByteArrayEditableData();
//                        CodeAreaUtils.insertHexStringIntoData((String) insertedData, pastedData, codeType);
//
//                        long length = pastedData.getDataSize();
//                        if (((EditModeCapable) codeArea).getEditMode() == EditMode.OVERWRITE) {
//                            long toRemove = length;
//                            if (dataPosition + toRemove > codeArea.getDataSize()) {
//                                toRemove = codeArea.getDataSize() - dataPosition;
//                            }
//                            ((EditableBinaryData) codeArea.getContentData()).remove(dataPosition, toRemove);
//                        }
//                        ((EditableBinaryData) codeArea.getContentData()).insert(caret.getDataPosition(), pastedData);
//                        codeArea.notifyDataChanged();
//
//                        caret.setCaretPosition(caret.getDataPosition() + length);
//                        caret.setCodeOffset(0);
//                        updateScrollBars();
//                        notifyCaretMoved();
//                        revealCursor();
//                    }
//                } catch (UnsupportedFlavorException | IOException ex) {
//                    Logger.getLogger(DefaultCodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        } catch (IllegalStateException ex) {
//            // Clipboard not available - ignore
//        }
    }

    @Override
    public boolean canPaste() {
        return canPaste;
    }

    @Override
    public void selectAll() {
        long dataSize = codeArea.getDataSize();
        if (dataSize > 0) {
            ((SelectionCapable) codeArea).setSelection(0, dataSize);
        }
    }

    @Override
    public void clearSelection() {
        ((SelectionCapable) codeArea).clearSelection();
    }

    public void updateSelection(SelectingMode selecting, CodeAreaCaretPosition caretPosition) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selecting == SelectingMode.SELECTING) {
            ((SelectionCapable) codeArea).setSelection(selection.getStart(), caret.getDataPosition());
        } else {
            ((SelectionCapable) codeArea).setSelection(caret.getDataPosition(), caret.getDataPosition());
        }
    }

    private void updateCanPaste() {
        canPaste = clipboard.hasPrimaryClip(); // CodeAreaUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void moveCaret(int positionX, int positionY, SelectingMode selecting) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).mousePositionToClosestCaretPosition(positionX, positionY, CaretOverlapMode.PARTIAL_OVERLAP);
        if (caretPosition != null) {
            ((CaretCapable) codeArea).getCaret().setCaretPosition(caretPosition);
            updateSelection(selecting, caretPosition);

            undoSequenceBreak();
            codeArea.repaint();
        }
    }

    public void move(int modifiers, MovementDirection direction) {
        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        CodeAreaCaretPosition movePosition = ((CaretCapable) codeArea).computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            caret.setCaretPosition(movePosition);
            updateSelection((modifiers & KeyEvent.META_SHIFT_MASK) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE, movePosition);
        }
    }

    public void scroll(ScrollingDirection direction) {
        CodeAreaScrollPosition sourcePosition = ((ScrollingCapable) codeArea).getScrollPosition();
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) codeArea).computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            ((ScrollingCapable) codeArea).setScrollPosition(scrollPosition);
            codeArea.resetPainter();
        }
    }

    @Override
    public void wheelScroll(int scrollSize, ScrollbarOrientation direction) {
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
        return ((CharsetCapable) codeArea).getCharset().canEncode();
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = ((CharsetCapable) codeArea).getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Nonnull
    private CodeType getCodeType() {
        if (codeTypeSupported) {
            return ((CodeTypeCapable) codeArea).getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    private void revealCursor() {
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.repaint();
    }

    @Override
    public boolean checkEditAllowed() {
        return ((EditModeCapable) codeArea).isEditable();
    }
}
