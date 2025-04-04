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

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Compatibility utilities.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CompatUtils {

    private CompatUtils() {
    }

    public static void setApplicationLocales(LocaleListCompat languageLocaleList) {
        try {
            Method setApplicationLocalesMethod = AppCompatDelegate.class.getMethod("setApplicationLocales", LocaleListCompat.class);
            setApplicationLocalesMethod.invoke(null, languageLocaleList);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // No switching available
        }
    }

    @Nonnull
    public static LocaleListCompat getApplicationLocales() {
        try {
            Method getApplicationLocalesMethod = AppCompatDelegate.class.getMethod("getApplicationLocales");
            Object result = getApplicationLocalesMethod.invoke(null);
            if (result instanceof LocaleListCompat) {
                return (LocaleListCompat) result;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // No switching available
        }
        return LocaleListCompat.getEmptyLocaleList();
    }
}
