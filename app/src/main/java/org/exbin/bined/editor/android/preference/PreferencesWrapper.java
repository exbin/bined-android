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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Preferences wrapper.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PreferencesWrapper implements Preferences {

    private SharedPreferences preferences;

    public PreferencesWrapper(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean exists(String key) {
        return preferences.contains(key);
    }

    @Nonnull
    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(preferences.getString(key, null));
    }

    @Nonnull
    @Override
    public String get(String key, String def) {
        return preferences.getString(key, def);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);
    }

    @Nonnull
    @Override
    public byte[] getByteArray(String key, byte[] def) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getDouble(String key, double def) {
        return preferences.getFloat(key, (float) def);
    }

    @Override
    public float getFloat(String key, float def) {
        return preferences.getFloat(key, def);
    }

    @Override
    public int getInt(String key, int def) {
        return preferences.getInt(key, def);
    }

    @Override
    public long getLong(String key, long def) {
        return preferences.getLong(key, def);
    }

    @Override
    public void put(String key, @Nullable String value) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putDouble(String key, double value) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putFloat(key, (float) value);
        edit.commit();
    }

    @Override
    public void putFloat(String key, float value) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    @Override
    public void putInt(String key, int value) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    @Override
    public void putLong(String key, long value) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    @Override
    public void remove(String key) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.remove(key);
        edit.commit();
    }

    @Override
    public void sync() {
    }
}
