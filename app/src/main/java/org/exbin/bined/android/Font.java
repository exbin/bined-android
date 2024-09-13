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
package org.exbin.bined.android;

import android.graphics.Paint;
import android.graphics.Typeface;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Font definition.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class Font {

    private Typeface typeface;
    private int size = 30;
    private int fontFlags;

    public static Font fromPaint(Paint paint)
    {
        Font font = new Font();
        font.setFontFlags(paint.getFlags());
        return font;
    }

    public static Font create(Font font) {
        Font fontCopy = new Font();
        fontCopy.typeface = font.typeface;
        fontCopy.size = font.size;
        fontCopy.fontFlags = font.fontFlags;
        return fontCopy;
    }

    @Nullable
    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(@Nullable Typeface typeface) {
        this.typeface = typeface;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFontFlags() {
        return fontFlags;
    }

    public void setFontFlags(int fontFlags) {
        this.fontFlags = fontFlags;
    }
}
