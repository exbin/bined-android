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

import org.exbin.bined.editor.android.options.MainOptions;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Application main preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MainPreferences implements MainOptions {

    public static final String PREFERENCES_THEME = "theme";
    public static final String PREFERENCES_LOCALE_LANGUAGE = "locale.language";
    public static final String PREFERENCES_LOCALE_COUNTRY = "locale.country";
    public static final String PREFERENCES_LOCALE_VARIANT = "locale.variant";
    public static final String PREFERENCES_LOCALE_TAG = "locale.tag";

    private final Preferences preferences;

    public MainPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    @Override
    public String getLocaleLanguage() {
        return preferences.get(PREFERENCES_LOCALE_LANGUAGE, "");
    }

    @Nonnull
    @Override
    public String getLocaleCountry() {
        return preferences.get(PREFERENCES_LOCALE_COUNTRY, "");
    }

    @Nonnull
    @Override
    public String getLocaleVariant() {
        return preferences.get(PREFERENCES_LOCALE_VARIANT, "");
    }

    @Nonnull
    @Override
    public String getLocaleTag() {
        return preferences.get(PREFERENCES_LOCALE_TAG, "");
    }

    @Nonnull
    public Locale getLocale() {
        String localeTag = getLocaleTag();
        if (!localeTag.trim().isEmpty()) {
            try {
                return Locale.forLanguageTag(localeTag);
            } catch (SecurityException ex) {
                // Ignore it in java webstart
            }
        }

        String localeLanguage = getLocaleLanguage();
        String localeCountry = getLocaleCountry();
        String localeVariant = getLocaleVariant();
        try {
            return new Locale(localeLanguage, localeCountry, localeVariant);
        } catch (SecurityException ex) {
            // Ignore it in java webstart
        }

        return Locale.ROOT;
    }

    @Nonnull
    @Override
    public String getTheme() {
        return preferences.get(PREFERENCES_THEME, "default");
    }

    @Override
    public void setLocaleLanguage(String language) {
        preferences.put(PREFERENCES_LOCALE_LANGUAGE, language);
    }

    @Override
    public void setLocaleCountry(String country) {
        preferences.put(PREFERENCES_LOCALE_COUNTRY, country);
    }

    @Override
    public void setLocaleVariant(String variant) {
        preferences.put(PREFERENCES_LOCALE_VARIANT, variant);
    }

    @Override
    public void setLocaleTag(String variant) {
        preferences.put(PREFERENCES_LOCALE_TAG, variant);
    }

    public void setLocale(Locale locale) {
        setLocaleTag(locale.toLanguageTag());
        setLocaleLanguage(locale.getLanguage());
        setLocaleCountry(locale.getCountry());
        setLocaleVariant(locale.getVariant());
    }

    @Override
    public void setTheme(String theme) {
        preferences.put(PREFERENCES_THEME, theme);
    }
}
