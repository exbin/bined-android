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

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.editor.android.preference.BinaryEditorPreferences;
import org.exbin.bined.editor.android.preference.CodeAreaPreferences;
import org.exbin.bined.editor.android.preference.EncodingPreference;
import org.exbin.bined.editor.android.preference.FontPreference;
import org.exbin.bined.editor.android.preference.MainPreferences;
import org.exbin.bined.editor.android.preference.PreferencesWrapper;
import org.exbin.bined.editor.android.preference.TextEncodingPreferences;
import org.exbin.bined.editor.android.preference.TextFontPreferences;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Settings activity.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    public static final String LANGUAGE_KEY = "language";
    public static final String THEME_KEY = "theme";
    public static final String FONT_KEY = "font";
    public static final String ENCODING_KEY = "encoding";
    public static final String BYTES_PER_ROW_KEY = "bytes_per_row";
    public static final String VIEW_MODE_KEY = "view_mode";
    public static final String CODE_TYPE_KEY = "code_type";
    public static final String HEX_CHARACTERS_CASE = "hex_characters_case";
    public static final String CODE_COLORIZATION = "code_colorization";
    public static final String NONPRINTABLE_CHARACTERS = "nonprintable_characters";

    private BinaryEditorPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferences = getAppPreferences();
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });
/*        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } */
    }

    @Nonnull
    private BinaryEditorPreferences getAppPreferences() {
        if (appPreferences == null) {
            return new BinaryEditorPreferences(new PreferencesWrapper(getApplicationContext()));
        }

        return appPreferences;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragmentManager.setFragmentResultListener("requestKey", fragment, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@Nonnull String requestKey, @Nonnull Bundle result) {
                caller.getParentFragmentManager().setFragmentResult(requestKey, result);
            }
        });
        // Replace the existing Fragment with the new Fragment
        fragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    @ParametersAreNonnullByDefault
    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);
        }
    }

    @ParametersAreNonnullByDefault
    public static class AppearanceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.main_preferences, rootKey);

            // Load from preferences
            SettingsActivity activity = (SettingsActivity) requireActivity();
            MainPreferences mainPreferences = activity.getAppPreferences().getMainPreferences();
            ListPreference languagePreference = findPreference(LANGUAGE_KEY);
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String language = (String) newValue;
                // Dynamically change theme
                if ("default".equals(language)) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat. getEmptyLocaleList());
                } else {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(Locale.forLanguageTag(language)));
                }

                return true;
            });
            String localeTag = mainPreferences.getLocaleTag();
            languagePreference.setValue(localeTag.isEmpty() ? "default" : localeTag);

            ListPreference themePreference = findPreference(THEME_KEY);
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Dynamically change theme
                if ("dark".equals(newValue)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else if ("light".equals(newValue)) {
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
            MainPreferences mainPreferences = activity.appPreferences.getMainPreferences();
            String value = ((ListPreference) findPreference(LANGUAGE_KEY)).getValue();
            mainPreferences.setLocaleTag("default".equals(value) ? "" : value);
            mainPreferences.setTheme(((ListPreference) findPreference(THEME_KEY)).getValue());

            super.onDestroy();
        }
    }

    @ParametersAreNonnullByDefault
    public static class ViewFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.view_preferences, rootKey);

            // Load from preferences
            SettingsActivity activity = (SettingsActivity) requireActivity();
            CodeAreaPreferences codeAreaPreferences = activity.appPreferences.getCodeAreaPreferences();
            TextFontPreferences fontPreferences = activity.appPreferences.getFontPreferences();
            ((FontPreference) findPreference(FONT_KEY)).setText(String.valueOf(fontPreferences.getFontSize()));
            TextEncodingPreferences encodingPreferences = activity.appPreferences.getEncodingPreferences();
            ((EncodingPreference) findPreference(ENCODING_KEY)).setText(encodingPreferences.getDefaultEncoding());
            ((ListPreference) findPreference(BYTES_PER_ROW_KEY)).setValue(String.valueOf(codeAreaPreferences.getMaxBytesPerRow()));
            ((ListPreference) findPreference(VIEW_MODE_KEY)).setValue(codeAreaPreferences.getViewMode().name());
            ((ListPreference) findPreference(CODE_TYPE_KEY)).setValue(codeAreaPreferences.getCodeType().name());
            ((ListPreference) findPreference(HEX_CHARACTERS_CASE)).setValue(codeAreaPreferences.getCodeCharactersCase().name());
            ((TwoStatePreference) findPreference(CODE_COLORIZATION)).setChecked(codeAreaPreferences.isCodeColorization());
            ((TwoStatePreference) findPreference(NONPRINTABLE_CHARACTERS)).setChecked(codeAreaPreferences.isShowNonprintables());
        }

        @Override
        public void onDestroy() {
            // Save to preferences
            SettingsActivity activity = (SettingsActivity) requireActivity();
            CodeAreaPreferences codeAreaPreferences = activity.appPreferences.getCodeAreaPreferences();
            TextFontPreferences fontPreferences = activity.appPreferences.getFontPreferences();
            fontPreferences.setFontSize(Integer.parseInt(((FontPreference) findPreference(FONT_KEY)).getText()));
            TextEncodingPreferences encodingPreferences = activity.appPreferences.getEncodingPreferences();
            encodingPreferences.setDefaultEncoding(((EncodingPreference) findPreference(ENCODING_KEY)).getText());
            codeAreaPreferences.setMaxBytesPerRow(Integer.parseInt(((ListPreference) findPreference(BYTES_PER_ROW_KEY)).getValue()));
            codeAreaPreferences.setViewMode(CodeAreaViewMode.valueOf(((ListPreference) findPreference(VIEW_MODE_KEY)).getValue()));
            codeAreaPreferences.setCodeType(CodeType.valueOf(((ListPreference) findPreference(CODE_TYPE_KEY)).getValue()));
            codeAreaPreferences.setCodeCharactersCase(CodeCharactersCase.valueOf(((ListPreference) findPreference(HEX_CHARACTERS_CASE)).getValue()));
            codeAreaPreferences.setCodeColorization(((TwoStatePreference) findPreference(CODE_COLORIZATION)).isChecked());
            codeAreaPreferences.setShowNonprintables(((TwoStatePreference) findPreference(NONPRINTABLE_CHARACTERS)).isChecked());

            super.onDestroy();
        }
    }
}