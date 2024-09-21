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

import org.exbin.bined.PositionCodeType;
import org.exbin.bined.editor.android.options.StatusOptions;
import org.exbin.bined.editor.android.options.impl.StatusOptionsImpl;
import org.exbin.framework.bined.StatusCursorPositionFormat;
import org.exbin.framework.bined.StatusDocumentSizeFormat;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Code area status panel preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatusPreferences implements StatusOptions {

    public static final String PREFERENCES_CURSOR_POSITION_CODE_TYPE = "statusCursorPositionFormat";
    public static final String PREFERENCES_CURSOR_POSITION_SHOW_OFFSET = "statusCursorShowOffset";
    public static final String PREFERENCES_DOCUMENT_SIZE_CODE_TYPE = "statusDocumentSizeFormat";
    public static final String PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE = "statusDocumentShowRelative";
    public static final String PREFERENCES_OCTAL_SPACE_GROUP_SIZE = "statusOctalSpaceGroupSize";
    public static final String PREFERENCES_DECIMAL_SPACE_GROUP_SIZE = "statusDecimalSpaceGroupSize";
    public static final String PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE = "statusHexadecimalSpaceGroupSize";

    private final Preferences preferences;

    public StatusPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public PositionCodeType getCursorPositionCodeType() {
        PositionCodeType defaultCodeType = PositionCodeType.DECIMAL;
        try {
            return PositionCodeType.valueOf(preferences.get(PREFERENCES_CURSOR_POSITION_CODE_TYPE, defaultCodeType.name()));
        } catch (Exception ex) {
            Logger.getLogger(StatusPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    public void setCursorPositionCodeType(PositionCodeType statusCursorPositionCodeType) {
        preferences.put(PREFERENCES_CURSOR_POSITION_CODE_TYPE, statusCursorPositionCodeType.name());
    }

    public boolean isCursorShowOffset() {
        return preferences.getBoolean(PREFERENCES_CURSOR_POSITION_SHOW_OFFSET, true);
    }

    public void setCursorShowOffset(boolean statusCursorShowOffset) {
        preferences.putBoolean(PREFERENCES_CURSOR_POSITION_SHOW_OFFSET, statusCursorShowOffset);
    }

    @Nonnull
    public PositionCodeType getDocumentSizeCodeType() {
        PositionCodeType defaultCodeType = PositionCodeType.DECIMAL;
        try {
            return PositionCodeType.valueOf(preferences.get(PREFERENCES_DOCUMENT_SIZE_CODE_TYPE, defaultCodeType.name()));
        } catch (Exception ex) {
            Logger.getLogger(StatusPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    public void setDocumentSizeCodeType(PositionCodeType statusDocumentSizeCodeType) {
        preferences.put(PREFERENCES_DOCUMENT_SIZE_CODE_TYPE, statusDocumentSizeCodeType.name());
    }

    public boolean isDocumentSizeShowRelative() {
        return preferences.getBoolean(PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE, true);
    }

    public void setDocumentSizeShowRelative(boolean statusDocumentSizeShowRelative) {
        preferences.putBoolean(PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE, statusDocumentSizeShowRelative);
    }

    @Nonnull
    @Override
    public StatusCursorPositionFormat getCursorPositionFormat() {
        return new StatusCursorPositionFormat(getCursorPositionCodeType(), isCursorShowOffset());
    }

    @Nonnull
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
        return preferences.getInt(PREFERENCES_OCTAL_SPACE_GROUP_SIZE, StatusOptionsImpl.DEFAULT_OCTAL_SPACE_GROUP_SIZE);
    }

    @Override
    public void setOctalSpaceGroupSize(int octalSpaceSize) {
        preferences.putInt(PREFERENCES_OCTAL_SPACE_GROUP_SIZE, octalSpaceSize);
    }

    @Override
    public int getDecimalSpaceGroupSize() {
        return preferences.getInt(PREFERENCES_DECIMAL_SPACE_GROUP_SIZE, StatusOptionsImpl.DEFAULT_DECIMAL_SPACE_GROUP_SIZE);
    }

    @Override
    public void setDecimalSpaceGroupSize(int decimalSpaceSize) {
        preferences.putInt(PREFERENCES_DECIMAL_SPACE_GROUP_SIZE, decimalSpaceSize);
    }

    @Override
    public int getHexadecimalSpaceGroupSize() {
        return preferences.getInt(PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE, StatusOptionsImpl.DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE);
    }

    @Override
    public void setHexadecimalSpaceGroupSize(int hexadecimalSpaceSize) {
        preferences.putInt(PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE, hexadecimalSpaceSize);
    }
}
