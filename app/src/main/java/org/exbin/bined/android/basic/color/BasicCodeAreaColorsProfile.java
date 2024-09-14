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
package org.exbin.bined.android.basic.color;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.appcompat.widget.ThemeUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.color.MaterialColors;

import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.editor.android.R;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Basic code area set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaColorsProfile implements CodeAreaColorsProfile {

    @Nullable
    private Integer textColor;
    @Nullable
    private Integer textBackground;
    @Nullable
    private Integer selectionColor;
    @Nullable
    private Integer selectionBackground;
    @Nullable
    private Integer selectionMirrorColor;
    @Nullable
    private Integer selectionMirrorBackground;
    @Nullable
    private Integer alternateColor;
    @Nullable
    private Integer alternateBackground;
    @Nullable
    private Integer cursorColor;
    @Nullable
    private Integer cursorNegativeColor;
    @Nullable
    private Integer decorationLine;
    @Nullable
    private Context context;

    public BasicCodeAreaColorsProfile() {
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTextBackground() {
        return textBackground;
    }

    public int getSelectionColor() {
        return selectionColor;
    }

    public int getSelectionBackground() {
        return selectionBackground;
    }

    public int getSelectionMirrorColor() {
        return selectionMirrorColor;
    }

    public int getSelectionMirrorBackground() {
        return selectionMirrorBackground;
    }

    public int getCursorColor() {
        return cursorColor;
    }

    public int getCursorNegativeColor() {
        return cursorNegativeColor;
    }

    public int getDecorationLine() {
        return decorationLine;
    }

    public int getAlternateBackground() {
        return alternateBackground;
    }

    @Nullable
    @Override
    public Integer getColor(CodeAreaColorType colorType) {
        if (colorType == CodeAreaBasicColors.TEXT_COLOR) {
            return textColor;
        }
        if (colorType == CodeAreaBasicColors.TEXT_BACKGROUND) {
            return textBackground;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_COLOR) {
            return selectionColor;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_BACKGROUND) {
            return selectionBackground;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_MIRROR_COLOR) {
            return selectionMirrorColor;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND) {
            return selectionMirrorBackground;
        }
        if (colorType == CodeAreaBasicColors.ALTERNATE_COLOR) {
            return alternateColor;
        }
        if (colorType == CodeAreaBasicColors.ALTERNATE_BACKGROUND) {
            return alternateBackground;
        }
        if (colorType == CodeAreaBasicColors.CURSOR_COLOR) {
            return cursorColor;
        }
        if (colorType == CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR) {
            return cursorNegativeColor;
        }
        if (colorType == BasicCodeAreaDecorationColorType.LINE) {
            return decorationLine;
        }

        return null;
    }

    @Nullable
    @Override
    public Integer getColor(CodeAreaColorType colorType, @Nullable CodeAreaBasicColors basicAltColor) {
        Integer color = getColor(colorType);
        return (color == null) ? (basicAltColor == null ? null : getColor(basicAltColor)) : color;
    }

    public void setContext(@Nullable Context context) {
        this.context = context;
    }

    public void reinitialize() {
        textColor = getThemeColor(androidx.appcompat.R.attr.editTextColor);
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        textBackground = getThemeColor(androidx.appcompat.R.attr.editTextBackground);
        if (textBackground == null) {
            textBackground = CodeAreaAndroidUtils.createNegativeColor(textColor);
        }
        selectionColor = getThemeColor(androidx.appcompat.R.attr.editTextColor);
        if (selectionColor == null) {
            selectionColor = Color.WHITE;
        }
        selectionBackground = getThemeColor(androidx.appcompat.R.attr.selectableItemBackground);
        if (selectionBackground == null) {
            selectionBackground = Color.rgb(96, 96, 255);
        }
        selectionMirrorColor = selectionColor;
        selectionMirrorBackground = CodeAreaAndroidUtils.computeGrayColor(selectionBackground);
        cursorColor = getThemeColor(androidx.appcompat.R.attr.colorControlActivated);
        if (cursorColor == null) {
            cursorColor = Color.BLACK;
        }
        cursorNegativeColor = CodeAreaAndroidUtils.createNegativeColor(cursorColor);
        decorationLine = Color.GRAY;

        alternateColor = textColor;
        alternateBackground = CodeAreaAndroidUtils.createOddColor(textBackground);
    }

    @Nullable
    private Integer getThemeColor(int attr) {
        if (context == null)
            return null;

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        try {
            return ContextCompat.getColor(context, typedValue.resourceId);
        } catch (Exception ex) {
            try {
                Drawable drawable = ContextCompat.getDrawable(context, typedValue.resourceId);
                if (drawable instanceof ColorDrawable) {
                    return ((ColorDrawable) drawable).getColor();
                }
            } catch (Exception ex2) {
                // ignore
            }
        }
        return null;
    }
}
