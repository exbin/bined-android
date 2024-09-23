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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;

import org.exbin.bined.editor.android.preference.BinaryEditorPreferences;
import org.exbin.bined.editor.android.preference.CodeAreaPreferences;
import org.exbin.bined.editor.android.preference.EncodingPreference;
import org.exbin.bined.editor.android.preference.PreferencesWrapper;
import org.exbin.bined.editor.android.preference.TextEncodingPreferences;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    private BinaryEditorPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferences = new BinaryEditorPreferences(new PreferencesWrapper(getApplicationContext()));
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
/*
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } */
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
// TODO: Replace deprecated
//        fragmentManager.setFragmentResultListener(0, fragment, new FragmentResultListener() {
//            @Override
//            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
//                caller.result
//            }
//        });
        fragment.setTargetFragment(caller, 0);
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
            setPreferencesFromResource(R.xml.appearance_preferences, rootKey);
        }
    }

    @ParametersAreNonnullByDefault
    public static class ViewFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.view_preferences, rootKey);

            SettingsActivity activity = (SettingsActivity) getActivity();

            // Load from preferences
            CodeAreaPreferences codeAreaPreferences = activity.appPreferences.getCodeAreaPreferences();
            TextEncodingPreferences encodingPreferences = activity.appPreferences.getEncodingPreferences();
            ((EncodingPreference) findPreference("encoding")).setText(encodingPreferences.getDefaultEncoding());
            ((ListPreference) findPreference("bytes_per_row")).setValue(String.valueOf(codeAreaPreferences.getMaxBytesPerRow()));
            ((ListPreference) findPreference("view_mode")).setValue(codeAreaPreferences.getViewMode().name());
            ((ListPreference) findPreference("code_type")).setValue(codeAreaPreferences.getCodeType().name());
            ((ListPreference) findPreference("hex_characters_case")).setValue(codeAreaPreferences.getCodeCharactersCase().name());
            ((TwoStatePreference) findPreference("code_colorization")).setChecked(codeAreaPreferences.isCodeColorization());
        }
    }
}