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

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.exbin.bined.editor.android.options.DataInspectorMode;
import org.exbin.bined.editor.android.options.KeysPanelMode;
import org.exbin.bined.editor.android.preference.BinaryEditorPreferences;
import org.exbin.bined.editor.android.preference.EditorPreferences;
import org.exbin.bined.editor.android.preference.HeaderFragment;
import org.exbin.bined.editor.android.preference.PreferencesWrapper;
import org.exbin.framework.bined.FileHandlingMode;

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

    private BinaryEditorPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
//            getWindow().getDecorView().setPadding(0, getStatusBarHeight(), 0, 0);
//        }

        super.onCreate(savedInstanceState);
        appPreferences = getAppPreferences();
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_activity, null);
        setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }

            view.setPadding(0, getStatusBarHeight() + actionBarHeight, 0, 0);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment(), "header_fragment")
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Nonnull
    public BinaryEditorPreferences getAppPreferences() {
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

        super.onSupportNavigateUp();
        finish();
        return true;
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

    @Override
    public void finish() {
        HeaderFragment fragment = (HeaderFragment) getSupportFragmentManager().findFragmentByTag("header_fragment");
        // Save to preferences
        EditorPreferences editorPreferences = getAppPreferences().getEditorPreferences();
        editorPreferences.setFileHandlingMode(FileHandlingMode.valueOf(((ListPreference) fragment.findPreference(HeaderFragment.FILE_HANDLING_MODE)).getValue().toUpperCase()));
        editorPreferences.setKeysPanelMode(KeysPanelMode.valueOf(((ListPreference) fragment.findPreference(HeaderFragment.KEYS_PANEL_MODE)).getValue().toUpperCase()));
        editorPreferences.setDataInspectorMode(DataInspectorMode.valueOf(((ListPreference) fragment.findPreference(HeaderFragment.DATA_INSPECTOR_MODE)).getValue().toUpperCase()));

        super.finish();
    }

    private int getStatusBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}