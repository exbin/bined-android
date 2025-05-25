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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import org.exbin.auxiliary.binary_data.delta.DeltaDocument;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CodeAreaCaretListener;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditModeChangedListener;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.Font;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.editor.android.inspector.BasicValuesInspector;
import org.exbin.bined.editor.android.inspector.BasicValuesPositionColorModifier;
import org.exbin.bined.editor.android.options.DataInspectorMode;
import org.exbin.bined.editor.android.options.KeysPanelMode;
import org.exbin.bined.editor.android.options.Theme;
import org.exbin.bined.editor.android.preference.BinaryEditorPreferences;
import org.exbin.bined.editor.android.preference.EditorPreferences;
import org.exbin.bined.editor.android.preference.EncodingPreference;
import org.exbin.bined.editor.android.preference.FontPreference;
import org.exbin.bined.editor.android.preference.MainPreferences;
import org.exbin.bined.editor.android.search.BinarySearch;
import org.exbin.bined.editor.android.search.BinarySearchService;
import org.exbin.bined.editor.android.search.BinarySearchServiceImpl;
import org.exbin.bined.editor.android.search.SearchCondition;
import org.exbin.bined.editor.android.search.SearchParameters;
import org.exbin.bined.highlight.android.NonAsciiCodeAreaColorAssessor;
import org.exbin.bined.highlight.android.NonprintablesCodeAreaAssessor;
import org.exbin.bined.operation.android.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoRedoChangeListener;
import org.exbin.framework.bined.BinEdCodeAreaAssessor;
import org.exbin.framework.bined.BinaryStatusApi;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Main activity.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MainActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {

    private static final int DOUBLE_BACK_KEY_INTERVAL = 3000;
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
    private static final int GO_TO_SIDE_PANEL_POPUP_ID = 11;
    private static final int OPEN_MAIN_MENU_POPUP_ID = 12;

    private static final int STORAGE_PERMISSION_CODE = 1;

    private BinEdFileHandler fileHandler;
    private CodeArea codeArea;
    private BasicValuesInspector basicValuesInspector;
    private BinaryEditorPreferences appPreferences;

    private Toolbar toolbar;
    private View keyPanel;
    private View basicValuesInspectorView;
    private Menu menu;
    private SearchView searchView;
    private final BinaryStatusHandler binaryStatus = new BinaryStatusHandler(this);
    private BinarySearch binarySearch;
    private Runnable postSaveAsAction = null;
    private boolean keyboardShown = false;
    private boolean dataInspectorShown = true;
    private long lastBackKeyPressTime = -1;
    private long lastReleaseBackKeyPressTime = -1;
    BasicValuesPositionColorModifier basicValuesPositionColorModifier = new BasicValuesPositionColorModifier();
    private FallbackFileType fallbackFileType = FallbackFileType.FILE;

    private final BinarySearchService.SearchStatusListener searchStatusListener = new BinarySearchService.SearchStatusListener() {
        @Override
        public void setStatus(BinarySearchService.FoundMatches foundMatches, SearchParameters.MatchMode matchMode) {
            // TODO Add search status panel
        }

        @Override
        public void clearStatus() {

        }
    };
    private final BinaryDataUndoRedoChangeListener codeAreaChangeListener = () -> {
        if (menu != null) {
            updateUndoState();
        }
    };
    private final DataChangedListener codeAreaDataChangedListener = () -> {
//            activeFile.getComponent().notifyDataChanged();
//            if (editorModificationListener != null) {
//                editorModificationListener.modified();
//            }
        updateCurrentDocumentSize();
    };
    private final SelectionChangedListener codeAreaSelectionChangedListener = () -> {
        binaryStatus.setSelectionRange(codeArea.getSelection());
//            updateClipboardActionsStatus();
    };
    private final CodeAreaCaretListener codeAreaCodeAreaCaretListener = caretPosition -> {
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
    };
    private final View.OnKeyListener codeAreaOnKeyListener = new CodeAreaKeyListener();
    private Object codeAreaOnUnhandledKeyListener = null;
    private final EditModeChangedListener codeAreaEditModeChangedListener = binaryStatus::setEditMode;

    private final ActivityResultLauncher<Intent> openFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::openFileResultCallback);
    private final ActivityResultLauncher<Intent> openTableFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::openTableFileResultCallback);
    private final ActivityResultLauncher<Intent> saveFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::saveFileResultCallback);
    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::settingsResultCallback);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApplicationContext application = (ApplicationContext) getApplication();
        appPreferences = application.getAppPreferences();
        setContentView(R.layout.activity_main);

        MainPreferences mainPreferences = appPreferences.getMainPreferences();
        String theme = mainPreferences.getTheme();
        if (Theme.DARK.name().equalsIgnoreCase(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (Theme.LIGHT.name().equalsIgnoreCase(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        toolbar = findViewById(R.id.toolbar);
        keyPanel = findViewById(R.id.keyPanel);
        basicValuesInspectorView = findViewById(R.id.basic_values_inspector);
        setSupportActionBar(toolbar);

        // For now steal code area and keep it in application context
        fileHandler = application.getFileHandler();
        basicValuesInspector = new BasicValuesInspector();
        if (fileHandler == null) {
            codeArea = findViewById(R.id.codeArea);
            fileHandler = application.createFileHandler(codeArea);
        } else {
            codeArea = fileHandler.getCodeArea();
            ViewGroup parentView = (ViewGroup) codeArea.getParent();
            parentView.removeView(codeArea);
            ViewGroup contentView = findViewById(R.id.contentMain);
            contentView.removeView(findViewById(R.id.codeArea));
            contentView.addView(codeArea);
        }

        CompatUtils.setApplicationLocales(this, CompatUtils.getApplicationLocales(this));

        BasicCodeAreaColorsProfile basicColors = codeArea.getBasicColors().orElse(null);
        if (basicColors == null) {
            throw new IllegalStateException("Missing colors profile");
        }
        basicColors.setContext(this);
        basicColors.reinitialize();
        codeArea.resetColors();

        binarySearch = new BinarySearch();
        binarySearch.setBinarySearchService(new BinarySearchServiceImpl(codeArea));

        registerForContextMenu(codeArea);

        fileHandler.getUndoRedo().addChangeListener(codeAreaChangeListener);
        codeArea.addDataChangedListener(codeAreaDataChangedListener);
        codeArea.addSelectionChangedListener(codeAreaSelectionChangedListener);
        codeArea.addCaretMovedListener(codeAreaCodeAreaCaretListener);
        codeArea.addEditModeChangedListener(codeAreaEditModeChangedListener);
        codeArea.setOnKeyListener(codeAreaOnKeyListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            codeAreaOnUnhandledKeyListener = (View.OnUnhandledKeyEventListener) (view, event) -> codeAreaOnKeyListener.onKey(view, KeyEvent.KEYCODE_UNKNOWN, event);
            codeArea.addOnUnhandledKeyEventListener((View.OnUnhandledKeyEventListener) codeAreaOnUnhandledKeyListener);
        }
        basicValuesInspector.setCodeArea(codeArea, fileHandler.getUndoRedo(), basicValuesInspectorView);
        basicValuesInspector.enableUpdate();
        BinEdCodeAreaAssessor codeAreaAssessor = fileHandler.getCodeAreaAssessor();
        codeAreaAssessor.addColorModifier(basicValuesPositionColorModifier);
        basicValuesInspector.registerFocusPainter(basicValuesPositionColorModifier);

        basicValuesInspectorView.setNextFocusUpId(R.id.toolbar);

        applySettings();

        processIntent(getIntent());

        codeArea.post(() -> codeArea.requestFocus());
    }

    private void setupKeyPanel(KeysPanelMode keysPanelMode) {
        LinearLayout mainView = findViewById(R.id.main);
        int keyPanelIndex = mainView.indexOfChild(keyPanel);
        if (keysPanelMode == KeysPanelMode.HIDE) {
            if (keyPanelIndex >= 0) {
                mainView.removeViewAt(keyPanelIndex);
                mainView.requestLayout();
            }
            return;
        }

        if (keyPanelIndex == -1) {
            mainView.addView(keyPanel, 2);
            mainView.requestLayout();
        }

        switch (keysPanelMode) {
            case SMALL: {
                setupKeyPanelSize(60, 40);
                break;
            }
            case MEDIUM: {
                setupKeyPanelSize(90, 60);
                break;
            }
            case BIG: {
                setupKeyPanelSize(120, 80);
                break;
            }
        }
        keyPanel.requestLayout();
    }

    private void setupKeyPanelSize(int buttonWidth, int buttonHeight) {
        Button button0 = findViewById(R.id.button0);
        button0.setMinWidth(buttonWidth);
        button0.setMinHeight(buttonHeight);
        Button button1 = findViewById(R.id.button1);
        button1.setMinWidth(buttonWidth);
        button1.setMinHeight(buttonHeight);
        Button button2 = findViewById(R.id.button2);
        button2.setMinWidth(buttonWidth);
        button2.setMinHeight(buttonHeight);
        Button button3 = findViewById(R.id.button3);
        button3.setMinWidth(buttonWidth);
        button3.setMinHeight(buttonHeight);
        Button button4 = findViewById(R.id.button4);
        button4.setMinWidth(buttonWidth);
        button4.setMinHeight(buttonHeight);
        Button button5 = findViewById(R.id.button5);
        button5.setMinWidth(buttonWidth);
        button5.setMinHeight(buttonHeight);
        Button button6 = findViewById(R.id.button6);
        button6.setMinWidth(buttonWidth);
        button6.setMinHeight(buttonHeight);
        Button button7 = findViewById(R.id.button7);
        button7.setMinWidth(buttonWidth);
        button7.setMinHeight(buttonHeight);
        Button button8 = findViewById(R.id.button8);
        button8.setMinWidth(buttonWidth);
        button8.setMinHeight(buttonHeight);
        Button button9 = findViewById(R.id.button9);
        button9.setMinWidth(buttonWidth);
        button9.setMinHeight(buttonHeight);
        Button buttonA = findViewById(R.id.buttonA);
        buttonA.setMinWidth(buttonWidth);
        buttonA.setMinHeight(buttonHeight);
        Button buttonB = findViewById(R.id.buttonB);
        buttonB.setMinWidth(buttonWidth);
        buttonB.setMinHeight(buttonHeight);
        Button buttonC = findViewById(R.id.buttonC);
        buttonC.setMinWidth(buttonWidth);
        buttonC.setMinHeight(buttonHeight);
        Button buttonD = findViewById(R.id.buttonD);
        buttonD.setMinWidth(buttonWidth);
        buttonD.setMinHeight(buttonHeight);
        Button buttonE = findViewById(R.id.buttonE);
        buttonE.setMinWidth(buttonWidth);
        buttonE.setMinHeight(buttonHeight);
        Button buttonF = findViewById(R.id.buttonF);
        buttonF.setMinWidth(buttonWidth);
        buttonF.setMinHeight(buttonHeight);

        Button buttonHome = findViewById(R.id.buttonHome);
        buttonHome.setMinWidth(buttonWidth);
        buttonHome.setMinHeight(buttonHeight);
        Button buttonEnd = findViewById(R.id.buttonEnd);
        buttonEnd.setMinWidth(buttonWidth);
        buttonEnd.setMinHeight(buttonHeight);
        Button buttonLeft = findViewById(R.id.buttonLeft);
        buttonLeft.setMinWidth(buttonWidth);
        buttonLeft.setMinHeight(buttonHeight);
        Button buttonRight = findViewById(R.id.buttonRight);
        buttonRight.setMinWidth(buttonWidth);
        buttonRight.setMinHeight(buttonHeight);
        Button buttonUp = findViewById(R.id.buttonUp);
        buttonUp.setMinWidth(buttonWidth);
        buttonUp.setMinHeight(buttonHeight);
        Button buttonDown = findViewById(R.id.buttonDown);
        buttonDown.setMinWidth(buttonWidth);
        buttonDown.setMinHeight(buttonHeight);

        Button buttonDelete = findViewById(R.id.buttonDelete);
        buttonDelete.setMinWidth(buttonWidth);
        buttonDelete.setMinHeight(buttonHeight);
        Button buttonBk = findViewById(R.id.buttonBk);
        buttonBk.setMinWidth(buttonWidth);
        buttonBk.setMinHeight(buttonHeight);
        Button buttonInsert = findViewById(R.id.buttonInsert);
        buttonInsert.setMinWidth(buttonWidth);
        buttonInsert.setMinHeight(buttonHeight);
        Button buttonTab = findViewById(R.id.buttonTab);
        buttonTab.setMinWidth(buttonWidth);
        buttonTab.setMinHeight(buttonHeight);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            codeArea.removeOnUnhandledKeyEventListener((View.OnUnhandledKeyEventListener) codeAreaOnUnhandledKeyListener);
        }
        codeArea.setOnKeyListener(null);
        codeArea.removeEditModeChangedListener(codeAreaEditModeChangedListener);
        codeArea.removeCaretMovedListener(codeAreaCodeAreaCaretListener);
        codeArea.removeSelectionChangedListener(codeAreaSelectionChangedListener);
        codeArea.removeDataChangedListener(codeAreaDataChangedListener);
        fileHandler.getUndoRedo().removeChangeListener(codeAreaChangeListener);
        BinEdCodeAreaAssessor codeAreaAssessor = fileHandler.getCodeAreaAssessor();
        codeAreaAssessor.removeColorModifier(basicValuesPositionColorModifier);
    }

    private void processIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) {
            String scheme = intent.getScheme();
            if (ContentResolver.SCHEME_FILE.equals(scheme) || ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                Uri fileUri = intent.getData();
                if (fileUri != null) {
                    releaseFile(() -> {
                        try {
                            fileHandler.openFile(getContentResolver(), fileUri, appPreferences.getEditorPreferences().getFileHandlingMode());
                            // Content should be opened as unspecified file
                            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                                fileHandler.clearFileUri();
                            }
                            updateStatus();
                        } catch (Throwable tw) {
                            reportException(tw);
                        }
                    });
                }
            }
        }
    }

    @Nonnull
    private LocaleListCompat getLanguageLocaleList() {
        String language = appPreferences.getMainPreferences().getLocaleTag();

        if (language.isEmpty()) {
            return LocaleListCompat.getEmptyLocaleList();
        }

        return LocaleListCompat.forLanguageTags(language);
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
        binaryStatus.setEncoding(codeArea.getCharset().name());

        EditorPreferences editorPreferences = appPreferences.getEditorPreferences();
        setupKeyPanel(editorPreferences.getKeysPanelMode());
        DataInspectorMode dataInspectorMode = editorPreferences.getDataInspectorMode();
        boolean showDataInspector = dataInspectorMode == DataInspectorMode.SHOW || (dataInspectorMode == DataInspectorMode.LANDSCAPE && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (showDataInspector != dataInspectorShown) {
            LinearLayout mainHorizontalLayout = findViewById(R.id.mainHorizontalLayout);
            if (showDataInspector) {
                basicValuesInspector.enableUpdate();
                mainHorizontalLayout.addView(basicValuesInspectorView);
            } else {
                basicValuesInspector.disableUpdate();
                mainHorizontalLayout.removeView(basicValuesInspectorView);
            }
            mainHorizontalLayout.requestLayout();
            dataInspectorShown = showDataInspector;
        }

        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) > 0;
        basicValuesPositionColorModifier.setDarkMode(isDarkMode);

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

        // Currently on Google TV access to app bar icons doesn't seem to work
        if (isGoogleTV(this)) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }

        codeArea.addSelectionChangedListener(this::updateEditActionsState);
        updateUndoState();
        updateEditActionsState();
        updateViewActionsState();

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = Objects.requireNonNull((SearchView) searchMenuItem.getActionView());
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binarySearch.performFindAgain(searchStatusListener);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SearchCondition searchCondition = new SearchCondition();
                searchCondition.setSearchText(newText);
                SearchParameters searchParameters = new SearchParameters();
                searchParameters.setCondition(searchCondition);
                binarySearch.performFind(searchParameters, searchStatusListener);
                return true;
            }
        });

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Resources resources = getResources();
        int order = 0;
        if (isGoogleTV(codeArea.getContext())) {
            menu.add(0, GO_TO_SIDE_PANEL_POPUP_ID, order, resources.getString(R.string.action_go_to_side_panel));
            order++;
            menu.add(0, OPEN_MAIN_MENU_POPUP_ID, order, resources.getString(R.string.action_open_main_menu));
            order++;
        }
        menu.add(0, SELECTION_START_POPUP_ID, order, resources.getString(R.string.action_selection_start));
        menu.add(0, SELECTION_END_POPUP_ID, order + 1, resources.getString(R.string.action_selection_end));
        menu.add(0, CLEAR_SELECTION_POPUP_ID, order + 2, resources.getString(R.string.action_clear_selection));
        MenuItem cutMenuItem = menu.add(1, CUT_ACTION_POPUP_ID, order + 3, resources.getString(R.string.action_cut));
        cutMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
        MenuItem copyMenuItem = menu.add(1, COPY_ACTION_POPUP_ID, order + 4, resources.getString(R.string.action_copy));
        copyMenuItem.setEnabled(codeArea.hasSelection());
        menu.add(1, COPY_AS_CODE_ACTION_POPUP_ID, order + 5, resources.getString(R.string.action_copy_as_code));
        MenuItem pasteMenuItem = menu.add(1, PASTE_ACTION_POPUP_ID, order + 6, resources.getString(R.string.action_paste));
        pasteMenuItem.setEnabled(codeArea.isEditable() && codeArea.canPaste());
        menu.add(1, PASTE_FROM_CODE_ACTION_POPUP_ID, order + 7, resources.getString(R.string.action_paste_from_code));
        MenuItem deleteMenuItem = menu.add(1, DELETE_ACTION_POPUP_ID, order + 8, resources.getString(R.string.action_delete));
        deleteMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
        menu.add(1, SELECT_ALL_ACTION_POPUP_ID, order + 9, resources.getString(R.string.action_select_all));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case GO_TO_SIDE_PANEL_POPUP_ID: {
                LinearLayout mainView = findViewById(R.id.main);
                int keyPanelIndex = mainView.indexOfChild(keyPanel);
                if (keyPanelIndex >= 0) {
                    View downButton = findViewById(R.id.buttonDown);
                    downButton.requestFocus();
                } else {
                    View editTextByte = findViewById(R.id.editTextByte);
                    editTextByte.requestFocus();
                }
                break;
            }
            case OPEN_MAIN_MENU_POPUP_ID: {
                toolbar.showOverflowMenu();
                break;
            }
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
                try {
                    codeArea.copy();
                } catch (Throwable tw) {
                    reportException(tw);
                }
                break;
            }
            case PASTE_ACTION_POPUP_ID: {
                try {
                    codeArea.paste();
                } catch (Throwable tw) {
                    reportException(tw);
                }
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
                try {
                    ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).copyAsCode();
                } catch (Throwable tw) {
                    reportException(tw);
                }
                break;
            }
            case PASTE_FROM_CODE_ACTION_POPUP_ID: {
                try {
                    ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).pasteFromCode();
                } catch (Throwable tw) {
                    reportException(tw);
                }
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
                fileHandler.setNewData(appPreferences.getEditorPreferences().getFileHandlingMode());
            });

            return true;
        } else if (id == R.id.action_open) {
            releaseFile(this::openFile);

            return true;
        } else if (id == R.id.action_open_table_file) {
            openTableFile();

            return true;
        } else if (id == R.id.action_save) {
            Uri currentFileUri = fileHandler.getCurrentFileUri();
            if (currentFileUri == null) {
                saveAs();
            } else {
                try {
                    fileHandler.saveFile(getContentResolver(), currentFileUri);
                } catch (Throwable tw) {
                    reportException(tw);
                    return false;
                }
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
                finish();
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
                binaryStatus.setEncoding(codeArea.getCharset().name());
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
        } else if (id == R.id.non_printable_characters) {
            boolean checked = item.isChecked();
            item.setChecked(!checked);
            NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaAndroidUtils.findColorAssessor((ColorAssessorPainterCapable) codeArea.getPainter(), NonprintablesCodeAreaAssessor.class);
            if (nonprintablesCodeAreaAssessor != null) {
                nonprintablesCodeAreaAssessor.setShowNonprintables(!checked);
            }
            appPreferences.getCodeAreaPreferences().setShowNonprintables(!checked);
            return true;
        } else if (id == R.id.code_colorization) {
            boolean checked = item.isChecked();
            item.setChecked(!checked);
            NonAsciiCodeAreaColorAssessor nonAsciiColorAssessor = CodeAreaAndroidUtils.findColorAssessor((ColorAssessorPainterCapable) codeArea.getPainter(), NonAsciiCodeAreaColorAssessor.class);
            if (nonAsciiColorAssessor != null) {
                nonAsciiColorAssessor.setNonAsciiHighlightingEnabled(!checked);
            }
            appPreferences.getCodeAreaPreferences().setCodeColorization(!checked);
            return true;
        } else if (id == R.id.action_undo) {
            try {
                fileHandler.getUndoRedo().performUndo();
            } catch (Throwable tw) {
                reportException(tw);
            }
            // TODO fix operations instead of validating
            codeArea.validateCaret();
            return true;
        } else if (id == R.id.action_redo) {
            try {
                fileHandler.getUndoRedo().performRedo();
            } catch (Throwable tw) {
                reportException(tw);
            }
            codeArea.validateCaret();
            return true;
        } else if (id == R.id.action_cut) {
            codeArea.cut();
            return true;
        } else if (id == R.id.action_copy) {
            try {
                codeArea.copy();
            } catch (Throwable tw) {
                reportException(tw);
            }
            return true;
        } else if (id == R.id.action_paste) {
            try {
                codeArea.paste();
            } catch (Throwable tw) {
                reportException(tw);
            }
            return true;
        } else if (id == R.id.action_search) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.action_search);
            searchView.setIconifiedByDefault(false);
            builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                binarySearch.cancelSearch();
                binarySearch.clearSearch();
            });
            builder.setView(searchView);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            searchView.requestFocus();

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
                long targetPosition = Long.parseLong(inputNumber.getText().toString());
                caretPosition.setDataPosition(Math.min(targetPosition, codeArea.getDataSize()));
                codeArea.setActiveCaretPosition(caretPosition);
                codeArea.validateCaret();
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
        if (!fileHandler.isModified()) {
            postReleaseAction.run();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file_modified);
        builder.setPositiveButton(R.string.button_save, (dialog, which) -> {
            Uri currentFileUri = fileHandler.getCurrentFileUri();
            if (currentFileUri == null) {
                saveAs();
            } else {
                try {
                    fileHandler.saveFile(getContentResolver(), currentFileUri);
                } catch (Throwable tw) {
                    reportException(tw);
                }
            }
            postReleaseAction.run();
        });
        builder.setNeutralButton(R.string.button_discard, (dialog, which) -> {
            postReleaseAction.run();
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (System.currentTimeMillis() - lastReleaseBackKeyPressTime < DOUBLE_BACK_KEY_INTERVAL) {
                    postReleaseAction.run();
                } else {
                    lastReleaseBackKeyPressTime = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.confirm_discard), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void openFile() {
        if (MainActivity.isGoogleTV(this) || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fallBackOpenFile(FallbackFileType.FILE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra(Intent.EXTRA_LOCALE_LIST, (android.os.Parcelable) getLanguageLocaleList().unwrap());
        }

        Uri pickerInitialUri = fileHandler.getPickerInitialUri();
        if (pickerInitialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        }

        try {
            openFileLauncher.launch(Intent.createChooser(intent, getResources().getString(R.string.select_file)));
        } catch (ActivityNotFoundException ex) {
            fallBackOpenFile(FallbackFileType.FILE);
        }
    }

    public void openTableFile() {
        if (MainActivity.isGoogleTV(this) || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fallBackOpenFile(FallbackFileType.TABLE_FILE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra(Intent.EXTRA_LOCALE_LIST, (android.os.Parcelable) getLanguageLocaleList().unwrap());
        }

        try {
            openTableFileLauncher.launch(Intent.createChooser(intent, getResources().getString(R.string.select_file)));
        } catch (ActivityNotFoundException ex) {
            fallBackOpenFile(FallbackFileType.TABLE_FILE);
        }
    }

    private void fallBackOpenFile(FallbackFileType fallbackFileType) {
        this.fallbackFileType = fallbackFileType;
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            requestWriteExternalStoragePermission();
            return;
        }

        OpenFileDialog dialog = new OpenFileDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
        dialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());
    }

    public void saveAs() {
        saveAs(null);
    }

    public void saveAs(@Nullable Runnable postSaveAsAction) {
        this.postSaveAsAction = postSaveAsAction;

        if (MainActivity.isGoogleTV(this) || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fallBackSaveAs();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra(Intent.EXTRA_LOCALE_LIST, (android.os.Parcelable) getLanguageLocaleList().unwrap());
        }

        Uri pickerInitialUri = fileHandler.getPickerInitialUri();
        if (pickerInitialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        }

        try {
            saveFileLauncher.launch(Intent.createChooser(intent, getResources().getString(R.string.save_as_file)));
        } catch (ActivityNotFoundException ex) {
            fallBackSaveAs();
        } catch (Throwable tw) {
            reportException(tw);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            Toast.makeText(this, R.string.storage_permission_is_not_granted, Toast.LENGTH_LONG).show();
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
        binaryStatus.setCurrentDocumentSize(dataSize, fileHandler.getDocumentOriginalSize());
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
        if (codeArea.getEditMode() == EditMode.READ_ONLY) {
            newMemoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } else if (codeArea.getContentData() instanceof DeltaDocument) {
            newMemoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        }

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
        menu.findItem(R.id.non_printable_characters).setChecked(appPreferences.getCodeAreaPreferences().isShowNonprintables());
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
        Uri currentFileUri = fileHandler.getCurrentFileUri();
        saveMenuItem.setEnabled(currentFileUri == null || fileHandler.getUndoRedo().isModified());

        boolean canUndo = fileHandler.getUndoRedo().canUndo();
        MenuItem undoMenuItem = menu.findItem(R.id.action_undo);
        undoMenuItem.setEnabled(canUndo);

        boolean canRedo = fileHandler.getUndoRedo().canRedo();
        MenuItem redoMenuItem = menu.findItem(R.id.action_redo);
        redoMenuItem.setEnabled(canRedo);
    }

    private void openFileResultCallback(ActivityResult activityResult) {
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (resultCode != MainActivity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        try {
            fileHandler.openFile(getContentResolver(), data.getData(), appPreferences.getEditorPreferences().getFileHandlingMode());
            updateStatus();
        } catch (Throwable tw) {
            reportException(tw);
        }
    }

    private void openTableFileResultCallback(ActivityResult activityResult) {
        CodeAreaTableMapAssessor codeAreaTableMapAssessor = fileHandler.getCodeAreaTableMapAssessor();
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (resultCode != MainActivity.RESULT_OK || data == null || data.getData() == null) {
            codeAreaTableMapAssessor.setUseTable(false);
            return;
        }

        try {
            codeAreaTableMapAssessor.openFile(getContentResolver(), data.getData());
            fileHandler.getCodeArea().repaint();
        } catch (Throwable tw) {
            reportException(tw);
        }
    }

    private void saveFileResultCallback(ActivityResult activityResult) {
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (resultCode != MainActivity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        try {
            fileHandler.saveFile(getContentResolver(), data.getData());
            if (postSaveAsAction != null) {
                postSaveAsAction.run();
                postSaveAsAction = null;
            }
        } catch (Throwable tw) {
            reportException(tw);
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
            switch (fallbackFileType) {
                case FILE:
                    try {
                        fileHandler.openFile(getContentResolver(), Uri.fromFile(file), appPreferences.getEditorPreferences().getFileHandlingMode());
                        updateStatus();
                    } catch (Throwable tw) {
                        reportException(tw);
                    }
                    break;
                case TABLE_FILE:
                    try {
                        fileHandler.getCodeAreaTableMapAssessor().openFile(getContentResolver(), Uri.fromFile(file));
                        fileHandler.getCodeArea().repaint();
                    } catch (Throwable tw) {
                        reportException(tw);
                    }
                    break;
            }
        } else {
            try {
                fileHandler.saveFile(getContentResolver(), Uri.fromFile(file));
                if (postSaveAsAction != null) {
                    postSaveAsAction.run();
                    postSaveAsAction = null;
                }
            } catch (Throwable tw) {
                reportException(tw);
            }
        }
    }

    private void settingsResultCallback(ActivityResult activityResult) {
        applySettings();
        if (menu != null) {
            updateViewActionsState();
        }
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
        codeArea.getCommandHandler().keyTyped('9', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_9));
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

    private void requestWriteExternalStoragePermission() {
        final String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.storage_permission_request);
            builder.setPositiveButton(R.string.button_request, (dialog, which) -> {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, STORAGE_PERMISSION_CODE);
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            builder.show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, STORAGE_PERMISSION_CODE);
        }
    }

    @Nonnull
    public static LocaleListCompat getLanguageLocaleList(String language) {
        if (language.isEmpty()) {
            return LocaleListCompat.getEmptyLocaleList();
        }

        return LocaleListCompat.forLanguageTags(language);
    }

    @ParametersAreNonnullByDefault
    private class CodeAreaKeyListener implements View.OnKeyListener {

        private final KeyListener keyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
        private final Editable editable = Editable.Factory.getInstance().newEditable("");

        public CodeAreaKeyListener() {
            editable.clear();
        }

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (!codeArea.isFocused()) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && isGoogleTV(codeArea.getContext())) {
                        codeArea.post(codeArea::requestFocus);
                        return true;
                    }
                }
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    return currentFocus.dispatchKeyEvent(keyEvent);
                }

                return false;
            }

            try {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL || keyEvent.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
                        editable.clear();
                        codeArea.getCommandHandler().keyPressed(keyEvent);
                    } else {
                        keyListener.onKeyDown(view, editable, keyCode, keyEvent);
                        processKeys(keyEvent);
                    }
                } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    editable.clear();
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (keyEvent.getEventTime() - keyEvent.getDownTime() > TimeUnit.SECONDS.toMillis(1)) {
                            toolbar.showOverflowMenu();
                        } else {
                            codeArea.showContextMenu();
                        }
                    } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        if (fileHandler.isModified()) {
                            releaseFile(MainActivity.this::finish);
                        } else {
                            if (System.currentTimeMillis() - lastBackKeyPressTime < DOUBLE_BACK_KEY_INTERVAL) {
                                finish();
                            } else {
                                lastBackKeyPressTime = System.currentTimeMillis();
                                Toast.makeText(MainActivity.this, getResources().getText(R.string.confirm_exit), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_DEL && keyEvent.getKeyCode() != KeyEvent.KEYCODE_FORWARD_DEL) {
                        // TODO Do this on key up?
                        codeArea.getCommandHandler().keyPressed(keyEvent);
                    }
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
                    codeArea.getCommandHandler().keyTyped(editable.charAt(i), keyEvent);
                }
                editable.clear();
            }
        }
    }

    private void reportException(Throwable exception) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_exception);
        builder.setMessage(exception.getLocalizedMessage());
        builder.setNegativeButton(R.string.button_close, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public enum FallbackFileType {
        FILE, TABLE_FILE
    }
}
