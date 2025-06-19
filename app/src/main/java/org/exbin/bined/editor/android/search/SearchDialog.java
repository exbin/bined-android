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
package org.exbin.bined.editor.android.search;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.tabs.TabLayout;

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.jna.JnaBufferEditableData;
import org.exbin.bined.CodeAreaCaretListener;
import org.exbin.bined.EditOperation;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.editor.android.MainActivity;
import org.exbin.bined.editor.android.R;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Search text or data dialog.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SearchDialog extends AppCompatDialogFragment {

    private int lastTab = 0;
    private EditText editText;
    private CodeArea codeArea;
    private CodeArea templateCodeArea = null;
    private boolean keyboardShown = false;

    private View searchView;
    private SearchParameters searchParameters = null;

    private BinarySearch binarySearch;
    private BinarySearchService.SearchStatusListener searchStatusListener;
    private final CodeAreaCaretListener codeAreaCodeAreaCaretListener = caretPosition -> {
        boolean showKeyboard = true;
        if (showKeyboard != keyboardShown) {

            keyboardShown = showKeyboard;
            InputMethodManager im = (InputMethodManager) codeArea.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            codeArea.requestFocus();
            if (showKeyboard) {
                // TODO im.setInputMethodAndSubtype();
                im.showSoftInput(codeArea, InputMethodManager.SHOW_IMPLICIT);
                Dialog dialog = SearchDialog.this.getDialog();
                if (dialog != null) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            } else {
                im.hideSoftInputFromWindow(codeArea.getWindowToken(), 0);
            }
        }
    };

    @Nonnull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) requireActivity();
        binarySearch = activity.getBinarySearch();
        templateCodeArea = activity.getCodeArea();
        searchParameters = activity.getSearchParameters();
        searchStatusListener = activity.getSearchStatusListener();

        editText = new EditText(activity);
        codeArea = new CodeArea(activity, null);
        codeArea.setContentData(new JnaBufferEditableData());
        codeArea.setEditOperation(EditOperation.INSERT);
        codeArea.addCaretMovedListener(codeAreaCodeAreaCaretListener);
        codeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        codeArea.setOnKeyListener(new CodeAreaKeyListener());
        codeArea.setOnFocusChangeListener((view, hasFocus) -> {
            if (view == codeArea) {
                if (hasFocus) {
                    codeAreaCodeAreaCaretListener.caretMoved(codeArea.getActiveCaretPosition());
                } else {
                    keyboardShown = false;
                }
            }
        });
        BasicCodeAreaColorsProfile basicColors = codeArea.getBasicColors().orElse(null);
        if (basicColors == null) {
            throw new IllegalStateException("Missing colors profile");
        }
        basicColors.setContext(activity);
        basicColors.reinitialize();
        codeArea.resetColors();
        codeArea.setMinimumHeight(120);
        if (templateCodeArea != null) {
            codeArea.setCodeFont(templateCodeArea.getCodeFont());
            codeArea.setCodeType(templateCodeArea.getCodeType());
            codeArea.setCharset(templateCodeArea.getCharset());
            codeArea.setCodeCharactersCase(templateCodeArea.getCodeCharactersCase());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.search_title));

        LayoutInflater inflater = activity.getLayoutInflater();
        searchView = inflater.inflate(R.layout.search_view, null);

        FrameLayout frameLayout = searchView.findViewById(R.id.frameLayout);
        frameLayout.addView(editText);

        if (searchParameters != null) {
            loadSearchParameters();
        }

        TabLayout tabLayout = searchView.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSwitched(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        builder.setView(searchView);
        builder.setPositiveButton(R.string.button_search, (dialog, which) -> {
            saveSearchParameters();
            binarySearch.performFind(searchParameters, searchStatusListener);
        });
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {
            binarySearch.cancelSearch();
            binarySearch.clearSearch();
        });
        return builder.create();
    }

    private void tabSwitched(TabLayout.Tab tab) {
        FrameLayout frameLayout = searchView.findViewById(R.id.frameLayout);
        int tabPos = tab.getPosition();
        if (tabPos != lastTab) {
            frameLayout.removeView(lastTab == 0 ? editText : codeArea);
            frameLayout.addView(tabPos == 0 ? editText : codeArea);
            SwitchCompat matchCaseSwitch = searchView.findViewById(R.id.match_case);
            matchCaseSwitch.setEnabled(tabPos == 0);
            lastTab = tabPos;
        }
    }

    private void loadSearchParameters() {
        SearchCondition condition = searchParameters.getCondition();
        editText.setText(condition.getSearchText());
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        data.clear();
        data.insert(0, condition.getBinaryData());
        codeAreaCodeAreaCaretListener.caretMoved(codeArea.getActiveCaretPosition());

        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            // Should work automatically, but force for now
            keyboardShown = false;
        } else {
            TabLayout tabLayout = searchView.findViewById(R.id.tabLayout);
            TabLayout.Tab binaryTab = tabLayout.getTabAt(1);
            tabLayout.selectTab(binaryTab);
            tabSwitched(binaryTab);
        }
        SwitchCompat matchCaseSwitch = searchView.findViewById(R.id.match_case);
        matchCaseSwitch.setChecked(searchParameters.isMatchCase());
        SwitchCompat backwardDirectionSwitch = searchView.findViewById(R.id.backward_direction);
        backwardDirectionSwitch.setChecked(searchParameters.getSearchDirection() == SearchParameters.SearchDirection.BACKWARD);
        SwitchCompat multipleMatchesSwitch = searchView.findViewById(R.id.multiple_matches);
        multipleMatchesSwitch.setChecked(searchParameters.getMatchMode() == SearchParameters.MatchMode.MULTIPLE);
        SwitchCompat fromCursorSwitch = searchView.findViewById(R.id.from_cursor);
        fromCursorSwitch.setChecked(searchParameters.isSearchFromCursor());
    }

    private void saveSearchParameters() {
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSearchMode(lastTab == 0 ? SearchCondition.SearchMode.TEXT : SearchCondition.SearchMode.BINARY);
        searchCondition.setSearchText(editText.getText().toString());
        JnaBufferEditableData data = new JnaBufferEditableData();
        data.insert(0, codeArea.getContentData());
        searchCondition.setBinaryData(data);
        searchParameters = new SearchParameters();
        searchParameters.setCondition(searchCondition);
        SwitchCompat matchCaseSwitch = searchView.findViewById(R.id.match_case);
        searchParameters.setMatchCase(matchCaseSwitch.isChecked());
        SwitchCompat backwardDirectionSwitch = searchView.findViewById(R.id.backward_direction);
        searchParameters.setSearchDirection(backwardDirectionSwitch.isChecked() ? SearchParameters.SearchDirection.BACKWARD : SearchParameters.SearchDirection.FORWARD);
        SwitchCompat multipleMatchesSwitch = searchView.findViewById(R.id.multiple_matches);
        searchParameters.setMatchMode(multipleMatchesSwitch.isChecked() ? SearchParameters.MatchMode.MULTIPLE : SearchParameters.MatchMode.SINGLE);
        SwitchCompat fromCursorSwitch = searchView.findViewById(R.id.from_cursor);
        searchParameters.setSearchFromCursor(fromCursorSwitch.isChecked());
        MainActivity activity = (MainActivity) requireActivity();
        activity.setSearchParameters(searchParameters);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSearchParameters();
    }

    @ParametersAreNonnullByDefault
    private class CodeAreaKeyListener implements View.OnKeyListener {

        private final KeyListener keyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
        private final Editable editable = Editable.Factory.getInstance().newEditable("");

        public CodeAreaKeyListener() {
            editable.clear();
            Selection.setSelection(editable, 0, 0);
        }

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (!codeArea.isFocused()) {
                View currentFocus = getActivity().getCurrentFocus();
                if (currentFocus != null) {
                    return currentFocus.dispatchKeyEvent(keyEvent);
                }

                return false;
            }

            if (keyboardShown && MainActivity.isAndroidTV(codeArea.getContext())) {
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
                        codeArea.showContextMenu();
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
}
