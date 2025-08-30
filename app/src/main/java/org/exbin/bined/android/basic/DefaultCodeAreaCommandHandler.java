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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;

import android.view.KeyEvent;

import org.exbin.auxiliary.binary_data.jna.JnaBufferData;
import org.exbin.auxiliary.binary_data.jna.JnaBufferEditableData;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.ScrollBarOrientation;
import org.exbin.bined.android.CodeAreaAndroidUtils;
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
import org.exbin.bined.basic.SelectingMode;
import org.exbin.bined.basic.TabKeyHandlingMode;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default binary editor command handler.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {

    public static final int LAST_CONTROL_CODE = 31;
    private static final char DELETE_CHAR = (char) 0x7f;

    private final int metaMask = KeyEvent.META_CTRL_MASK;

    @Nonnull
    protected final CodeAreaCore codeArea;
    @Nonnull
    protected EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    @Nonnull
    protected TabKeyHandlingMode tabKeyHandlingMode = TabKeyHandlingMode.PLATFORM_SPECIFIC;
    protected final boolean codeTypeSupported;
    protected final boolean viewModeSupported;

    protected Context context;
    protected ClipboardManager clipboard;
    protected boolean canPaste = false;
    protected ClipDescription binedDataFlavor;
    protected ClipDescription binaryDataFlavor;
    protected ClipData currentClipboardData = null;

    public DefaultCodeAreaCommandHandler(Context context, CodeAreaCore codeArea) {
        this.context = context;
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

        binedDataFlavor = new ClipDescription("BinEd Data", new String[] { CodeAreaUtils.BINED_CLIPBOARD_MIME });
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
    public void sequenceBreak() {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                move(isSelectingMode(keyEvent), MovementDirection.LEFT);
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                move(isSelectingMode(keyEvent), MovementDirection.RIGHT);
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                move(isSelectingMode(keyEvent), MovementDirection.UP);
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                move(isSelectingMode(keyEvent), MovementDirection.DOWN);
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_MOVE_HOME: {
                if ((keyEvent.getModifiers() & metaMask) > 0) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_START);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_START);
                }
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_MOVE_END: {
                if ((keyEvent.getModifiers() & metaMask) > 0) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_END);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_END);
                }
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_UP);
                sequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_DOWN);
                sequenceBreak();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_INSERT: {
                if (changeEditOperation()) {
                    // keyEvent.consume();
                }
                break;
            }
            case KeyEvent.KEYCODE_TAB: {
                tabPressed(isSelectingMode(keyEvent));
//                if (tabKeyHandlingMode != TabKeyHandlingMode.IGNORE) {
//                    keyEvent.consume();
//                }
                break;
            }
            case KeyEvent.KEYCODE_ENTER: {
                enterPressed();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_FORWARD_DEL: {
                EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
                if (editMode == EditMode.EXPANDING) {
                    deletePressed();
//                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.KEYCODE_DEL: {
                EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
                if (editMode == EditMode.EXPANDING) {
                    backSpacePressed();
//                    keyEvent.consume();
                }
                break;
            }
            default: {
                if (((ClipboardCapable) codeArea).getClipboardHandlingMode() == ClipboardHandlingMode.PROCESS) {
                    if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_C) {
                        copy();
//                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_X) {
                        cut();
//                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_V) {
                        paste();
//                        keyEvent.consume();
                        break;
                    } else if ((keyEvent.getModifiers() & metaMask) > 0 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_A) {
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
        if (!checkEditAllowed()) {
            return;
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
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
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyChar, codeOffset, codeType);
        if (validKey) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                deleteSelection();
                sequenceBreak();
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
                            throw CodeAreaUtils.getInvalidTypeException(codeType);
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
                if (editMode != EditMode.INPLACE || dataPosition < codeArea.getDataSize()) {
                    setCodeValue(value);
                }
            }
            codeArea.notifyDataChanged();
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if (editMode == EditMode.INPLACE) {
                int length = bytes.length;
                if (dataPosition + length > codeArea.getDataSize()) {
                    return;
                }
            }
            if (codeArea.hasSelection() && editMode != EditMode.INPLACE) {
                sequenceBreak();
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
            ((CaretCapable) codeArea).getCodeAreaCaret().setCaretPosition(dataPosition + bytes.length - 1);
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void setCodeValue(int value) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
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
        if (!checkEditAllowed()) {
            return;
        }

        if (((CaretCapable) codeArea).getActiveSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
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
    public void tabPressed() {
        tabPressed(SelectingMode.NONE);
    }

    public void tabPressed(SelectingMode selectingMode) {
        if (tabKeyHandlingMode == TabKeyHandlingMode.PLATFORM_SPECIFIC || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_NEXT_SECTION || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_PREVIOUS_SECTION) {
            if (viewModeSupported && ((ViewModeCapable) codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                move(selectingMode, MovementDirection.SWITCH_SECTION);
                sequenceBreak();
                revealCursor();
            }
        } else if (((CaretCapable) codeArea).getActiveSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            String sequence = tabKeyHandlingMode == TabKeyHandlingMode.INSERT_TAB ? "\t" : "  ";
            pressedCharInPreview(sequence.charAt(0));
            if (sequence.length() == 2) {
                pressedCharInPreview(sequence.charAt(1));
            }
        }
    }

    @Override
    public void backSpacePressed() {
        if (!checkEditAllowed()) {
            return;
        }
        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        } else {
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
            long dataPosition = ((CaretCapable) codeArea).getDataPosition();
            if (dataPosition == 0 || dataPosition > codeArea.getDataSize()) {
                return;
            }
            caret.setCodeOffset(0);
            move(SelectingMode.NONE, MovementDirection.LEFT);
            caret.setCodeOffset(0);
            ((EditableBinaryData) data).remove(dataPosition - 1, 1);
            codeArea.notifyDataChanged();
            ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
            revealCursor();
            clearSelection();
        }
    }

    @Override
    public void deletePressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
            revealCursor();
        } else {
            BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
            DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
            long dataPosition = caret.getDataPosition();
            if (dataPosition >= codeArea.getDataSize()) {
                return;
            }
            ((EditableBinaryData) data).remove(dataPosition, 1);
            codeArea.notifyDataChanged();
            if (caret.getCodeOffset() > 0) {
                caret.setCodeOffset(0);
            }
            ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
            clearSelection();
            revealCursor();
        }
    }

    private void deleteSelection() {
        BinaryData data = codeArea.getContentData();
        if (!(data instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selection.isEmpty()) {
            return;
        }

        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        long first = selection.getFirst();
        long last = selection.getLast();
        long length = last - first + 1;
        if (editMode == EditMode.INPLACE) {
            ((EditableBinaryData) data).fillData(first, length);
        } else {
            ((EditableBinaryData) data).remove(first, length);
        }
        ((CaretCapable) codeArea).setActiveCaretPosition(first);
        clearSelection();
        revealCursor();
    }

    @Override
    public void delete() {
        if (!checkEditAllowed()) {
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

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            ClipData clipData = CodeAreaAndroidUtils.createBinaryDataClipboardData(context, copy, binaryDataFlavor);
            setClipboardContent(clipData);
        }
    }

    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            BinaryData data = codeArea.getContentData();

            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = data.copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea).getCodeType();
            CodeCharactersCase charactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
            ClipData clipData = CodeAreaAndroidUtils.createCodeDataClipboardData(context, copy, binaryDataFlavor, codeType, charactersCase);
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
        if (!checkEditAllowed()) {
            return;
        }

        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            copy();
            if (editMode == EditMode.EXPANDING) {
                deleteSelection();
                codeArea.notifyDataChanged();
            }
        }
    }

    @Override
    public void paste() {
        if (!checkEditAllowed()) {
            return;
        }

        if (!clipboard.hasPrimaryClip()) {
            return;
        }

        try {
            ClipData primaryClip = clipboard.getPrimaryClip();

            if (primaryClip == null || primaryClip.getItemCount() == 0) {
                return;
            }

            ClipDescription description = primaryClip.getDescription();
            try {
                if (!description.hasMimeType(CodeAreaUtils.BINED_CLIPBOARD_MIME) && !description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY) && !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return;
                }
            } catch (IllegalStateException ex) {
                return;
            }

            ClipData.Item clipItem = primaryClip.getItemAt(0);
            if (description.hasMimeType(CodeAreaUtils.BINED_CLIPBOARD_MIME)) {
                ContentResolver contentResolver = context.getContentResolver();
//                ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient(clipItem.getUri());
//                Object clipboardData = clipboard.getData(binedDataFlavor);
//                if (clipboardData instanceof BinaryData) {
//                    pasteBinaryData((BinaryData) clipboardData);
//                }
            } else if (description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY)) {
                ContentResolver contentResolver = context.getContentResolver();
//                Object clipboardData = clipboard.getData(binaryDataFlavor);
//                if (clipboardData instanceof InputStream) {
//                    // PagedData pastedData = new PagedData();
//                    ByteArrayEditableData pastedData = new ByteArrayEditableData();
//                    pastedData.insert(0, (InputStream) clipboardData, -1);
//                    pasteBinaryData((BinaryData) pastedData);
//                }
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                CharSequence clipboardData = clipItem.getText();
                if (clipboardData != null) {
                    byte[] bytes = clipboardData.toString().getBytes(Charset.forName(CodeAreaAndroidUtils.DEFAULT_ENCODING));
                    BinaryData pastedData = new JnaBufferData(bytes);
                    pasteBinaryData(pastedData);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    private void pasteBinaryData(BinaryData pastedData) {
        BinaryData data = codeArea.getContentData();
        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();

        if (codeArea.hasSelection()) {
            deleteSelection();
            codeArea.notifyDataChanged();
        }

        DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
        long dataPosition = caret.getDataPosition();

        long clipDataSize = pastedData.getDataSize();
        long toReplace = clipDataSize;
        if (editMode == EditMode.INPLACE) {
            if (dataPosition + toReplace > codeArea.getDataSize()) {
                toReplace = codeArea.getDataSize() - dataPosition;
            }
            ((EditableBinaryData) data).replace(dataPosition, pastedData, 0, toReplace);
        } else {
            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                if (dataPosition + toReplace > codeArea.getDataSize()) {
                    toReplace = codeArea.getDataSize() - dataPosition;
                }
                ((EditableBinaryData) data).remove(dataPosition, toReplace);
            }

            ((EditableBinaryData) data).insert(dataPosition, pastedData);
            caret.setCaretPosition(caret.getDataPosition() + clipDataSize);
            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
        }

        caret.setCodeOffset(0);
        ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
        sequenceBreak();
        codeArea.notifyDataChanged();
        revealCursor();
        clearSelection();
    }

    public void pasteFromCode() {
        if (!checkEditAllowed()) {
            return;
        }

        if (!clipboard.hasPrimaryClip()) {
            return;
        }

        ClipData primaryClip = clipboard.getPrimaryClip();
        ClipDescription description = primaryClip.getDescription();

        BinaryData data = CodeAreaUtils.requireNonNullContentData(codeArea.getContentData());
        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
        try {
            if (!description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY) && !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

        try {
            if (description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY)) {
                paste();
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                if (codeArea.hasSelection()) {
                    deleteSelection();
                    codeArea.notifyDataChanged();
                }

                Object insertedData;
                CharSequence clipboardData = primaryClip.getItemAt(0).getText();
                if (clipboardData != null) {
                    insertedData = clipboardData.toString();
                    DefaultCodeAreaCaret caret = (DefaultCodeAreaCaret) ((CaretCapable) codeArea).getCodeAreaCaret();
                    long dataPosition = caret.getDataPosition();

                    CodeType codeType = getCodeType();
                    EditableBinaryData pastedData = new JnaBufferEditableData();
                    CodeAreaUtils.insertHexStringIntoData((String) insertedData, pastedData, codeType);

                    long length = pastedData.getDataSize();
                    long toRemove = length;
                    if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
                        if (dataPosition + toRemove > codeArea.getDataSize()) {
                            toRemove = codeArea.getDataSize() - dataPosition;
                        }
                        ((EditableBinaryData) data).remove(dataPosition, toRemove);
                    }
                    if (editMode == EditMode.INPLACE && length > toRemove) {
                        ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData, 0, toRemove);
                        caret.setCaretPosition(caret.getDataPosition() + toRemove);
                        updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                    } else {
                        ((EditableBinaryData) data).insert(caret.getDataPosition(), pastedData);
                        caret.setCaretPosition(caret.getDataPosition() + length);
                        updateSelection(SelectingMode.NONE, caret.getCaretPosition());
                    }

                    caret.setCodeOffset(0);
                    ((CaretCapable) codeArea).setActiveCaretPosition(caret.getCaretPosition());
                    sequenceBreak();
                    codeArea.notifyDataChanged();
                    revealCursor();
                    clearSelection();
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

    @Nonnull
    public EnterKeyHandlingMode getEnterKeyHandlingMode() {
        return enterKeyHandlingMode;
    }

    public void setEnterKeyHandlingMode(EnterKeyHandlingMode enterKeyHandlingMode) {
        this.enterKeyHandlingMode = enterKeyHandlingMode;
    }

    @Nonnull
    public TabKeyHandlingMode getTabKeyHandlingMode() {
        return tabKeyHandlingMode;
    }

    public void setTabKeyHandlingMode(TabKeyHandlingMode tabKeyHandlingMode) {
        this.tabKeyHandlingMode = tabKeyHandlingMode;
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
        long dataPosition = ((CaretCapable) codeArea).getActiveCaretPosition().getDataPosition();
        ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
    }

    public void updateSelection(SelectingMode selectingMode, CodeAreaCaretPosition caretPosition) {
        long dataPosition = ((CaretCapable) codeArea).getDataPosition();
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selectingMode == SelectingMode.SELECTING) {
            ((SelectionCapable) codeArea).setSelection(selection.getStart(), dataPosition);
        } else {
            ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
        }
    }

    private void updateCanPaste() {
        canPaste = clipboard.hasPrimaryClip(); // CodeAreaUtils.canPaste(clipboard, binaryDataFlavor);
    }

    @Override
    public void moveCaret(int positionX, int positionY, SelectingMode selecting) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).mousePositionToClosestCaretPosition(positionX, positionY, CaretOverlapMode.PARTIAL_OVERLAP);
        ((CaretCapable) codeArea).setActiveCaretPosition(caretPosition);
        updateSelection(selecting, caretPosition);

        sequenceBreak();
        codeArea.repaint();
    }

    public void move(SelectingMode selectingMode, MovementDirection direction) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        CodeAreaCaretPosition movePosition = ((CaretCapable) codeArea).computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            ((CaretCapable) codeArea).setActiveCaretPosition(movePosition);
            updateSelection(selectingMode, movePosition);
        } else if (selectingMode == SelectingMode.NONE) {
            clearSelection();
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
    public void wheelScroll(int scrollSize, ScrollBarOrientation direction) {
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

    public boolean changeEditOperation() {
        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        if (editMode == EditMode.EXPANDING || editMode == EditMode.CAPPED) {
            EditOperation editOperation = ((EditModeCapable) codeArea).getEditOperation();
            switch (editOperation) {
                case INSERT: {
                    ((EditModeCapable) codeArea).setEditOperation(EditOperation.OVERWRITE);
                    break;
                }
                case OVERWRITE: {
                    ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
                    break;
                }
            }
            return true;
        }

        return false;
    }

    public boolean isValidChar(char value) {
        return ((CharsetCapable) codeArea).getCharset().canEncode();
    }

    @Nonnull
    public byte[] charToBytes(char value) {
        ByteBuffer buffer = ((CharsetCapable) codeArea).getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Nonnull
    protected CodeType getCodeType() {
        if (codeTypeSupported) {
            return ((CodeTypeCapable) codeArea).getCodeType();
        }

        return CodeType.HEXADECIMAL;
    }

    protected void revealCursor() {
        ((ScrollingCapable) codeArea).revealCursor();
        codeArea.repaint();
    }

    @Override
    public boolean checkEditAllowed() {
        return codeArea.isEditable();
    }

    @Nonnull
    protected static SelectingMode isSelectingMode(KeyEvent keyEvent) {
        return (keyEvent.getModifiers() & KeyEvent.META_SHIFT_MASK) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE;
    }
}
