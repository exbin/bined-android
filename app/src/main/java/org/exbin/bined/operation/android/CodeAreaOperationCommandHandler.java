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
package org.exbin.bined.operation.android;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.KeyEvent;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.CodeAreaCommandHandler;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
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
import org.exbin.bined.operation.android.command.CodeAreaCommand;
import org.exbin.bined.operation.android.command.CodeAreaCommandType;
import org.exbin.bined.operation.android.command.CodeAreaCompoundCommand;
import org.exbin.bined.operation.android.command.EditCharDataCommand;
import org.exbin.bined.operation.android.command.EditCodeDataCommand;
import org.exbin.bined.operation.android.command.EditDataCommand;
import org.exbin.bined.operation.android.command.InsertDataCommand;
import org.exbin.bined.operation.android.command.ModifyDataCommand;
import org.exbin.bined.operation.android.command.RemoveDataCommand;
import org.exbin.bined.operation.undo.BinaryDataAppendableUndoRedo;
import org.exbin.bined.operation.undo.BinaryDataUndoRedo;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Command handler for undo/redo aware binary editor editing.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaOperationCommandHandler implements CodeAreaCommandHandler {

    public static final String BINED_CLIPBOARD_MIME = "application/x-bined";
    public static final String MIME_CHARSET = "charset";
    protected static final int CODE_BUFFER_LENGTH = 16;
    protected static final char BACKSPACE_CHAR = '\b';
    protected static final char DELETE_CHAR = (char) 0x7f;

    private final int metaMask = CodeAreaAndroidUtils.getMetaMaskDown();

    @Nonnull
    protected final CodeAreaCore codeArea;
    @Nonnull
    private EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    @Nonnull
    private TabKeyHandlingMode tabKeyHandlingMode = TabKeyHandlingMode.PLATFORM_SPECIFIC;
    private final boolean codeTypeSupported;
    private final boolean viewModeSupported;

    private Context context;
    protected ClipboardManager clipboard;
    private boolean canPaste = false;
    private ClipDescription binedDataFlavor;
    private ClipDescription binaryDataFlavor;
    private ClipData currentClipboardData = null;

    protected final BinaryDataUndoRedo undoRedo;
    protected EditDataCommand editCommand = null;

    public CodeAreaOperationCommandHandler(Context context, CodeAreaCore codeArea, BinaryDataUndoRedo undoRedo) {
        this.context = context;
        this.codeArea = codeArea;
        this.undoRedo = undoRedo;

        codeTypeSupported = codeArea instanceof CodeTypeCapable;
        viewModeSupported = codeArea instanceof ViewModeCapable;

        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                updateCanPaste();
            }
        });

        binedDataFlavor = new ClipDescription("BinEd Data", new String[] { BINED_CLIPBOARD_MIME });
        binaryDataFlavor = new ClipDescription("Binary Data", new String[] { CodeAreaUtils.MIME_CLIPBOARD_BINARY });

/*        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.addFlavorListener((FlavorEvent e) -> {
                updateCanPaste();
            });
            binedDataFlavor = new DataFlavor(BinaryData.class, DefaultCodeAreaCommandHandler.BINED_CLIPBOARD_MIME_FULL);
            try {
                binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            updateCanPaste();
        } catch (IllegalStateException ex) {
            canPaste = false;
        } catch (java.awt.HeadlessException ex) {
            Logger.getLogger(CodeAreaOperationCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        } */
    }

    @Nonnull
    public static CodeAreaCommandHandler.CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory(final Context context) {
        return (CodeAreaCore codeAreaCore) -> new CodeAreaOperationCommandHandler(context, codeAreaCore, new CodeAreaUndoRedo(codeAreaCore));
    }

    @Nonnull
    public BinaryDataUndoRedo getUndoRedo() {
        return undoRedo;
    }

    private void updateCanPaste() {
        canPaste = clipboard.hasPrimaryClip();  // canPaste = CodeAreaSwingUtils.canPaste(clipboard, binedDataFlavor) || CodeAreaSwingUtils.canPaste(clipboard, DataFlavor.stringFlavor);
    }

    @Override
    public void undoSequenceBreak() {
        editCommand = null;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (!codeArea.isEnabled()) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                move(isSelectingMode(keyEvent), MovementDirection.LEFT);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                move(isSelectingMode(keyEvent), MovementDirection.RIGHT);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                move(isSelectingMode(keyEvent), MovementDirection.UP);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                move(isSelectingMode(keyEvent), MovementDirection.DOWN);
                undoSequenceBreak();
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
                undoSequenceBreak();
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
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_UP);
                undoSequenceBreak();
                revealCursor();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_DOWN);
                undoSequenceBreak();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_INSERT: {
                changeEditOperation();
//                keyEvent.consume();
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
                if (enterKeyHandlingMode != EnterKeyHandlingMode.IGNORE) {
//                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.KEYCODE_FORWARD_DEL: {
                deletePressed();
//                keyEvent.consume();
                break;
            }
            case KeyEvent.KEYCODE_DEL: {
                backSpacePressed();
//                keyEvent.consume();
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

        CodeAreaSection section = ((CaretCapable) codeArea).getActiveSection();
        if (section != BasicCodeAreaSection.TEXT_PREVIEW) {
//            int modifiersEx = keyEvent.getModifiers();
//            if (modifiersEx == 0 || modifiersEx == KeyEvent.SHIFT_DOWN_MASK) {
                pressedCharAsCode(keyValue);
//            }
        } else {
            if (keyValue > DefaultCodeAreaCommandHandler.LAST_CONTROL_CODE && keyValue != DELETE_CHAR) {
                pressedCharInPreview(keyValue);
            }
        }
    }

    private void pressedCharAsCode(char keyChar) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        int startCodeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        boolean validKey = CodeAreaUtils.isValidCodeKeyValue(keyChar, startCodeOffset, codeType);
        if (validKey) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
            DeleteSelectionCommand deleteSelectionCommand = null;
            if (codeArea.hasSelection()) {
                long selectionStart = ((SelectionCapable) codeArea).getSelection().getFirst();
                deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
                ((CaretCapable) codeArea).setActiveCaretPosition(selectionStart);
                undoSequenceBreak();
            }

            int value;
            if (keyChar >= '0' && keyChar <= '9') {
                value = keyChar - '0';
            } else {
                value = Character.toLowerCase(keyChar) - 'a' + 10;
            }

//                if (codeArea.getEditAllowed() == EditAllowed.OVERWRITE_ONLY && codeArea.getEditMode() == EditMode.OVERWRITE && dataPosition == dataSize) {
//                    return;
//                }
//            if (editCommand != null && editCommand.wasReverted()) {
//                editCommand = null;
//            }

            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                if (deleteSelectionCommand != null) {
                    CodeAreaCompoundCommand compoundCommand = new CodeAreaCompoundCommand(codeArea);
                    compoundCommand.addCommand(deleteSelectionCommand);
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    int codeOffset = ((CaretCapable) codeArea).getCodeOffset();
                    editCommand = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition, codeOffset, (byte) value);
                    compoundCommand.addCommand(editCommand);
                    undoRedo.execute(compoundCommand);
                } else {
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    int codeOffset = ((CaretCapable) codeArea).getCodeOffset();
                    EditCodeDataCommand command = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition, codeOffset, (byte) value);
                    if (editCommand != null && isAppendAllowed() && undoRedo instanceof BinaryDataAppendableUndoRedo) {
                        if (!((BinaryDataAppendableUndoRedo) undoRedo).appendExecute(command)) {
                            editCommand = command;
                        }
                    } else {
                        editCommand = command;
                        undoRedo.execute(editCommand);
                    }
                }
            } else {
                if (deleteSelectionCommand != null) {
                    CodeAreaCompoundCommand compoundCommand = new CodeAreaCompoundCommand(codeArea);
                    compoundCommand.addCommand(deleteSelectionCommand);
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    int codeOffset = ((CaretCapable) codeArea).getCodeOffset();
                    editCommand = new EditCodeDataCommand(codeArea, EditCharDataCommand.EditCommandType.INSERT, dataPosition, codeOffset, (byte) value);
                    compoundCommand.addCommand(editCommand);
                    undoRedo.execute(compoundCommand);
                } else {
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    int codeOffset = ((CaretCapable) codeArea).getCodeOffset();
                    EditCodeDataCommand command = new EditCodeDataCommand(codeArea, EditCharDataCommand.EditCommandType.INSERT, dataPosition, codeOffset, (byte) value);
                    if (editCommand != null && isAppendAllowed() && undoRedo instanceof BinaryDataAppendableUndoRedo) {
                        if (!((BinaryDataAppendableUndoRedo) undoRedo).appendExecute(command)) {
                            editCommand = command;
                        }
                    } else {
                        editCommand = command;
                        undoRedo.execute(editCommand);
                    }
                }
            }
            codeArea.notifyDataChanged();
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char keyChar) {
        boolean validKey = isValidChar(keyChar);
        if (validKey) {
            EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
            EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
//            if (editCommand != null && editCommand.wasReverted()) {
//                editCommand = null;
//            }
            DeleteSelectionCommand deleteCommand = null;
            if (codeArea.hasSelection()) {
                undoSequenceBreak();
                deleteCommand = new DeleteSelectionCommand(codeArea);
            }

            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                if (deleteCommand != null) {
                    CodeAreaCompoundCommand compoundCommand = new CodeAreaCompoundCommand(codeArea);
                    compoundCommand.addCommand(deleteCommand);
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    editCommand = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition, keyChar);
                    compoundCommand.addCommand(editCommand);
                    undoRedo.execute(compoundCommand);
                } else {
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    EditCharDataCommand command = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.OVERWRITE, dataPosition, keyChar);
                    if (editCommand != null && isAppendAllowed() && undoRedo instanceof BinaryDataAppendableUndoRedo) {
                        if (!((BinaryDataAppendableUndoRedo) undoRedo).appendExecute(command)) {
                            editCommand = command;
                        }
                    } else {
                        editCommand = command;
                        undoRedo.execute(editCommand);
                    }
                }
            } else {
                if (deleteCommand != null) {
                    CodeAreaCompoundCommand compoundCommand = new CodeAreaCompoundCommand(codeArea);
                    compoundCommand.addCommand(deleteCommand);
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    editCommand = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.INSERT, dataPosition, keyChar);
                    compoundCommand.addCommand(editCommand);
                    undoRedo.execute(compoundCommand);
                } else {
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    EditCharDataCommand command = new EditCharDataCommand(codeArea, EditCodeDataCommand.EditCommandType.INSERT, dataPosition, keyChar);
                    if (editCommand != null && isAppendAllowed() && undoRedo instanceof BinaryDataAppendableUndoRedo) {
                        if (!((BinaryDataAppendableUndoRedo) undoRedo).appendExecute(command)) {
                            editCommand = command;
                        }
                    } else {
                        editCommand = command;
                        undoRedo.execute(editCommand);
                    }
                }
            }

            codeArea.notifyDataChanged();
            revealCursor();
        }
    }

    @Override
    public void enterPressed() {
        if (!checkEditAllowed()) {
            return;
        }

        CodeAreaSection section = ((CaretCapable) codeArea).getActiveSection();
        if (section == BasicCodeAreaSection.TEXT_PREVIEW) {
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
        if (!checkEditAllowed()) {
            return;
        }

        if (tabKeyHandlingMode == TabKeyHandlingMode.PLATFORM_SPECIFIC || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_NEXT_SECTION || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_PREVIOUS_SECTION) {
            if (viewModeSupported && ((ViewModeCapable) codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                move(selectingMode, MovementDirection.SWITCH_SECTION);
                undoSequenceBreak();
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

        deleteAction(BACKSPACE_CHAR);
    }

    @Override
    public void deletePressed() {
        if (!checkEditAllowed()) {
            return;
        }

        deleteAction(DELETE_CHAR);
    }

    private void deleteAction(char keyChar) {
        if (codeArea.hasSelection()) {
            DeleteSelectionCommand deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
            undoRedo.execute(deleteSelectionCommand);
            undoSequenceBreak();
            codeArea.notifyDataChanged();
        } else {
//            if (editCommand != null && editCommand.wasReverted()) {
//                editCommand = null;
//            }

            CodeAreaSection section = ((CaretCapable) codeArea).getActiveSection();
            long dataPosition = ((CaretCapable) codeArea).getDataPosition();
            if (section == BasicCodeAreaSection.CODE_MATRIX) {
                EditCodeDataCommand command = new EditCodeDataCommand(codeArea, EditCodeDataCommand.EditCommandType.DELETE, dataPosition, 0, (byte) keyChar);
                if (editCommand != null && isAppendAllowed() && undoRedo instanceof BinaryDataAppendableUndoRedo) {
                    if (!((BinaryDataAppendableUndoRedo) undoRedo).appendExecute(command)) {
                        editCommand = command;
                    }
                } else {
                    editCommand = command;
                    undoRedo.execute(editCommand);
                }
            } else {
                EditCharDataCommand command = new EditCharDataCommand(codeArea, EditCharDataCommand.EditCommandType.DELETE, dataPosition, keyChar);
                if (editCommand != null && isAppendAllowed() && undoRedo instanceof BinaryDataAppendableUndoRedo) {
                    if (!((BinaryDataAppendableUndoRedo) undoRedo).appendExecute(command)) {
                        editCommand = command;
                    }
                } else {
                    editCommand = command;
                    undoRedo.execute(editCommand);
                }
            }
            codeArea.notifyDataChanged();
        }
    }

    @Override
    public void delete() {
        if (!checkEditAllowed()) {
            return;
        }

        undoRedo.execute(new DeleteSelectionCommand(codeArea));
        undoSequenceBreak();
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

            Charset charset = codeArea instanceof CharsetCapable ? ((CharsetCapable) codeArea).getCharset() : null;
            ClipData clipData = CodeAreaAndroidUtils.createBinaryDataClipboardData(context, copy, binedDataFlavor, binaryDataFlavor, charset);
            setClipboardContent(clipData);
        }
    }

    @Override
    public void copyAsCode() {
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (!selection.isEmpty()) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = codeArea.getContentData().copy(first, last - first + 1);

            CodeType codeType = ((CodeTypeCapable) codeArea).getCodeType();
            CodeCharactersCase charactersCase = ((CodeCharactersCaseCapable) codeArea).getCodeCharactersCase();
            ClipData clipData = CodeAreaAndroidUtils.createCodeDataClipboardData(context, copy, binedDataFlavor, codeType, charactersCase);
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
                undoRedo.execute(new DeleteSelectionCommand(codeArea));
                undoSequenceBreak();
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

        ClipData primaryClip = clipboard.getPrimaryClip();
        if (primaryClip == null || primaryClip.getItemCount() == 0) {
            return;
        }

        try {
            ClipDescription description = primaryClip.getDescription();
            try {
                if (!description.hasMimeType(BINED_CLIPBOARD_MIME) && !description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY) && !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return;
                }
            } catch (IllegalStateException ex) {
                return;
            }

            ClipData.Item clipItem = primaryClip.getItemAt(0);
            if (description.hasMimeType(BINED_CLIPBOARD_MIME)) {
//                Object clipboardData = clipboard.getData(binedDataFlavor);
//                if (clipboardData instanceof BinaryData) {
//                    pasteBinaryData((BinaryData) clipboardData);
//                }
            } else if (description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY)) {
//                ByteArrayEditableData pastedData = new ByteArrayEditableData();
//                InputStream clipboardData = (InputStream) clipboard.getData(binaryDataFlavor);
//                pastedData.insert(0, clipboardData, -1);
//                pasteBinaryData(pastedData);
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                CharSequence clipboardData = clipItem.getText();
                if (clipboardData != null) {
                    byte[] bytes = clipboardData.toString().getBytes(Charset.forName(CodeAreaAndroidUtils.DEFAULT_ENCODING));
                    BinaryData pastedData = new ByteArrayData(bytes);
                    pasteBinaryData(pastedData);
                }
            }
        } catch (IllegalStateException ex) {
            Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
            // Clipboard not available - ignore
        }
    }

    private void pasteBinaryData(BinaryData pastedData) {
        DeleteSelectionCommand deleteSelectionCommand = null;
        if (codeArea.hasSelection()) {
            deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
            deleteSelectionCommand.execute();
            undoSequenceBreak();
        }

        EditMode editMode = ((EditModeCapable) codeArea).getEditMode();
        EditOperation editOperation = ((EditModeCapable) codeArea).getActiveOperation();
        long dataSize = codeArea.getDataSize();
        long dataPosition = ((CaretCapable) codeArea).getDataPosition();

        CodeAreaCommand modifyCommand = null;
        long clipDataSize = pastedData.getDataSize();
        long insertionPosition = dataPosition;
        if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
            BinaryData modifiedData;
            long replacedPartSize = clipDataSize;
            if (insertionPosition + replacedPartSize > dataSize) {
                replacedPartSize = dataSize - insertionPosition;
                modifiedData = pastedData.copy(0, replacedPartSize);
            } else {
                modifiedData = pastedData.copy();
            }
            if (replacedPartSize > 0) {
                modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
                if (clipDataSize > replacedPartSize) {
                    pastedData = pastedData.copy(replacedPartSize, clipDataSize - replacedPartSize);
                    insertionPosition += replacedPartSize;
                } else {
                    pastedData = new ByteArrayData();
                }
            }
        }

        CodeAreaCommand insertCommand = null;
        if (!pastedData.isEmpty()) {
            insertCommand = new InsertDataCommand(codeArea, insertionPosition, ((CaretCapable) codeArea).getCodeOffset(), pastedData.copy());
        }

        CodeAreaCommand pasteCommand = CodeAreaCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
        if (pasteCommand == null) {
            return;
        }

        undoRedo.execute(pasteCommand);

        undoSequenceBreak();
        codeArea.notifyDataChanged();
        revealCursor();
        clearSelection();
    }

    @Override
    public void pasteFromCode() {
        if (!checkEditAllowed()) {
            return;
        }

        if (!clipboard.hasPrimaryClip()) {
            return;
        }

        ClipData primaryClip = clipboard.getPrimaryClip();
        if (primaryClip == null || primaryClip.getItemCount() == 0) {
            return;
        }

        try {
            ClipDescription description = primaryClip.getDescription();
            try {
                if (!description.hasMimeType(BINED_CLIPBOARD_MIME) && !description.hasMimeType(CodeAreaUtils.MIME_CLIPBOARD_BINARY) && !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return;
                }
            } catch (IllegalStateException ex) {
                return;
            }

            ClipData.Item clipItem = primaryClip.getItemAt(0);
            if (description.hasMimeType(BINED_CLIPBOARD_MIME)) {
                paste();
            } else if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                DeleteSelectionCommand deleteSelectionCommand = null;
                if (codeArea.hasSelection()) {
                    deleteSelectionCommand = new DeleteSelectionCommand(codeArea);
                    deleteSelectionCommand.execute();
                }

                long dataSize = codeArea.getDataSize();
                CharSequence clipboardData = clipItem.getText();
                if (clipboardData != null) {
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();

                    CodeAreaCommand modifyCommand = null;
                    CodeType codeType = ((CodeTypeCapable) codeArea).getCodeType();

                    String insertedString = clipboardData.toString();
                    ByteArrayEditableData clipData = new ByteArrayEditableData();
                    CodeAreaUtils.insertHexStringIntoData(insertedString, clipData, codeType);

                    ByteArrayEditableData pastedData = new ByteArrayEditableData();
                    pastedData.insert(0, clipData);
                    long pastedDataSize = pastedData.getDataSize();
                    long insertionPosition = dataPosition;
                    BinaryData modifiedData = pastedData;
                    long replacedPartSize = clipData.getDataSize();
                    if (insertionPosition + replacedPartSize > dataSize) {
                        replacedPartSize = dataSize - insertionPosition;
                        modifiedData = pastedData.copy(0, replacedPartSize);
                    }
                    if (replacedPartSize > 0) {
                        modifyCommand = new ModifyDataCommand(codeArea, dataPosition, modifiedData);
                        if (pastedDataSize > replacedPartSize) {
                            pastedData = (ByteArrayEditableData) pastedData.copy(replacedPartSize, pastedDataSize - replacedPartSize);
                            insertionPosition += replacedPartSize;
                        } else {
                            pastedData.clear();
                        }
                    }

                    CodeAreaCommand insertCommand = null;
                    if (pastedData.getDataSize() > 0) {
                        insertCommand = new InsertDataCommand(codeArea, insertionPosition, ((CaretCapable) codeArea).getCodeOffset(), pastedData);
                    }

                    CodeAreaCommand pasteCommand = CodeAreaCompoundCommand.buildCompoundCommand(codeArea, deleteSelectionCommand, modifyCommand, insertCommand);
                    if (pasteCommand == null) {
                        return;
                    }

                    if (modifyCommand != null) {
                        modifyCommand.execute();
                    }
                    if (insertCommand != null) {
                        insertCommand.execute();
                    }
                    undoRedo.execute(pasteCommand);

                    undoSequenceBreak();
                    codeArea.notifyDataChanged();
                    revealCursor();
                    clearSelection();
                }
            }
        } catch (IllegalStateException ex) {
            Logger.getLogger(CodeAreaCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
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
            ((SelectionCapable) codeArea).setSelection(0, dataSize);
        }
    }

    @Override
    public void clearSelection() {
        long dataPosition = ((CaretCapable) codeArea).getDataPosition();
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

    @Override
    public void moveCaret(int positionX, int positionY, SelectingMode selecting) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).mousePositionToClosestCaretPosition(positionX, positionY, CaretOverlapMode.PARTIAL_OVERLAP);
        ((CaretCapable) codeArea).setActiveCaretPosition(caretPosition);
        updateSelection(selecting, caretPosition);

        undoSequenceBreak();
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
    public void wheelScroll(int scrollSize, ScrollbarOrientation orientation) {
        if (scrollSize < 0) {
            for (int i = 0; i < -scrollSize; i++) {
                scroll(ScrollingDirection.UP);
            }
        } else if (scrollSize > 0) {
            for (int i = 0; i < scrollSize; i++) {
                scroll(ScrollingDirection.DOWN);
            }
        }
    }

    private boolean isAppendAllowed() {
        return undoRedo.getCommandPosition() != undoRedo.getSyncPosition();
    }

    public void changeEditOperation() {
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
        }
    }

    @ParametersAreNonnullByDefault
    private static class DeleteSelectionCommand extends CodeAreaCommand {

        private final RemoveDataCommand removeCommand;
        private final long position;
        private final long size;

        public DeleteSelectionCommand(CodeAreaCore coreArea) {
            super(coreArea);
            SelectionRange selection = ((SelectionCapable) coreArea).getSelection();
            position = selection.getFirst();
            size = selection.getLast() - position + 1;
            removeCommand = new RemoveDataCommand(coreArea, position, 0, size);
        }

        @Override
        public void execute() {
            removeCommand.redo();
            ((CaretCapable) codeArea).setActiveCaretPosition(position);
            clearSelection();
            ((ScrollingCapable) codeArea).revealCursor();
            codeArea.notifyDataChanged();
        }

        @Override
        public void undo() {
            removeCommand.undo();
            clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(position + size);
            ((ScrollingCapable) codeArea).revealCursor();
            codeArea.notifyDataChanged();
        }

        @Nonnull
        @Override
        public CodeAreaCommandType getType() {
            return CodeAreaCommandType.DATA_REMOVED;
        }

        private void clearSelection() {
            long dataPosition = ((CaretCapable) codeArea).getDataPosition();
            ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);
        }
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

    public boolean isValidChar(char value) {
        return ((CharsetCapable) codeArea).getCharset().canEncode();
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
        return codeArea.isEditable();
    }

    @Nonnull
    private static SelectingMode isSelectingMode(KeyEvent keyEvent) {
        return (keyEvent.getModifiers() & KeyEvent.META_SHIFT_MASK) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE;
    }
}
