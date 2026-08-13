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

import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.editor.android.options.CodeAreaOptions;
import org.exbin.bined.highlight.android.NonAsciiCodeAreaColorAssessor;
import org.exbin.bined.highlight.android.NonprintablesCodeAreaAssessor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jspecify.annotations.NullMarked;

/**
 * Code area preferences.
 */
@NullMarked
public class CodeAreaPreferences implements CodeAreaOptions {

    public static final String PREFERENCES_CODE_TYPE = "codeType";
    public static final String PREFERENCES_SHOW_NONPRINTABLES = "showNonpritables";
    public static final String PREFERENCES_BYTES_PER_LINE = "bytesPerLine";
    public static final String PREFERENCES_LINE_NUMBERS_LENGTH_TYPE = "lineNumbersLengthType";
    public static final String PREFERENCES_LINE_NUMBERS_LENGTH = "lineNumbersLength";
    public static final String PREFERENCES_VIEW_MODE = "viewMode";
    public static final String PREFERENCES_PAINT_LINE_NUMBERS_BACKGROUND = "showLineNumbersBackground";
    public static final String PREFERENCES_POSITION_CODE_TYPE = "positionCodeType";
    public static final String PREFERENCES_HEX_CHARACTERS_CASE = "hexCharactersCase";
    public static final String PREFERENCES_CODE_COLORIZATION = "codeColorization";
    public static final String PREFERENCES_ROW_WRAPPING_MODE = "rowWrappingMode";
    public static final String PREFERENCES_MAX_BYTES_PER_ROW = "maxBytesPerRow";
    public static final String PREFERENCES_MIN_ROW_POSITION_LENGTH = "minRowPositionLength";
    public static final String PREFERENCES_MAX_ROW_POSITION_LENGTH = "maxRowPositionLength";

    private final OptionsStorage optionsStorage;

    public CodeAreaPreferences(OptionsStorage optionsStorage) {
        this.optionsStorage = optionsStorage;
    }

    @Override
    public CodeType getCodeType() {
        CodeType defaultCodeType = CodeType.HEXADECIMAL;
        try {
            return CodeType.valueOf(optionsStorage.get(PREFERENCES_CODE_TYPE, defaultCodeType.name()));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CodeAreaPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    @Override
    public void setCodeType(CodeType codeType) {
        optionsStorage.put(PREFERENCES_CODE_TYPE, codeType.name());
    }

    @Override
    public boolean isShowNonprintables() {
        return optionsStorage.getBoolean(PREFERENCES_SHOW_NONPRINTABLES, true);
    }

    @Override
    public void setShowNonprintables(boolean showNonprintables) {
        optionsStorage.putBoolean(PREFERENCES_SHOW_NONPRINTABLES, showNonprintables);
    }

    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        CodeCharactersCase defaultCharactersCase = CodeCharactersCase.UPPER;
        try {
            return CodeCharactersCase.valueOf(optionsStorage.get(PREFERENCES_HEX_CHARACTERS_CASE, defaultCharactersCase.name()));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CodeAreaPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCharactersCase;
        }
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        optionsStorage.put(PREFERENCES_HEX_CHARACTERS_CASE, codeCharactersCase.name());
    }

    @Override
    public PositionCodeType getPositionCodeType() {
        PositionCodeType defaultCodeType = PositionCodeType.HEXADECIMAL;
        try {
            return PositionCodeType.valueOf(optionsStorage.get(PREFERENCES_POSITION_CODE_TYPE, defaultCodeType.name()));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CodeAreaPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    @Override
    public void setPositionCodeType(PositionCodeType positionCodeType) {
        optionsStorage.put(PREFERENCES_POSITION_CODE_TYPE, positionCodeType.name());
    }

    @Override
    public CodeAreaViewMode getViewMode() {
        CodeAreaViewMode defaultMode = CodeAreaViewMode.DUAL;
        try {
            return CodeAreaViewMode.valueOf(optionsStorage.get(PREFERENCES_VIEW_MODE, defaultMode.name()));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CodeAreaPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultMode;
        }
    }

    @Override
    public void setViewMode(CodeAreaViewMode viewMode) {
        optionsStorage.put(PREFERENCES_VIEW_MODE, viewMode.name());
    }

    public boolean isPaintRowPosBackground() {
        return optionsStorage.getBoolean(PREFERENCES_PAINT_LINE_NUMBERS_BACKGROUND, true);
    }

    public void setPaintRowPosBackground(boolean paintRowPosBackground) {
        optionsStorage.putBoolean(PREFERENCES_PAINT_LINE_NUMBERS_BACKGROUND, paintRowPosBackground);
    }

    @Override
    public boolean isCodeColorization() {
        return optionsStorage.getBoolean(PREFERENCES_CODE_COLORIZATION, true);
    }

    @Override
    public void setCodeColorization(boolean codeColorization) {
        optionsStorage.putBoolean(PREFERENCES_CODE_COLORIZATION, codeColorization);
    }

    @Override
    public RowWrappingMode getRowWrappingMode() {
        RowWrappingMode defaultMode = RowWrappingMode.NO_WRAPPING;
        try {
            return RowWrappingMode.valueOf(optionsStorage.get(PREFERENCES_ROW_WRAPPING_MODE, defaultMode.name()));
        } catch (Exception ex) {
            Logger.getLogger(CodeAreaPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultMode;
        }
    }

    @Override
    public void setRowWrappingMode(RowWrappingMode rowWrappingMode) {
        optionsStorage.put(PREFERENCES_ROW_WRAPPING_MODE, rowWrappingMode.name());
    }

    @Override
    public int getMaxBytesPerRow() {
        return optionsStorage.getInt(PREFERENCES_MAX_BYTES_PER_ROW, 16);
    }

    @Override
    public void setMaxBytesPerRow(int maxBytesPerRow) {
        optionsStorage.putInt(PREFERENCES_MAX_BYTES_PER_ROW, maxBytesPerRow);
    }

    @Override
    public int getMinRowPositionLength() {
        return optionsStorage.getInt(PREFERENCES_MIN_ROW_POSITION_LENGTH, 0);
    }

    @Override
    public void setMinRowPositionLength(int minRowPositionLength) {
        optionsStorage.putInt(PREFERENCES_MIN_ROW_POSITION_LENGTH, minRowPositionLength);
    }

    @Override
    public int getMaxRowPositionLength() {
        return optionsStorage.getInt(PREFERENCES_MAX_ROW_POSITION_LENGTH, 0);
    }

    @Override
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        optionsStorage.putInt(PREFERENCES_MAX_ROW_POSITION_LENGTH, maxRowPositionLength);
    }

    public void applyPreferences(CodeArea codeArea) {
        codeArea.setViewMode(getViewMode());
        codeArea.setCodeType(getCodeType());
        codeArea.setCodeCharactersCase(getCodeCharactersCase());
        codeArea.setRowWrapping(getRowWrappingMode());
        codeArea.setMaxBytesPerRow(getMaxBytesPerRow());
        NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaAndroidUtils.findColorAssessor((ColorAssessorPainterCapable) codeArea.getPainter(), NonprintablesCodeAreaAssessor.class);
        if (nonprintablesCodeAreaAssessor != null) {
            nonprintablesCodeAreaAssessor.setShowNonprintables(isShowNonprintables());
        }
        NonAsciiCodeAreaColorAssessor nonAsciiColorAssessor = CodeAreaAndroidUtils.findColorAssessor((ColorAssessorPainterCapable) codeArea.getPainter(), NonAsciiCodeAreaColorAssessor.class);
        if (nonAsciiColorAssessor != null) {
            nonAsciiColorAssessor.setNonAsciiHighlightingEnabled(isCodeColorization());
        }
    }
}
