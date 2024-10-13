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

import org.exbin.bined.android.Font;
import org.exbin.bined.editor.android.R;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Application font preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class FontPreference extends EditTextPreference {

    public FontPreference(@NonNull Context context) {
        super(context);
    }

    public FontPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        Font currentFont = new Font();
        try {
            currentFont.setSize(Integer.parseInt(getText()));
        } catch (Exception ex) {
            // ignore
        }
        FontPreference.showFontSelectionDialog(getContext(), currentFont, (codeFont) -> {
            setText(String.valueOf(codeFont.getSize()));
        });
    }

    public static void showFontSelectionDialog(Context context, Font codeFont, FontSelectionListener resultListener) {
        // TODO support for font family / accents
        // http://www.ulduzsoft.com/2012/01/enumerating-the-fonts-on-android-platform/
        // http://www.ulduzsoft.com/2012/01/fontpreference-dialog-for-android/
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.font);
        CharSequence[] fontHeights = new CharSequence[] {"20", "25", "30", "35", "40", "45", "50", "60", "70", "80", "90", "100"};
        int currentSelection = -1;
        String currentSize = String.valueOf(codeFont.getSize());
        for (int i = 0; i < fontHeights.length; i++) {
            if (fontHeights[i].equals(currentSize)) {
                currentSelection = i;
                break;
            }
        }
        builder.setSingleChoiceItems(fontHeights, currentSelection, (dialog, which) -> {
            Font resultFont = Font.create(codeFont);
            resultFont.setSize(Integer.parseInt(fontHeights[which].toString()));
            resultListener.resultFont(resultFont);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public interface FontSelectionListener {
        void resultFont(Font codeFont);
    }
}