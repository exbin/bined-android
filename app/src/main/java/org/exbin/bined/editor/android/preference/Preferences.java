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

import androidx.annotation.Nullable;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Preferences interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface Preferences {

    void flush();

    boolean exists(String key);

    @Nonnull
    Optional<String> get(String key);

    @Nonnull
    String get(String key, String def);

    boolean getBoolean(String key, boolean def);

    @Nonnull
    byte[] getByteArray(String key, byte[] def);

    double getDouble(String key, double def);

    float getFloat(String key, float def);

    int getInt(String key, int def);

    long getLong(String key, long def);

    void put(String key, @Nullable String value);

    void putBoolean(String key, boolean value);

    void putByteArray(String key, byte[] value);

    void putDouble(String key, double value);

    void putFloat(String key, float value);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void remove(String key);

    void sync();
}
