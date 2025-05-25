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
package org.exbin.bined.android.basic;

import android.graphics.Paint;

import org.exbin.bined.CharsetStreamTranslator;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaMetrics {

    @Nullable
    protected Paint fontMetrics;
    protected boolean monospaceFont;

    protected int rowHeight;
    protected int characterWidth;
    protected int fontHeight;
    protected int maxBytesPerChar;
    protected int subFontSpace = 0;

    public void recomputeMetrics(@Nullable Paint fontMetrics, Charset charset) {
        this.fontMetrics = fontMetrics;
        if (fontMetrics == null) {
            characterWidth = 0;
            fontHeight = 0;
        } else {
            fontHeight = (int) fontMetrics.getTextSize();
            rowHeight = fontHeight;

            /*
             * Use small 'w' character to guess normal font width.
             */
            characterWidth = (int) fontMetrics.measureText("w");
            int fontSize = (int) fontMetrics.getTextSize();
            subFontSpace = (rowHeight / 6);

            /*
             * Compare it to small 'i' to detect if font is monospaced.
             *
             * TODO: Is there better way?
             */
            monospaceFont = characterWidth == fontMetrics.measureText(" ") && characterWidth == fontMetrics.measureText("i");
        }

        try {
            CharsetEncoder encoder = charset.newEncoder();
            maxBytesPerChar = (int) encoder.maxBytesPerChar();
        } catch (UnsupportedOperationException ex) {
            maxBytesPerChar = CharsetStreamTranslator.DEFAULT_MAX_BYTES_PER_CHAR;
        }
    }

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

    @Nonnull
    public Optional<Paint> getFontMetrics() {
        return Optional.ofNullable(fontMetrics);
    }

    public int getCharWidth(char value) {
        return (int) fontMetrics.measureText(String.valueOf(value));
    }

    public int getCharsWidth(char[] data, int offset, int length) {
        return (int) fontMetrics.measureText(String.valueOf(data, offset, length));
    }

    public boolean isMonospaceFont() {
        return monospaceFont;
    }

    public boolean hasUniformLineMetrics() {
        return false; // TODO fontMetrics.hasUniformLineMetrics();
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getCharacterWidth() {
        return characterWidth;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public int getMaxBytesPerChar() {
        return maxBytesPerChar;
    }
}
