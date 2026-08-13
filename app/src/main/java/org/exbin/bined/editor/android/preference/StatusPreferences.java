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

import org.exbin.bined.PositionCodeType;
import org.exbin.bined.component.StatusNumericGrouping;
import org.exbin.bined.editor.android.options.StatusOptions;
import org.exbin.bined.component.StatusCursorPositionFormat;
import org.exbin.bined.component.StatusDocumentSizeFormat;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jspecify.annotations.NullMarked;

/**
 * Code area status panel preferences.
 */
@NullMarked
public class StatusPreferences implements StatusOptions {

    public static final String PREFERENCES_CURSOR_POSITION_CODE_TYPE = "statusCursorPositionFormat";
    public static final String PREFERENCES_CURSOR_POSITION_SHOW_OFFSET = "statusCursorShowOffset";
    public static final String PREFERENCES_DOCUMENT_SIZE_CODE_TYPE = "statusDocumentSizeFormat";
    public static final String PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE = "statusDocumentShowRelative";
    public static final String PREFERENCES_OCTAL_SPACE_GROUP_SIZE = "statusOctalSpaceGroupSize";
    public static final String PREFERENCES_DECIMAL_SPACE_GROUP_SIZE = "statusDecimalSpaceGroupSize";
    public static final String PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE = "statusHexadecimalSpaceGroupSize";

    private final OptionsStorage optionsStorage;

    public StatusPreferences(OptionsStorage optionsStorage) {
        this.optionsStorage = optionsStorage;
    }

    public PositionCodeType getCursorPositionCodeType() {
        PositionCodeType defaultCodeType = PositionCodeType.DECIMAL;
        try {
            return PositionCodeType.valueOf(optionsStorage.get(PREFERENCES_CURSOR_POSITION_CODE_TYPE, defaultCodeType.name()));
        } catch (Exception ex) {
            Logger.getLogger(StatusPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    public void setCursorPositionCodeType(PositionCodeType statusCursorPositionCodeType) {
        optionsStorage.put(PREFERENCES_CURSOR_POSITION_CODE_TYPE, statusCursorPositionCodeType.name());
    }

    public boolean isCursorShowOffset() {
        return optionsStorage.getBoolean(PREFERENCES_CURSOR_POSITION_SHOW_OFFSET, true);
    }

    public void setCursorShowOffset(boolean statusCursorShowOffset) {
        optionsStorage.putBoolean(PREFERENCES_CURSOR_POSITION_SHOW_OFFSET, statusCursorShowOffset);
    }

    public PositionCodeType getDocumentSizeCodeType() {
        PositionCodeType defaultCodeType = PositionCodeType.DECIMAL;
        try {
            return PositionCodeType.valueOf(optionsStorage.get(PREFERENCES_DOCUMENT_SIZE_CODE_TYPE, defaultCodeType.name()));
        } catch (Exception ex) {
            Logger.getLogger(StatusPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    public void setDocumentSizeCodeType(PositionCodeType statusDocumentSizeCodeType) {
        optionsStorage.put(PREFERENCES_DOCUMENT_SIZE_CODE_TYPE, statusDocumentSizeCodeType.name());
    }

    public boolean isDocumentSizeShowRelative() {
        return optionsStorage.getBoolean(PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE, true);
    }

    public void setDocumentSizeShowRelative(boolean statusDocumentSizeShowRelative) {
        optionsStorage.putBoolean(PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE, statusDocumentSizeShowRelative);
    }

    @Override
    public StatusCursorPositionFormat getCursorPositionFormat() {
        return new StatusCursorPositionFormat(getCursorPositionCodeType(), isCursorShowOffset());
    }

    @Override
    public StatusDocumentSizeFormat getDocumentSizeFormat() {
        return new StatusDocumentSizeFormat(getDocumentSizeCodeType(), isDocumentSizeShowRelative());
    }

    @Override
    public void setCursorPositionFormat(StatusCursorPositionFormat cursorPositionFormat) {
        setCursorPositionCodeType(cursorPositionFormat.getCodeType());
        setCursorShowOffset(cursorPositionFormat.isShowOffset());
    }

    @Override
    public void setDocumentSizeFormat(StatusDocumentSizeFormat documentSizeFormat) {
        setDocumentSizeCodeType(documentSizeFormat.getCodeType());
        setDocumentSizeShowRelative(documentSizeFormat.isShowRelative());
    }

    @Override
    public int getOctalSpaceGroupSize() {
        return optionsStorage.getInt(PREFERENCES_OCTAL_SPACE_GROUP_SIZE, StatusNumericGrouping.DEFAULT_OCTAL_SPACE_GROUP_SIZE);
    }

    @Override
    public void setOctalSpaceGroupSize(int octalSpaceSize) {
        optionsStorage.putInt(PREFERENCES_OCTAL_SPACE_GROUP_SIZE, octalSpaceSize);
    }

    @Override
    public int getDecimalSpaceGroupSize() {
        return optionsStorage.getInt(PREFERENCES_DECIMAL_SPACE_GROUP_SIZE, StatusNumericGrouping.DEFAULT_DECIMAL_SPACE_GROUP_SIZE);
    }

    @Override
    public void setDecimalSpaceGroupSize(int decimalSpaceSize) {
        optionsStorage.putInt(PREFERENCES_DECIMAL_SPACE_GROUP_SIZE, decimalSpaceSize);
    }

    @Override
    public int getHexadecimalSpaceGroupSize() {
        return optionsStorage.getInt(PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE, StatusNumericGrouping.DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE);
    }

    @Override
    public void setHexadecimalSpaceGroupSize(int hexadecimalSpaceSize) {
        optionsStorage.putInt(PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE, hexadecimalSpaceSize);
    }
}
