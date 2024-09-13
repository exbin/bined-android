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
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;

import org.exbin.bined.editor.android.R;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EncodingPreference extends EditTextPreference {

    public EncodingPreference(@NonNull Context context) {
        super(context);
    }

    public EncodingPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EncodingPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EncodingPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        EncodingPreference.showEncodingSelectionDialog(getContext(), getText(), this::setText);
    }

    @Nullable
    public static void showEncodingSelectionDialog(Context context, @Nullable String currentCharset, EncodingSelectionListener resultListener) {
        // TODO Rework to list with cycle ability
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.encoding);
        int current = -1;
        SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
        int index = 0;
        CharSequence[] encodings = new CharSequence[availableCharsets.size()];
        for (Map.Entry<String, Charset> entry : availableCharsets.entrySet()) {
            if (entry.getKey().equals(currentCharset)) {
                current = index;
            }
            encodings[index] = entry.getValue().name();
            index++;
        }
        builder.setSingleChoiceItems(encodings, current, (dialog, which) -> {
            resultListener.resultEncoding(encodings[which].toString());
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public interface EncodingSelectionListener {
        void resultEncoding(String encoding);
    }
}