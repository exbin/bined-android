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
package org.exbin.bined.editor.android;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.DialogFragment;

import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.Font;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.editor.android.preference.BinaryEditorPreferences;
import org.exbin.bined.editor.android.preference.EncodingPreference;
import org.exbin.bined.editor.android.preference.FontPreference;
import org.exbin.bined.editor.android.preference.MainPreferences;
import org.exbin.bined.editor.android.preference.PreferencesWrapper;
import org.exbin.bined.editor.android.search.BinarySearchService;
import org.exbin.bined.editor.android.search.BinarySearchServiceImpl;
import org.exbin.bined.editor.android.search.SearchCondition;
import org.exbin.bined.editor.android.search.SearchParameters;
import org.exbin.bined.highlight.android.HighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.operation.android.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.android.CodeAreaUndoRedo;
import org.exbin.framework.bined.BinaryStatusApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MainActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {

    private static final int SELECTION_START_POPUP_ID = 1;
    private static final int SELECTION_END_POPUP_ID = 2;
    private static final int CLEAR_SELECTION_POPUP_ID = 3;
    private static final int CUT_ACTION_POPUP_ID = 4;
    private static final int COPY_ACTION_POPUP_ID = 5;
    private static final int PASTE_ACTION_POPUP_ID = 6;
    private static final int DELETE_ACTION_POPUP_ID = 7;
    private static final int SELECT_ALL_ACTION_POPUP_ID = 8;
    private static final int COPY_AS_CODE_ACTION_POPUP_ID = 9;
    private static final int PASTE_FROM_CODE_ACTION_POPUP_ID = 10;

    private static final int STORAGE_PERMISSION_CODE = 1;

    private static final String CURSOR_POSITION_TAG = "bined.cursorPosition";
    private static final String CURSOR_OFFSET_TAG = "bined.cursorOffset";
    private static final String CURSOR_SECTION_TAG = "bined.cursorSection";
    private static final String SCROLL_ROW_POSITION_TAG = "bined.scrollRowPosition";
    private static final String SCROLL_ROW_OFFSET_TAG = "bined.scrollRowOffset";
    private static final String SCROLL_CHAR_POSITION_TAG = "bined.scrollCharPosition";
    private static final String SCROLL_CHAR_OFFSET_TAG = "bined.scrollOffsetPosition";
    private static final String SELECTION_START_TAG = "bined.selectionStart";
    private static final String SELECTION_END_TAG = "bined.selectionEnd";

    private CodeArea codeArea;
    private CodeAreaUndoRedo undoRedo;

    private long documentOriginalSize = 0;
    private Menu menu;
    private static ByteArrayEditableData fileData = null;
    private Uri currentFileUri = null;
    private Uri pickerInitialUri = null;
    private final BinaryStatusHandler binaryStatus = new BinaryStatusHandler(this);
    private BinarySearchService searchService;
    private Runnable postSaveAsAction = null;
    private final BinarySearchService.SearchStatusListener searchStatusListener = new BinarySearchService.SearchStatusListener() {
        @Override
        public void setStatus(BinarySearchService.FoundMatches foundMatches, SearchParameters.MatchMode matchMode) {
            // TODO Add search status panel
        }

        @Override
        public void clearStatus() {

        }
    };

    private boolean keyboardShown = false;
    private BinaryEditorPreferences appPreferences;

    private final ActivityResultLauncher<Intent> openFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::openFileResultCallback);
    private final ActivityResultLauncher<Intent> saveFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::saveFileResultCallback);
    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::settingsResultCallback);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferences = new BinaryEditorPreferences(new PreferencesWrapper(getApplicationContext()));
        setContentView(R.layout.activity_main);

        MainPreferences mainPreferences = appPreferences.getMainPreferences();
        String theme = mainPreferences.getTheme();
        if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("light".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.wrap(getLanguageLocaleList()));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        codeArea = findViewById(R.id.codeArea);
        codeArea.setEditOperation(EditOperation.INSERT);
        searchService = new BinarySearchServiceImpl(codeArea);

        undoRedo = new CodeAreaUndoRedo(codeArea);
        undoRedo.addChangeListener(() -> {
            if (menu != null) {
                updateUndoState();
            }
        });

        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea.getContext(), codeArea, undoRedo);
        codeArea.setCommandHandler(commandHandler);
        HighlightNonAsciiCodeAreaPainter painter = new HighlightNonAsciiCodeAreaPainter(codeArea);
        codeArea.setPainter(painter);
        BasicCodeAreaColorsProfile basicColors = painter.getBasicColors();
        basicColors.setContext(this);
        basicColors.reinitialize();
        painter.resetColors();

        registerForContextMenu(codeArea);

        if (fileData != null) {
            codeArea.setContentData(fileData);
        } else {
            codeArea.setContentData(new ByteArrayEditableData());
        }

        codeArea.addDataChangedListener(() -> {
//            activeFile.getComponent().notifyDataChanged();
//            if (editorModificationListener != null) {
//                editorModificationListener.modified();
//            }
            updateCurrentDocumentSize();
        });

        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
//            updateClipboardActionsStatus();
        });

        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);

            boolean showKeyboard = codeArea.getActiveSection() == BasicCodeAreaSection.TEXT_PREVIEW;
            if (showKeyboard != keyboardShown) {

                keyboardShown = showKeyboard;
                InputMethodManager im = (InputMethodManager) codeArea.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                codeArea.requestFocus();
                if (showKeyboard) {
                    im.showSoftInput(codeArea, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    im.hideSoftInputFromWindow(codeArea.getWindowToken(), 0);
                }
            }
        });

        codeArea.setOnKeyListener(new View.OnKeyListener() {

            private final KeyListener keyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
            private final Editable editable = Editable.Factory.getInstance().newEditable("");

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                try {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL || keyEvent.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
                            commandHandler.keyPressed(keyEvent);
                        } else {
                            keyListener.onKeyDown(view, editable, keyCode, keyEvent);
                            processKeys(keyEvent);
                        }
                    } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        commandHandler.keyPressed(keyEvent);
                    } else {
                        keyListener.onKeyOther(view, editable, keyEvent);
                        processKeys(keyEvent);
                    }
                    return true;
                } catch (Exception ex) {
                    // ignore
                }
                return false;
            }

            private void processKeys(KeyEvent keyEvent) {
                int outputCharsLength = editable.length();
                if (outputCharsLength > 0) {
                    for (int i = 0; i < outputCharsLength; i++) {
                        commandHandler.keyTyped(editable.charAt(i), keyEvent);
                    }
                    editable.clear();
                }
            }
        });

        codeArea.addEditModeChangedListener(binaryStatus::setEditMode);

        applySettings();

        processIntent(getIntent());
    }

    private void processIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) {
            String scheme = intent.getScheme();
            if ("file".equals(scheme) || "content".equals(scheme)) {
                Uri fileUri = intent.getData();
                if (fileUri != null) {
                    releaseFile(() -> {
                        openFile(fileUri);
                        // Content should be opened as unspecified file
                        if ("content".equals(scheme)) {
                            currentFileUri = null;
                            pickerInitialUri = null;
                        }
                    });
                }
            }
        }
    }

    @Nonnull
    private LocaleList getLanguageLocaleList() {
        String language = appPreferences.getMainPreferences().getLocaleTag();
        if (language.isEmpty()) {
            return LocaleList.getEmptyLocaleList();
        } else if ("en".equals(language)) {
            return LocaleList.getDefault();
        }

        return LocaleList.forLanguageTags(language);
    }

    private void applySettings() {
        appPreferences.getCodeAreaPreferences().applyPreferences(codeArea);
        try {
            codeArea.setCharset(Charset.forName(appPreferences.getEncodingPreferences().getDefaultEncoding()));
        } catch (Exception ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // TODO: Fix code font
            Font codeFont = new Font(); // codeArea.getCodeFont();
            int fontSize = appPreferences.getFontPreferences().getFontSize();
            codeFont.setSize(fontSize);
            codeArea.setCodeFont(codeFont);
        } catch (Exception ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        binaryStatus.setEncoding(codeArea.getCharset().toString());

        updateStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // If possible, attempt to show icons in the main menu via reflection
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        this.menu = menu;
        updateUndoState();

        codeArea.addSelectionChangedListener(this::updateEditActionsState);
        updateEditActionsState();

        updateViewActionsState();

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = Objects.requireNonNull((SearchView) searchMenuItem.getActionView());
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchService.performFindAgain(searchStatusListener);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SearchCondition searchCondition = new SearchCondition();
                searchCondition.setSearchText(newText);
                SearchParameters searchParameters = new SearchParameters();
                searchParameters.setCondition(searchCondition);
                searchService.performFind(searchParameters, searchStatusListener);
                return true;
            }
        });

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, SELECTION_START_POPUP_ID, 0, getResources().getString(R.string.action_selection_start));
        menu.add(0, SELECTION_END_POPUP_ID, 1, getResources().getString(R.string.action_selection_end));
        menu.add(0, CLEAR_SELECTION_POPUP_ID, 2, getResources().getString(R.string.action_clear_selection));
        MenuItem cutMenuItem = menu.add(1, CUT_ACTION_POPUP_ID, 3, getResources().getString(R.string.action_cut));
        cutMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
        MenuItem copyMenuItem = menu.add(1, COPY_ACTION_POPUP_ID, 4, getResources().getString(R.string.action_copy));
        copyMenuItem.setEnabled(codeArea.hasSelection());
        menu.add(1, COPY_AS_CODE_ACTION_POPUP_ID, 5, getResources().getString(R.string.action_copy_as_code));
        MenuItem pasteMenuItem = menu.add(1, PASTE_ACTION_POPUP_ID, 6, getResources().getString(R.string.action_paste));
        pasteMenuItem.setEnabled(codeArea.isEditable() && codeArea.canPaste());
        menu.add(1, PASTE_FROM_CODE_ACTION_POPUP_ID, 5, getResources().getString(R.string.action_paste_from_code));
        MenuItem deleteMenuItem = menu.add(1, DELETE_ACTION_POPUP_ID, 7, getResources().getString(R.string.action_delete));
        deleteMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
        menu.add(1, SELECT_ALL_ACTION_POPUP_ID, 8, getResources().getString(R.string.action_select_all));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SELECTION_START_POPUP_ID: {
                SelectionRange selection = codeArea.getSelection();
                CodeAreaCaretPosition touchCaretPosition = codeArea.mousePositionToClosestCaretPosition((int) codeArea.getTouchPositionX(), (int) codeArea.getTouchPositionY(), CaretOverlapMode.PARTIAL_OVERLAP);
                if (selection.isEmpty()) {
                    codeArea.setSelection(touchCaretPosition.getDataPosition(), codeArea.getDataPosition());
                } else {
                    codeArea.setSelection(touchCaretPosition.getDataPosition(), selection.getEnd());
                }
                break;
            }
            case SELECTION_END_POPUP_ID: {
                SelectionRange selection = codeArea.getSelection();
                CodeAreaCaretPosition touchCaretPosition = codeArea.mousePositionToClosestCaretPosition((int) codeArea.getTouchPositionX(), (int) codeArea.getTouchPositionY(), CaretOverlapMode.PARTIAL_OVERLAP);
                if (selection.isEmpty()) {
                    codeArea.setSelection(codeArea.getDataPosition(), touchCaretPosition.getDataPosition());
                } else {
                    codeArea.setSelection(selection.getStart(), touchCaretPosition.getDataPosition());
                }
                break;
            }
            case CLEAR_SELECTION_POPUP_ID: {
                codeArea.clearSelection();
                break;
            }
            case CUT_ACTION_POPUP_ID: {
                codeArea.cut();
                break;
            }
            case COPY_ACTION_POPUP_ID: {
                codeArea.copy();
                break;
            }
            case PASTE_ACTION_POPUP_ID: {
                codeArea.paste();
                break;
            }
            case DELETE_ACTION_POPUP_ID: {
                codeArea.delete();
                break;
            }
            case SELECT_ALL_ACTION_POPUP_ID: {
                codeArea.selectAll();
                break;
            }
            case COPY_AS_CODE_ACTION_POPUP_ID: {
                codeArea.copyAsCode();
                break;
            }
            case PASTE_FROM_CODE_ACTION_POPUP_ID: {
                codeArea.pasteFromCode();
                break;
            }

            default: {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_new) {
            releaseFile(() -> {
                codeArea.setContentData(new ByteArrayEditableData());
                undoRedo.clear();
                currentFileUri = null;

                documentOriginalSize = 0;
            });

            return true;
        } else if (id == R.id.action_open) {
            releaseFile(this::openFile);

            return true;
        } else if (id == R.id.action_save) {
            if (currentFileUri == null) {
                saveAs();
            } else {
                saveFile(currentFileUri);
            }

            return true;
        } else if (id == R.id.action_save_as) {
            saveAs();

            return true;
        } else if (id == R.id.action_settings) {
            // User chose the "Settings" item, show the app settings UI...
            Intent intent = new Intent(this, SettingsActivity.class);
            settingsLauncher.launch(intent);

            return true;
        } else if (id == R.id.action_about) {
            PackageManager manager = this.getPackageManager();
            String appVersion = "";
            try {
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
                appVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // ignore
            }

            AboutDialog aboutDialog = new AboutDialog();
            aboutDialog.setAppVersion(appVersion);
            aboutDialog.show(getSupportFragmentManager(), "aboutDialog");

            return true;
        } else if (id == R.id.action_exit) {
            releaseFile(() -> {
                System.exit(0);
            });

            return true;
        } else if (id == R.id.code_type) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.code_type);
            builder.setSingleChoiceItems(getResources().getTextArray(R.array.code_type_entries), codeArea.getCodeType().ordinal(), (dialog, which) -> {
                CodeType codeType = CodeType.values()[which];
                codeArea.setCodeType(codeType);
                appPreferences.getCodeAreaPreferences().setCodeType(codeType);
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        } else if (id == R.id.view_mode) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.view_mode);
            builder.setSingleChoiceItems(getResources().getTextArray(R.array.view_mode_entries), codeArea.getViewMode().ordinal(), (dialog, which) -> {
                CodeAreaViewMode viewMode = CodeAreaViewMode.values()[which];
                codeArea.setViewMode(viewMode);
                appPreferences.getCodeAreaPreferences().setViewMode(viewMode);
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        } else if (id == R.id.hex_chars_case) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.hex_characters_case);
            builder.setSingleChoiceItems(getResources().getTextArray(R.array.hex_chars_case_entries), codeArea.getCodeCharactersCase().ordinal(), (dialog, which) -> {
                CodeCharactersCase codeCharactersCase = CodeCharactersCase.values()[which];
                codeArea.setCodeCharactersCase(codeCharactersCase);
                appPreferences.getCodeAreaPreferences().setCodeCharactersCase(codeCharactersCase);
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        } else if (id == R.id.encoding) {
            EncodingPreference.showEncodingSelectionDialog(this, codeArea.getCharset().name(), encoding -> {
                codeArea.setCharset(Charset.forName(encoding));
                appPreferences.getEncodingPreferences().setDefaultEncoding(encoding);
                binaryStatus.setEncoding(codeArea.getCharset().toString());
            });
            return true;
        } else if (id == R.id.font) {
            FontPreference.showFontSelectionDialog(this, codeArea.getCodeFont(), codeFont -> {
                codeArea.setCodeFont(codeFont);
                appPreferences.getFontPreferences().setFontSize(codeFont.getSize());
            });
            return true;
        } else if (id == R.id.bytes_per_row_fill) {
            codeArea.setRowWrapping(RowWrappingMode.WRAPPING);
            codeArea.setMaxBytesPerRow(0);
            appPreferences.getCodeAreaPreferences().setRowWrappingMode(codeArea.getRowWrapping());
            appPreferences.getCodeAreaPreferences().setMaxBytesPerRow(codeArea.getMaxBytesPerRow());
            menu.findItem(R.id.bytes_per_row_fill).setChecked(true);
            return true;
        } else if (id == R.id.bytes_per_row_4) {
            codeArea.setRowWrapping(RowWrappingMode.NO_WRAPPING);
            codeArea.setMaxBytesPerRow(4);
            appPreferences.getCodeAreaPreferences().setRowWrappingMode(codeArea.getRowWrapping());
            appPreferences.getCodeAreaPreferences().setMaxBytesPerRow(codeArea.getMaxBytesPerRow());
            menu.findItem(R.id.bytes_per_row_4).setChecked(true);
            return true;
        } else if (id == R.id.bytes_per_row_8) {
            codeArea.setRowWrapping(RowWrappingMode.NO_WRAPPING);
            codeArea.setMaxBytesPerRow(8);
            appPreferences.getCodeAreaPreferences().setRowWrappingMode(codeArea.getRowWrapping());
            appPreferences.getCodeAreaPreferences().setMaxBytesPerRow(codeArea.getMaxBytesPerRow());
            menu.findItem(R.id.bytes_per_row_8).setChecked(true);
            return true;
        } else if (id == R.id.bytes_per_row_12) {
            codeArea.setRowWrapping(RowWrappingMode.NO_WRAPPING);
            codeArea.setMaxBytesPerRow(12);
            appPreferences.getCodeAreaPreferences().setRowWrappingMode(codeArea.getRowWrapping());
            appPreferences.getCodeAreaPreferences().setMaxBytesPerRow(codeArea.getMaxBytesPerRow());
            menu.findItem(R.id.bytes_per_row_12).setChecked(true);
            return true;
        } else if (id == R.id.bytes_per_row_16) {
            codeArea.setRowWrapping(RowWrappingMode.NO_WRAPPING);
            codeArea.setMaxBytesPerRow(16);
            appPreferences.getCodeAreaPreferences().setRowWrappingMode(codeArea.getRowWrapping());
            appPreferences.getCodeAreaPreferences().setMaxBytesPerRow(codeArea.getMaxBytesPerRow());
            menu.findItem(R.id.bytes_per_row_16).setChecked(true);
            return true;
        } else if (id == R.id.code_colorization) {
            boolean checked = item.isChecked();
            item.setChecked(!checked);
            ((HighlightNonAsciiCodeAreaPainter) codeArea.getPainter()).setNonAsciiHighlightingEnabled(!checked);
            appPreferences.getCodeAreaPreferences().setCodeColorization(!checked);
            return true;
        } else if (id == R.id.action_undo) {
            undoRedo.performUndo();
            return true;
        } else if (id == R.id.action_redo) {
            undoRedo.performRedo();
            return true;
        } else if (id == R.id.action_cut) {
            codeArea.cut();
            return true;
        } else if (id == R.id.action_copy) {
            codeArea.copy();
            return true;
        } else if (id == R.id.action_paste) {
            codeArea.paste();
            return true;
        } else if (id == R.id.action_delete) {
            codeArea.delete();
            return true;
        } else if (id == R.id.action_select_all) {
            codeArea.selectAll();
            return true;
        } else if (id == R.id.go_to_position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.go_to_position);
            final EditText inputNumber = new EditText(this);
            inputNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputNumber.setText(String.valueOf(codeArea.getDataPosition()));
            builder.setView(inputNumber);
            builder.setPositiveButton(R.string.button_go_to, (dialog, which) -> {
                DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();
                caretPosition.setCodeOffset(0);
                caretPosition.setPosition(codeArea.getActiveCaretPosition());
                caretPosition.setDataPosition(Long.parseLong(inputNumber.getText().toString()));
                codeArea.setActiveCaretPosition(caretPosition);
                codeArea.centerOnCursor();
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void releaseFile(Runnable postReleaseAction) {
        if (!undoRedo.isModified()) {
            postReleaseAction.run();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file_modified);
        builder.setPositiveButton(R.string.button_save, (dialog, which) -> {
            if (currentFileUri == null) {
                saveAs();
            } else {
                saveFile(currentFileUri);
            }
            postReleaseAction.run();
        });
        builder.setNeutralButton(R.string.button_discard, (dialog, which) -> {
            postReleaseAction.run();
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void openFile() {
        if (MainActivity.isGoogleTV(this)) {
            fallBackOpenFile();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra(Intent.EXTRA_LOCALE_LIST, getLanguageLocaleList());
        }

        if (pickerInitialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        }

        try {
            openFileLauncher.launch(Intent.createChooser(intent, getResources().getString(R.string.select_file)));
        } catch (ActivityNotFoundException ex) {
            fallBackOpenFile();
        }
    }

    private void fallBackOpenFile() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            OpenFileDialog dialog = new OpenFileDialog();
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
            dialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void saveAs() {
        saveAs(null);
    }

    public void saveAs(@Nullable Runnable postSaveAsAction) {
        this.postSaveAsAction = postSaveAsAction;

        if (MainActivity.isGoogleTV(this)) {
            fallBackSaveAs();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra(Intent.EXTRA_LOCALE_LIST, getLanguageLocaleList());
        }

        if (pickerInitialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        }

        try {
            saveFileLauncher.launch(Intent.createChooser(intent, getResources().getString(R.string.save_as_file)));
        } catch (ActivityNotFoundException ex) {
            fallBackSaveAs();
        }
    }

    private void fallBackSaveAs() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            SaveFileDialog dialog = new SaveFileDialog();
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
            dialog.show(getSupportFragmentManager(), SaveFileDialog.class.getName());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void openFile(Uri fileUri) {
        fileData = new ByteArrayEditableData();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return;
            }
            fileData.loadFromStream(inputStream);
            inputStream.close();
            documentOriginalSize = fileData.getDataSize();
            undoRedo.clear();
            codeArea.setContentData(fileData);
            currentFileUri = fileUri;
            pickerInitialUri = fileUri;
        } catch (IOException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveFile(Uri fileUri) {
        BinaryData contentData = codeArea.getContentData();
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(fileUri);
            if (outputStream == null) {
                return;
            }
            fileData.saveToStream(outputStream);
            outputStream.close();
            documentOriginalSize = contentData.getDataSize();
            undoRedo.setSyncPosition();
            currentFileUri = fileUri;
            pickerInitialUri = fileUri;
        } catch (IOException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long cursorDataPosition = savedInstanceState.getLong(CURSOR_POSITION_TAG);
        int cursorOffset = savedInstanceState.getInt(CURSOR_OFFSET_TAG);
        int cursorSection = savedInstanceState.getInt(CURSOR_SECTION_TAG);
        codeArea.setActiveCaretPosition(new DefaultCodeAreaCaretPosition(cursorDataPosition, cursorOffset, cursorSection == 0 ? BasicCodeAreaSection.CODE_MATRIX : BasicCodeAreaSection.TEXT_PREVIEW));

        long scrollRowPosition = savedInstanceState.getLong(SCROLL_ROW_POSITION_TAG);
        int scrollRowOffset = savedInstanceState.getInt(SCROLL_ROW_OFFSET_TAG);
        int scrollCharPosition = savedInstanceState.getInt(SCROLL_CHAR_POSITION_TAG);
        int scrollCharOffset = savedInstanceState.getInt(SCROLL_CHAR_OFFSET_TAG);
        codeArea.setScrollPosition(new CodeAreaScrollPosition(scrollRowPosition, scrollRowOffset, scrollCharPosition, scrollCharOffset));

        long selectionStart = savedInstanceState.getLong(SELECTION_START_TAG);
        long selectionEnd = savedInstanceState.getLong(SELECTION_END_TAG);
        codeArea.setSelection(new SelectionRange(selectionStart, selectionEnd));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SelectionRange selection = codeArea.getSelection();
        outState.putLong(SELECTION_START_TAG, selection.getStart());
        outState.putLong(SELECTION_END_TAG, selection.getEnd());

        CodeAreaScrollPosition scrollPosition = codeArea.getScrollPosition();
        outState.putLong(SCROLL_ROW_POSITION_TAG, scrollPosition.getRowPosition());
        outState.putInt(SCROLL_ROW_OFFSET_TAG, scrollPosition.getRowOffset());
        outState.putInt(SCROLL_CHAR_POSITION_TAG, scrollPosition.getCharPosition());
        outState.putInt(SCROLL_CHAR_OFFSET_TAG, scrollPosition.getCharOffset());

        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        outState.putLong(CURSOR_POSITION_TAG, caretPosition.getDataPosition());
        outState.putInt(CURSOR_OFFSET_TAG, caretPosition.getCodeOffset());
        outState.putInt(CURSOR_SECTION_TAG, ((BasicCodeAreaSection) caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX)).ordinal());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            Toast.makeText(this, "Storage permission is not granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    public void updateStatus() {
        updateCurrentDocumentSize();
        updateCurrentCaretPosition();
        updateCurrentSelectionRange();
        updateCurrentMemoryMode();
        updateCurrentEditMode();
    }

    private void updateCurrentDocumentSize() {
        if (binaryStatus == null) {
            return;
        }

        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    private void updateCurrentCaretPosition() {
        if (binaryStatus == null) {
            return;
        }

        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        binaryStatus.setCursorPosition(caretPosition);
    }

    private void updateCurrentSelectionRange() {
        if (binaryStatus == null) {
            return;
        }

        SelectionRange selectionRange = codeArea.getSelection();
        binaryStatus.setSelectionRange(selectionRange);
    }

    private void updateCurrentMemoryMode() {
        if (binaryStatus == null) {
            return;
        }

        BinaryStatusApi.MemoryMode newMemoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
        if (((EditModeCapable) codeArea).getEditMode() == EditMode.READ_ONLY) {
            newMemoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } /* else if (codeArea.getContentData() instanceof DeltaDocument) {
            newMemoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        } */

        binaryStatus.setMemoryMode(newMemoryMode);
    }

    private void updateCurrentEditMode() {
        if (binaryStatus == null) {
            return;
        }

        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());
    }

    private void updateEditActionsState() {
        MenuItem cutMenuItem = menu.findItem(R.id.action_cut);
        cutMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());

        MenuItem copyMenuItem = menu.findItem(R.id.action_copy);
        copyMenuItem.setEnabled(codeArea.hasSelection());
        MenuItem pasteMenuItem = menu.findItem(R.id.action_paste);
        pasteMenuItem.setEnabled(codeArea.isEditable() && codeArea.canPaste());
        MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
        deleteMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
    }

    private void updateViewActionsState() {
        menu.findItem(R.id.code_colorization).setChecked(appPreferences.getCodeAreaPreferences().isCodeColorization());
        int bytesPerRow = appPreferences.getCodeAreaPreferences().getMaxBytesPerRow();
        switch (bytesPerRow) {
            case 0: {
                menu.findItem(R.id.bytes_per_row_fill).setChecked(true);
                break;
            }
            case 4: {
                menu.findItem(R.id.bytes_per_row_4).setChecked(true);
                break;
            }
            case 8: {
                menu.findItem(R.id.bytes_per_row_8).setChecked(true);
                break;
            }
            case 12: {
                menu.findItem(R.id.bytes_per_row_12).setChecked(true);
                break;
            }
            case 16: {
                menu.findItem(R.id.bytes_per_row_16).setChecked(true);
                break;
            }
        }
    }

    private void updateUndoState() {
        MenuItem saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setEnabled(currentFileUri == null || undoRedo.isModified());

        boolean canUndo = undoRedo.canUndo();
        MenuItem undoMenuItem = menu.findItem(R.id.action_undo);
        undoMenuItem.setEnabled(canUndo);
        // TODO undoMenuItem.setIconTintList(new ColorStateList(ColorStateList.));

        boolean canRedo = undoRedo.canRedo();
        MenuItem redoMenuItem = menu.findItem(R.id.action_redo);
        redoMenuItem.setEnabled(canRedo);
    }

    private void openFileResultCallback(ActivityResult activityResult) {
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (resultCode != MainActivity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        openFile(data.getData());
    }

    private void saveFileResultCallback(ActivityResult activityResult) {
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (resultCode != MainActivity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        saveFile(data.getData());
        if (postSaveAsAction != null) {
            postSaveAsAction.run();
            postSaveAsAction = null;
        }
    }

    /**
     * Legacy support for file dialog using external library.
     *
     * @param dialog file dialog
     * @param file   selected file
     */
    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        if (dialog instanceof OpenFileDialog) {
            openFile(Uri.fromFile(file));
        } else {
            saveFile(Uri.fromFile(file));
            if (postSaveAsAction != null) {
                postSaveAsAction.run();
                postSaveAsAction = null;
            }
        }
    }

    private void settingsResultCallback(ActivityResult activityResult) {
        applySettings();
        updateViewActionsState();
    }

    public void buttonAction0(View view) {
        codeArea.getCommandHandler().keyTyped('0', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0));
    }

    public void buttonAction1(View view) {
        codeArea.getCommandHandler().keyTyped('1', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1));
    }

    public void buttonAction2(View view) {
        codeArea.getCommandHandler().keyTyped('2', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_2));
    }

    public void buttonAction3(View view) {
        codeArea.getCommandHandler().keyTyped('3', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_3));
    }

    public void buttonAction4(View view) {
        codeArea.getCommandHandler().keyTyped('4', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_4));
    }

    public void buttonAction5(View view) {
        codeArea.getCommandHandler().keyTyped('5', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_5));
    }

    public void buttonAction6(View view) {
        codeArea.getCommandHandler().keyTyped('6', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_6));
    }

    public void buttonAction7(View view) {
        codeArea.getCommandHandler().keyTyped('7', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_7));
    }

    public void buttonAction8(View view) {
        codeArea.getCommandHandler().keyTyped('8', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0));
    }

    public void buttonAction9(View view) {
        codeArea.getCommandHandler().keyTyped('1', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1));
    }

    public void buttonActionA(View view) {
        codeArea.getCommandHandler().keyTyped('a', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A));
    }

    public void buttonActionB(View view) {
        codeArea.getCommandHandler().keyTyped('b', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B));
    }

    public void buttonActionC(View view) {
        codeArea.getCommandHandler().keyTyped('c', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_C));
    }

    public void buttonActionD(View view) {
        codeArea.getCommandHandler().keyTyped('d', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_D));
    }

    public void buttonActionE(View view) {
        codeArea.getCommandHandler().keyTyped('e', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_E));
    }

    public void buttonActionF(View view) {
        codeArea.getCommandHandler().keyTyped('f', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F));
    }

    public void buttonActionUp(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
    }

    public void buttonActionDown(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
    }

    public void buttonActionLeft(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
    }

    public void buttonActionRight(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
    }

    public void buttonActionHome(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
    }

    public void buttonActionEnd(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
    }

    public void buttonActionInsert(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_INSERT));
    }

    public void buttonActionDelete(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL));
    }

    public void buttonActionBk(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    public void buttonActionTab(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB));
    }

    public static boolean isGoogleTV(Context context) {
        final PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
    }
}
