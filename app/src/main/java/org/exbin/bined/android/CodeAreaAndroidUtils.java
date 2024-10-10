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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CharsetStreamTranslator;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.android.capability.CharAssessorPainterCapable;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Hexadecimal editor component android utilities.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaAndroidUtils {

    public static final int MIN_MONOSPACE_CODE_POINT = 0x1F;
    public static final int MAX_MONOSPACE_CODE_POINT = 0x1C3;
    public static final int INV_SPACE_CODE_POINT = 0x7f;
    public static final int EXCEPTION1_CODE_POINT = 0x8e;
    public static final int EXCEPTION2_CODE_POINT = 0x9e;

    public static int MAX_COLOR_COMPONENT_VALUE = 255;
    public static final String DEFAULT_ENCODING = CharsetStreamTranslator.DEFAULT_ENCODING;

    private CodeAreaAndroidUtils() {
    }

    /**
     * Detect if character is in unicode range covered by monospace fonts width
     * exactly full width.
     *
     * @param character input character
     * @return true if character is suppose to have exactly full width
     */
    public static boolean isMonospaceFullWidthCharacter(char character) {
        return (character > MIN_MONOSPACE_CODE_POINT && (int) character < MAX_MONOSPACE_CODE_POINT
                && character != INV_SPACE_CODE_POINT
                && character != EXCEPTION1_CODE_POINT && character != EXCEPTION2_CODE_POINT);
    }

    public static boolean areSameColors(@Nullable Integer color, @Nullable Integer comparedColor) {
        return (color == null && comparedColor == null) || (color != null && color.equals(comparedColor));
    }

    public static int createOddColor(int color) {
        return Color.rgb(
                computeOddColorComponent(Color.red(color)),
                computeOddColorComponent(Color.green(color)),
                computeOddColorComponent(Color.blue(color)));
    }

    public static int computeOddColorComponent(int colorComponent) {
        return colorComponent + (colorComponent > 64 ? -16 : 16);
    }

    public static int createNegativeColor(int color) {
        return Color.rgb(
                MAX_COLOR_COMPONENT_VALUE - Color.red(color),
                MAX_COLOR_COMPONENT_VALUE - Color.green(color),
                MAX_COLOR_COMPONENT_VALUE - Color.blue(color));
    }

    public static int computeGrayColor(int color) {
        int grayLevel = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
        return Color.rgb(grayLevel, grayLevel, grayLevel);
    }

    @Nullable
    public static Rect computeIntersection(@Nullable Rect rect1, @Nullable Rect rect2) {
        if (rect1 == null || rect2 == null)
            return null;
        if (rect2.left > rect1.right || rect2.right < rect1.left || rect2.top > rect1.bottom || rect2.bottom < rect1.top)
            return null;

        int left = Math.max(rect1.left, rect2.left);
        int top = Math.max(rect1.top, rect2.top);
        int right = Math.min(rect1.right, rect2.right);
        int bottom = Math.min(rect1.bottom, rect2.bottom);
        return new Rect(left, top, right, bottom);
    }

    /**
     * Finds specific color assessor if present.
     *
     * @param <T> color assessor
     * @param painter painter
     * @param assessorClass color assessor class
     * @return color assessor if present
     */
    @Nullable
    public static <T extends CodeAreaColorAssessor> T findColorAssessor(ColorAssessorPainterCapable painter, Class<T> assessorClass) {
        CodeAreaColorAssessor colorAssessor = painter.getColorAssessor();
        do {
            if (assessorClass.isInstance(colorAssessor)) {
                return assessorClass.cast(colorAssessor);
            }
            colorAssessor = colorAssessor.getParentColorAssessor().orElse(null);
        } while (colorAssessor != null);

        return null;
    }

    /**
     * Finds specific character assessor if present.
     *
     * @param <T> character assessor
     * @param painter painter
     * @param assessorClass character assessor class
     * @return character assessor if present
     */
    @Nullable
    public static <T extends CodeAreaCharAssessor> T findCharAssessor(CharAssessorPainterCapable painter, Class<T> assessorClass) {
        CodeAreaCharAssessor charAssessor = painter.getCharAssessor();
        do {
            if (assessorClass.isInstance(charAssessor)) {
                return assessorClass.cast(charAssessor);
            }
            charAssessor = charAssessor.getParentCharAssessor().orElse(null);
        } while (charAssessor != null);

        return null;
    }

    public static int getMetaMaskDown() {
        return KeyEvent.META_CTRL_MASK;
    }

    @Nonnull
    public static ClipData createBinaryDataClipboardData(Context context, BinaryData data, ClipDescription binedDataFlavor) {
        return createBinaryDataClipboardData(context, data, binedDataFlavor, null, null);
    }

    @Nonnull
    public static ClipData createBinaryDataClipboardData(Context context, BinaryData data, ClipDescription binedDataFlavor, @Nullable ClipDescription binaryDataFlavor, @Nullable Charset charset) {
/*        ContentResolver contentResolver = new ContentResolver(context) {
            @androidx.annotation.Nullable
            @Override
            public String[] getStreamTypes(@NonNull Uri url, @NonNull String mimeTypeFilter) {
                return super.getStreamTypes(url, mimeTypeFilter);
            }
        };
        Uri.Builder builder = new Uri.Builder();
        ClipData clipData = ClipData.newUri(contentResolver, "BinEd Code Data", builder.build());
        // TODO clipData.add
*/
        String result;
        try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
            data.saveToStream(byteArrayStream);
            result = charset == null ? byteArrayStream.toString(DEFAULT_ENCODING) : byteArrayStream.toString(charset.name());
        } catch (IOException ex) {
            result = "";
        }
        ClipData clipData = ClipData.newPlainText("text", result);
        return clipData;
    }

    @Nonnull
    public static ClipData createCodeDataClipboardData(Context context, BinaryData data, ClipDescription binaryDataFlavor, CodeType codeType, CodeCharactersCase charactersCase) {
/*        ContentResolver contentResolver = new ContentResolver(context) {
            @androidx.annotation.Nullable
            @Override
            public String[] getStreamTypes(@NonNull Uri url, @NonNull String mimeTypeFilter) {
                return super.getStreamTypes(url, mimeTypeFilter);
            }
        };
        Uri.Builder builder = new Uri.Builder();
        ClipData clipData = ClipData.newUri(contentResolver, "BinEd Code Data", builder.build());
        // TODO clipData.add
 */
        int charsPerByte = codeType.getMaxDigitsForByte() + 1;
        int textLength = (int) (data.getDataSize() * charsPerByte);
        if (textLength > 0) {
            textLength--;
        }

        char[] targetData = new char[textLength];
        Arrays.fill(targetData, ' ');
        for (int i = 0; i < data.getDataSize(); i++) {
            CodeAreaUtils.byteToCharsCode(data.getByte(i), codeType, targetData, i * charsPerByte, charactersCase);
        }
        ClipData clipData = ClipData.newPlainText("text", new String(targetData));
        return clipData;
    }
}
