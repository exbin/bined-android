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

import android.app.LocaleManager;
import android.content.Context;
import android.os.Build;
import android.os.LocaleList;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

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

    public static void setApplicationLocales(Context context, LocaleListCompat languageLocaleList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager.class).setApplicationLocales((LocaleList) Objects.requireNonNull(languageLocaleList.unwrap()));
            return;
        }

        try {
            Method setApplicationLocalesMethod = AppCompatDelegate.class.getMethod("setApplicationLocales", LocaleListCompat.class);
            setApplicationLocalesMethod.invoke(null, languageLocaleList);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // No switching available
        }
    }

    @Nonnull
    public static LocaleListCompat getApplicationLocales(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            LocaleList localeList = context.getSystemService(LocaleManager.class).getApplicationLocales();
            return LocaleListCompat.wrap(localeList);
        }

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

    public static void enableEdgeToEdge(ComponentActivity activity) {
        try {
            Class<?> edgeClass = Class.forName("androidx.activity.EdgeToEdge");
            Method getApplicationLocalesMethod = edgeClass.getMethod("enable", ComponentActivity.class);
            Object result = getApplicationLocalesMethod.invoke(null, activity);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            // No switching available
        }
    }
}
