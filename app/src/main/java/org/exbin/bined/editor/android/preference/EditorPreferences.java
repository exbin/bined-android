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

import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.bined.basic.TabKeyHandlingMode;
import org.exbin.bined.editor.android.options.EditorOptions;
import org.exbin.framework.bined.FileHandlingMode;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Binary editor preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditorPreferences implements EditorOptions {

    public static final String PREFERENCES_FILE_HANDLING_MODE = "fileHandlingMode";
    public static final String PREFERENCES_ENTER_KEY_HANDLING_MODE = "enterKeyHandlingMode";
    public static final String PREFERENCES_TAB_KEY_HANDLING_MODE = "tabKeyHandlingMode";

    private final Preferences preferences;

    public EditorPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    @Override
    public FileHandlingMode getFileHandlingMode() {
        FileHandlingMode defaultFileHandlingMode = FileHandlingMode.MEMORY;
        try {
            return FileHandlingMode.valueOf(preferences.get(PREFERENCES_FILE_HANDLING_MODE, defaultFileHandlingMode.name()));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(EditorPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultFileHandlingMode;
        }
    }

    @Override
    public void setFileHandlingMode(FileHandlingMode fileHandlingMode) {
        preferences.put(PREFERENCES_FILE_HANDLING_MODE, fileHandlingMode.name());
    }

    @Nonnull
    @Override
    public EnterKeyHandlingMode getEnterKeyHandlingMode() {
        EnterKeyHandlingMode defaultValue = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
        try {
            return EnterKeyHandlingMode.valueOf(preferences.get(PREFERENCES_ENTER_KEY_HANDLING_MODE, defaultValue.name()));
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    @Override
    public void setEnterKeyHandlingMode(EnterKeyHandlingMode enterKeyHandlingMode) {
        preferences.put(PREFERENCES_ENTER_KEY_HANDLING_MODE, enterKeyHandlingMode.name());
    }

    @Nonnull
    @Override
    public TabKeyHandlingMode getTabKeyHandlingMode() {
        TabKeyHandlingMode defaultValue = TabKeyHandlingMode.PLATFORM_SPECIFIC;
        try {
            return TabKeyHandlingMode.valueOf(preferences.get(PREFERENCES_TAB_KEY_HANDLING_MODE, defaultValue.name()));
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    @Override
    public void setTabKeyHandlingMode(TabKeyHandlingMode tabKeyHandlingMode) {
        preferences.put(PREFERENCES_TAB_KEY_HANDLING_MODE, tabKeyHandlingMode.name());
    }
}
