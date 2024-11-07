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

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.exbin.bined.editor.android.MainActivity;
import org.exbin.bined.editor.android.R;
import org.exbin.bined.editor.android.SettingsActivity;
import org.exbin.bined.editor.android.options.Theme;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Settings appearance fragment.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class AppearanceFragment extends PreferenceFragmentCompat {

    public static final String LANGUAGE_KEY = "language";
    public static final String THEME_KEY = "theme";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);

        // Load from preferences
        SettingsActivity activity = (SettingsActivity) requireActivity();
        MainPreferences mainPreferences = activity.getAppPreferences().getMainPreferences();
        ListPreference languagePreference = findPreference(LANGUAGE_KEY);
        languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            String language = (String) newValue;
            // Dynamically change language
            LocaleListCompat locales = MainActivity.getLanguageLocaleList("default".equals(language) ? "" : language);
            AppCompatDelegate.setApplicationLocales(locales);

            // Update title for possibly switched language
            Resources resources = getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocales((LocaleList) AppCompatDelegate.getApplicationLocales().unwrap());
            resources = getContext().createConfigurationContext(configuration).getResources();

            activity.setTitle(resources.getString(R.string.pref_header_appearance));

            return true;
        });
        String localeTag = mainPreferences.getLocaleTag();
        languagePreference.setValue(localeTag.isEmpty() ? "default" : localeTag);

        ListPreference themePreference = findPreference(THEME_KEY);
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            // Dynamically change theme
            if (Theme.DARK.name().equalsIgnoreCase((String) newValue)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (Theme.LIGHT.name().equalsIgnoreCase((String) newValue)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }

            return true;
        });
        themePreference.setValue(mainPreferences.getTheme());
    }

    @Override
    public void onDestroy() {
        // Save to preferences
        SettingsActivity activity = (SettingsActivity) requireActivity();
        BinaryEditorPreferences appPreferences = activity.getAppPreferences();
        MainPreferences mainPreferences = appPreferences.getMainPreferences();
        String value = ((ListPreference) findPreference(LANGUAGE_KEY)).getValue();
        mainPreferences.setLocaleTag("default".equals(value) ? "" : value);
        mainPreferences.setTheme(((ListPreference) findPreference(THEME_KEY)).getValue());

        super.onDestroy();
    }
}
