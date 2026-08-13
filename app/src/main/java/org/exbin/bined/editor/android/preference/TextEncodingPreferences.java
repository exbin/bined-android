/*
 * Copyright (C) ExBin Project, https://exbin.org
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

import org.exbin.bined.editor.android.options.TextEncodingOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;

/**
 * Text editor encodings preferences.
 */
@NullMarked
public class TextEncodingPreferences implements TextEncodingOptions {

    public static final String PREFERENCES_TEXT_ENCODING_PREFIX = "textEncoding.";
    public static final String PREFERENCES_TEXT_ENCODING_DEFAULT = PREFERENCES_TEXT_ENCODING_PREFIX + "default";
    public static final String PREFERENCES_TEXT_ENCODING_SELECTED = "selectedEncoding";

    private final OptionsStorage optionsStorage;

    public TextEncodingPreferences(OptionsStorage optionsStorage) {
        this.optionsStorage = optionsStorage;
    }

    public String getDefaultEncoding() {
        return optionsStorage.get(PREFERENCES_TEXT_ENCODING_DEFAULT, BinaryEditorPreferences.ENCODING_UTF8);
    }

    public void setDefaultEncoding(String encodingName) {
        optionsStorage.put(PREFERENCES_TEXT_ENCODING_DEFAULT, encodingName);
    }

    @Override
    public String getSelectedEncoding() {
        return optionsStorage.get(PREFERENCES_TEXT_ENCODING_SELECTED, BinaryEditorPreferences.ENCODING_UTF8);
    }

    @Override
    public void setSelectedEncoding(String encodingName) {
        optionsStorage.put(PREFERENCES_TEXT_ENCODING_SELECTED, encodingName);
    }

    @Override
    public List<String> getEncodings() {
        List<String> encodings = new ArrayList<>();
        Optional<String> value;
        int i = 0;
        do {
            value = optionsStorage.get(PREFERENCES_TEXT_ENCODING_PREFIX + Integer.toString(i));
            if (value.isPresent()) {
                encodings.add(value.get());
                i++;
            }
        } while (value.isPresent());

        return encodings;
    }

    @Override
    public void setEncodings(List<String> encodings) {
        for (int i = 0; i < encodings.size(); i++) {
            optionsStorage.put(PREFERENCES_TEXT_ENCODING_PREFIX + Integer.toString(i), encodings.get(i));
        }
        optionsStorage.remove(PREFERENCES_TEXT_ENCODING_PREFIX + Integer.toString(encodings.size()));
    }
}
