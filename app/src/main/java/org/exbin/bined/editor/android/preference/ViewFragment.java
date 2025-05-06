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
import androidx.preference.TwoStatePreference;

import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.editor.android.R;
import org.exbin.bined.editor.android.SettingsActivity;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * View preferences fragment.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ViewFragment extends PreferenceFragmentCompat {

    public static final String FONT_KEY = "font";
    public static final String ENCODING_KEY = "encoding";
    public static final String BYTES_PER_ROW_KEY = "bytes_per_row";
    public static final String VIEW_MODE_KEY = "view_mode";
    public static final String CODE_TYPE_KEY = "code_type";
    public static final String HEX_CHARACTERS_CASE = "hex_characters_case";
    public static final String CODE_COLORIZATION = "code_colorization";
    public static final String NONPRINTABLE_CHARACTERS = "nonprintable_characters";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.view_preferences, rootKey);

        // Load from preferences
        SettingsActivity activity = (SettingsActivity) requireActivity();
        BinaryEditorPreferences appPreferences = activity.getAppPreferences();
        CodeAreaPreferences codeAreaPreferences = appPreferences.getCodeAreaPreferences();
        TextFontPreferences fontPreferences = appPreferences.getFontPreferences();
        ((FontPreference) findPreference(FONT_KEY)).setText(String.valueOf(fontPreferences.getFontSize()));
        TextEncodingPreferences encodingPreferences = appPreferences.getEncodingPreferences();
        ((EncodingPreference) findPreference(ENCODING_KEY)).setText(encodingPreferences.getDefaultEncoding());
        ((ListPreference) findPreference(BYTES_PER_ROW_KEY)).setValue(String.valueOf(codeAreaPreferences.getMaxBytesPerRow()));
        ((ListPreference) findPreference(VIEW_MODE_KEY)).setValue(codeAreaPreferences.getViewMode().name().toLowerCase());
        ((ListPreference) findPreference(CODE_TYPE_KEY)).setValue(codeAreaPreferences.getCodeType().name().toLowerCase());
        ((ListPreference) findPreference(HEX_CHARACTERS_CASE)).setValue(codeAreaPreferences.getCodeCharactersCase().name().toLowerCase());
        ((TwoStatePreference) findPreference(CODE_COLORIZATION)).setChecked(codeAreaPreferences.isCodeColorization());
        ((TwoStatePreference) findPreference(NONPRINTABLE_CHARACTERS)).setChecked(codeAreaPreferences.isShowNonprintables());
    }

    @Override
    public void onDestroy() {
        // Save to preferences
        SettingsActivity activity = (SettingsActivity) requireActivity();
        BinaryEditorPreferences appPreferences = activity.getAppPreferences();
        CodeAreaPreferences codeAreaPreferences = appPreferences.getCodeAreaPreferences();
        TextFontPreferences fontPreferences = appPreferences.getFontPreferences();
        fontPreferences.setFontSize(Integer.parseInt(((FontPreference) findPreference(FONT_KEY)).getText()));
        TextEncodingPreferences encodingPreferences = appPreferences.getEncodingPreferences();
        encodingPreferences.setDefaultEncoding(((EncodingPreference) findPreference(ENCODING_KEY)).getText());
        String bytesPerRowMode = ((ListPreference) findPreference(BYTES_PER_ROW_KEY)).getValue();
        if (bytesPerRowMode.equals("custom")) {
            // TODO Add support for custom
            bytesPerRowMode = "0";
        }
        codeAreaPreferences.setMaxBytesPerRow(Integer.parseInt(bytesPerRowMode));
        codeAreaPreferences.setViewMode(CodeAreaViewMode.valueOf(((ListPreference) findPreference(VIEW_MODE_KEY)).getValue().toUpperCase()));
        codeAreaPreferences.setCodeType(CodeType.valueOf(((ListPreference) findPreference(CODE_TYPE_KEY)).getValue().toUpperCase()));
        codeAreaPreferences.setCodeCharactersCase(CodeCharactersCase.valueOf(((ListPreference) findPreference(HEX_CHARACTERS_CASE)).getValue().toUpperCase()));
        codeAreaPreferences.setCodeColorization(((TwoStatePreference) findPreference(CODE_COLORIZATION)).isChecked());
        codeAreaPreferences.setShowNonprintables(((TwoStatePreference) findPreference(NONPRINTABLE_CHARACTERS)).isChecked());

        super.onDestroy();
    }
}
