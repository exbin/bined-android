/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.android;

import android.graphics.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Hexadecimal editor component swing utilities.
 *
 * @version 0.2.0 2018/05/03
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaAndroidUtils {

    public static final int MIN_MONOSPACE_CODE_POINT = 0x1F;
    public static final int MAX_MONOSPACE_CODE_POINT = 0x1C3;
    public static final int INV_SPACE_CODE_POINT = 0x7f;
    public static final int EXCEPTION1_CODE_POINT = 0x8e;
    public static final int EXCEPTION2_CODE_POINT = 0x9e;

    public static int MAX_COMPONENT_VALUE = 255;

    private CodeAreaAndroidUtils() {
    }

    /**
     * Detect if character is in unicode range covered by monospace fonts width
     * exactly full width.
     *
     * @param character input character
     * @return true if character is suppose to have exactly full width
     */
    public static boolean isMonospaceFullWidthCharater(char character) {
        return (character > MIN_MONOSPACE_CODE_POINT && (int) character < MAX_MONOSPACE_CODE_POINT
                && character != INV_SPACE_CODE_POINT
                && character != EXCEPTION1_CODE_POINT && character != EXCEPTION2_CODE_POINT);
    }

    public static boolean areSameColors(@Nullable Integer color, @Nullable Integer comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    @Nonnull
    public static int createOddColor(@Nonnull int color) {
        return Color.rgb(
                computeOddColorComponent(Color.red(color)),
                computeOddColorComponent(Color.green(color)),
                computeOddColorComponent(Color.blue(color)));
    }

    public static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? - 16 : 16);
    }

    @Nonnull
    public static int createNegativeColor(@Nonnull int color) {
        return Color.rgb(
                MAX_COMPONENT_VALUE - Color.red(color),
                MAX_COMPONENT_VALUE - Color.green(color),
                MAX_COMPONENT_VALUE - Color.blue(color));
    }

    @Nonnull
    public static int computeGrayColor(@Nonnull int color) {
        int grayLevel = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
        return Color.rgb(grayLevel, grayLevel, grayLevel);
    }
}
