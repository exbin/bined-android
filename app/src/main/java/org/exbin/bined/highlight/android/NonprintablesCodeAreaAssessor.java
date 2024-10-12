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
package org.exbin.bined.highlight.android;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.highlight.android.color.CodeAreaNonprintablesColorType;
import org.exbin.bined.android.CodeAreaCharAssessor;
import org.exbin.bined.android.CodeAreaPaintState;
import org.exbin.bined.android.CodeAreaColorAssessor;
import org.exbin.bined.android.basic.color.CodeAreaColorsProfile;

/**
 * Code area non-printable characters highlighting.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class NonprintablesCodeAreaAssessor implements CodeAreaColorAssessor, CodeAreaCharAssessor {

    protected final CodeAreaColorAssessor parentColorAssessor;
    protected final CodeAreaCharAssessor parentCharAssessor;

    @Nullable
    protected Map<Character, Character> nonprintableCharactersMapping = null;
    protected boolean showNonprintables = true;

    @Nullable
    protected Integer nonprintablesColor;
    @Nullable
    protected Integer nonprintablesBackground;

    public NonprintablesCodeAreaAssessor(@Nullable CodeAreaColorAssessor parentColorAssessor, @Nullable CodeAreaCharAssessor parentCharAssessor) {
        this.parentColorAssessor = parentColorAssessor;
        this.parentCharAssessor = parentCharAssessor;
    }

    public boolean isShowNonprintables() {
        return showNonprintables;
    }

    public void setShowNonprintables(boolean showNonprintables) {
        this.showNonprintables = showNonprintables;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPaintState) {
        if (nonprintableCharactersMapping == null) {
            buildNonprintableCharactersMapping();
        }

        CodeAreaColorsProfile colorsProfile = codeAreaPaintState.getColorsProfile();
        nonprintablesColor = colorsProfile.getColor(CodeAreaNonprintablesColorType.NONPRINTABLES_COLOR);
        if (nonprintablesColor == null) {
            Integer textColor = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
            nonprintablesColor = Color.rgb(Color.red(textColor), (Color.green(textColor) + 128) % 256, (Color.blue(textColor) + 96) % 256);
        }
        nonprintablesBackground = colorsProfile.getColor(CodeAreaNonprintablesColorType.NONPRINTABLES_BACKGROUND);

        if (parentColorAssessor != null) {
            parentColorAssessor.startPaint(codeAreaPaintState);
        }

        if (parentCharAssessor != null) {
            parentCharAssessor.startPaint(codeAreaPaintState);
        }
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (showNonprintables && section == BasicCodeAreaSection.TEXT_PREVIEW) {
            // Cache results to speed up?
            Character character = parentCharAssessor != null ? parentCharAssessor.getPreviewCharacter(rowDataPosition, byteOnRow, charOnRow, section) : null;
            if (character != null && nonprintableCharactersMapping.containsKey(character)) {
                return nonprintablesColor;
            }
        }

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Nullable
    @Override
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (nonprintablesBackground != null && showNonprintables && section == BasicCodeAreaSection.TEXT_PREVIEW) {
            // Cache results to speed up?
            Character character = parentCharAssessor != null ? parentCharAssessor.getPreviewCharacter(rowDataPosition, byteOnRow, charOnRow, section) : null;
            if (character != null && nonprintableCharactersMapping.containsKey(character)) {
                return nonprintablesBackground;
            }
        }

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Nonnull
    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        Character character = parentCharAssessor != null ? parentCharAssessor.getPreviewCharacter(rowDataPosition, byteOnRow, charOnRow, section) : null;
        if (showNonprintables && section == BasicCodeAreaSection.TEXT_PREVIEW && character != null) {
            Character altChar = nonprintableCharactersMapping.get(character);
            return altChar == null ? character : altChar;
        }

        return character == null ? ' ' : character;
    }

    @Nonnull
    @Override
    public char getPreviewCursorCharacter(long rowDataPosition, int byteOnRow, int charOnRow, byte[] cursorData, int cursorDataLength, CodeAreaSection section) {
        Character character = parentCharAssessor != null ? parentCharAssessor.getPreviewCursorCharacter(rowDataPosition, byteOnRow, charOnRow, cursorData, cursorDataLength, section) : null;
        if (showNonprintables && section == BasicCodeAreaSection.TEXT_PREVIEW && character != null) {
            Character altChar = nonprintableCharactersMapping.get(character);
            return altChar == null ? character : altChar;
        }

        return character == null ? ' ' : character;
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentCharAssessor);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentColorAssessor);
    }

    private void buildNonprintableCharactersMapping() {
        nonprintableCharactersMapping = new HashMap<>();
        // Unicode control characters, might not be supported by font
        for (int i = 0; i < 32; i++) {
            nonprintableCharactersMapping.put((char) i, Character.toChars(9216 + i)[0]);
        }
        // Space -> Middle Dot
        nonprintableCharactersMapping.put(' ', Character.toChars(183)[0]);
        // Tab -> Right-Pointing Double Angle Quotation Mark
        nonprintableCharactersMapping.put('\t', Character.toChars(187)[0]);
        // Line Feed -> Currency Sign
        nonprintableCharactersMapping.put('\r', Character.toChars(164)[0]);
        // Carriage Return -> Pilcrow Sign
        nonprintableCharactersMapping.put('\n', Character.toChars(182)[0]);
        // Ideographic Space -> Degree Sign
        nonprintableCharactersMapping.put(Character.toChars(127)[0], Character.toChars(176)[0]);
    }
}
