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
package org.exbin.bined.editor.android.preference;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.exbin.bined.editor.android.R;
import org.exbin.bined.editor.android.SettingsActivity;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * View preferences fragment.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class HeaderFragment extends PreferenceFragmentCompat {

    public static final String APPEARANCE_GROUP = "appearance_group";
    public static final String VIEW_GROUP ="view_group";
    public static final String FILE_HANDLING_MODE = "file_handling_mode";
    public static final String KEYS_PANEL_MODE = "keys_panel_mode";
    public static final String DATA_INSPECTOR_MODE = "data_inspector_mode";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.header_preferences, rootKey);

        SettingsActivity activity = (SettingsActivity) requireActivity();
        EditorPreferences editorPreferences = activity.getAppPreferences().getEditorPreferences();
        findPreference(APPEARANCE_GROUP).setFragment(AppearanceFragment.class.getCanonicalName());
        findPreference(VIEW_GROUP).setFragment(ViewFragment.class.getCanonicalName());
        ((ListPreference) findPreference(FILE_HANDLING_MODE)).setValue(editorPreferences.getFileHandlingMode().name().toLowerCase());
        ((ListPreference) findPreference(KEYS_PANEL_MODE)).setValue(editorPreferences.getKeysPanelMode().name().toLowerCase());
        ((ListPreference) findPreference(DATA_INSPECTOR_MODE)).setValue(editorPreferences.getDataInspectorMode().name().toLowerCase());
    }
}
