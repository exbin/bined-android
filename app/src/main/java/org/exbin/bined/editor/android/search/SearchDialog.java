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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.tabs.TabLayout;

import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.jna.JnaBufferEditableData;
import org.exbin.bined.CodeAreaCaretListener;
import org.exbin.bined.EditOperation;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.editor.android.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private final BinarySearchService.SearchStatusListener searchStatusListener = new BinarySearchService.SearchStatusListener() {
        @Override
        public void setStatus(BinarySearchService.FoundMatches foundMatches, SearchParameters.MatchMode matchMode) {
            // TODO Add search status panel
        }

        @Override
        public void clearStatus() {

        }
    };
    private final CodeAreaCaretListener codeAreaCodeAreaCaretListener = caretPosition -> {
        boolean showKeyboard = true;
        if (showKeyboard != keyboardShown) {

            keyboardShown = showKeyboard;
            InputMethodManager im = (InputMethodManager) codeArea.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            codeArea.requestFocus();
            if (showKeyboard) {
                // TODO im.setInputMethodAndSubtype();
                im.showSoftInput(codeArea, InputMethodManager.SHOW_IMPLICIT);
                // TODO refit dialog
            } else {
                im.hideSoftInputFromWindow(codeArea.getWindowToken(), 0);
            }
        }
    };
    private CloseListener closeListener = null;

    public void setBinarySearch(BinarySearch binarySearch) {
        this.binarySearch = binarySearch;
    }

    @Nullable
    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    public void setTemplateCodeArea(CodeArea codeArea) {
        templateCodeArea = codeArea;
    }

    public void setSearchParameters(@Nullable SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    @Nonnull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        editText = new EditText(getContext());
        codeArea = new CodeArea(getContext(), null);
        codeArea.setContentData(new JnaBufferEditableData());
        codeArea.setEditOperation(EditOperation.INSERT);
        codeArea.addCaretMovedListener(codeAreaCodeAreaCaretListener);
        codeArea.setOnKeyListener(new CodeAreaKeyListener());
        codeArea.setOnFocusChangeListener((v, hasFocus) -> {
            if (v == codeArea) {
                if (hasFocus) {
                    codeAreaCodeAreaCaretListener.caretMoved(codeArea.getActiveCaretPosition());
                } else {
                    keyboardShown = false;
                }
            }
        });
        if (templateCodeArea != null) {
            codeArea.setCodeFont(templateCodeArea.getCodeFont());
            codeArea.setCodeType(templateCodeArea.getCodeType());
            codeArea.setCharset(templateCodeArea.getCharset());
            codeArea.setCodeCharactersCase(templateCodeArea.getCodeCharactersCase());
        }

        FragmentActivity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.search_title));

        LayoutInflater inflater = activity.getLayoutInflater();
        searchView = inflater.inflate(R.layout.search_view, null);

        if (searchParameters != null) {
            SearchCondition condition = searchParameters.getCondition();
            if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
                editText.setText(condition.getSearchText());
                // Should work automatically, but force for now
                keyboardShown = false;
            } else {
                EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
                data.clear();
                data.insert(0, condition.getBinaryData());
                codeAreaCodeAreaCaretListener.caretMoved(codeArea.getActiveCaretPosition());
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

        FrameLayout frameLayout = searchView.findViewById(R.id.frameLayout);
        frameLayout.addView(editText);
        TabLayout tabLayout = searchView.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabPos = tab.getPosition();
                if (tabPos != lastTab) {
                    frameLayout.removeView(lastTab == 0 ? editText : codeArea);
                    frameLayout.addView(tabPos == 0 ? editText : codeArea);
                    SwitchCompat matchCaseSwitch = searchView.findViewById(R.id.match_case);
                    matchCaseSwitch.setEnabled(tabPos == 0);
                    lastTab = tabPos;
                }
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
            SearchCondition searchCondition = new SearchCondition();
            switch (lastTab) {
                case 0: {
                    searchCondition.setSearchText(editText.getText().toString());
                    break;
                }
                case 1: {
                    searchCondition.setSearchMode(SearchCondition.SearchMode.BINARY);
                    JnaBufferEditableData data = new JnaBufferEditableData();
                    data.insert(0, codeArea.getContentData());
                    searchCondition.setBinaryData(data);
                    break;
                }
            }
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
            binarySearch.performFind(searchParameters, searchStatusListener);
            if (closeListener != null) {
                closeListener.close();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {
            binarySearch.cancelSearch();
            binarySearch.clearSearch();
        });
        return builder.create();
    }

    public void setOnCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    public interface CloseListener {
        void close();
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
