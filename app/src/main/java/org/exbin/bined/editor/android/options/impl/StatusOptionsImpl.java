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
package org.exbin.bined.editor.android.options.impl;

import org.exbin.bined.editor.android.options.StatusOptions;
import org.exbin.bined.editor.android.preference.StatusPreferences;
import org.exbin.framework.bined.StatusCursorPositionFormat;
import org.exbin.framework.bined.StatusDocumentSizeFormat;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Status panel options.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatusOptionsImpl implements StatusOptions {

    public static int DEFAULT_OCTAL_SPACE_GROUP_SIZE = 4;
    public static int DEFAULT_DECIMAL_SPACE_GROUP_SIZE = 3;
    public static int DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE = 4;

    private StatusCursorPositionFormat cursorPositionFormat = new StatusCursorPositionFormat();
    private StatusDocumentSizeFormat documentSizeFormat = new StatusDocumentSizeFormat();
    private int octalSpaceGroupSize = DEFAULT_OCTAL_SPACE_GROUP_SIZE;
    private int decimalSpaceGroupSize = DEFAULT_DECIMAL_SPACE_GROUP_SIZE;
    private int hexadecimalSpaceGroupSize = DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE;

    @Nonnull
    @Override
    public StatusCursorPositionFormat getCursorPositionFormat() {
        return cursorPositionFormat;
    }

    @Override
    public void setCursorPositionFormat(StatusCursorPositionFormat cursorPositionFormat) {
        this.cursorPositionFormat = cursorPositionFormat;
    }

    @Nonnull
    @Override
    public StatusDocumentSizeFormat getDocumentSizeFormat() {
        return documentSizeFormat;
    }

    @Override
    public void setDocumentSizeFormat(StatusDocumentSizeFormat documentSizeFormat) {
        this.documentSizeFormat = documentSizeFormat;
    }

    @Override
    public int getOctalSpaceGroupSize() {
        return octalSpaceGroupSize;
    }

    @Override
    public void setOctalSpaceGroupSize(int octalSpaceGroupSize) {
        this.octalSpaceGroupSize = octalSpaceGroupSize;
    }

    @Override
    public int getDecimalSpaceGroupSize() {
        return decimalSpaceGroupSize;
    }

    @Override
    public void setDecimalSpaceGroupSize(int decimalSpaceGroupSize) {
        this.decimalSpaceGroupSize = decimalSpaceGroupSize;
    }

    @Override
    public int getHexadecimalSpaceGroupSize() {
        return hexadecimalSpaceGroupSize;
    }

    @Override
    public void setHexadecimalSpaceGroupSize(int hexadecimalSpaceGroupSize) {
        this.hexadecimalSpaceGroupSize = hexadecimalSpaceGroupSize;
    }

    public void loadFromPreferences(StatusPreferences preferences) {
        cursorPositionFormat.setCodeType(preferences.getCursorPositionCodeType());
        cursorPositionFormat.setShowOffset(preferences.isCursorShowOffset());
        documentSizeFormat.setCodeType(preferences.getDocumentSizeCodeType());
        documentSizeFormat.setShowRelative(preferences.isDocumentSizeShowRelative());
        octalSpaceGroupSize = preferences.getOctalSpaceGroupSize();
        decimalSpaceGroupSize = preferences.getDecimalSpaceGroupSize();
        hexadecimalSpaceGroupSize = preferences.getHexadecimalSpaceGroupSize();
    }

    public void saveToPreferences(StatusPreferences preferences) {
        preferences.setCursorPositionCodeType(cursorPositionFormat.getCodeType());
        preferences.setCursorShowOffset(cursorPositionFormat.isShowOffset());
        preferences.setDocumentSizeCodeType(documentSizeFormat.getCodeType());
        preferences.setDocumentSizeShowRelative(documentSizeFormat.isShowRelative());
        preferences.setOctalSpaceGroupSize(octalSpaceGroupSize);
        preferences.setDecimalSpaceGroupSize(decimalSpaceGroupSize);
        preferences.setHexadecimalSpaceGroupSize(hexadecimalSpaceGroupSize);
    }

    public void setOptions(StatusOptionsImpl statusOptions) {
        cursorPositionFormat = statusOptions.cursorPositionFormat;
        documentSizeFormat = statusOptions.documentSizeFormat;
        octalSpaceGroupSize = statusOptions.octalSpaceGroupSize;
        decimalSpaceGroupSize = statusOptions.decimalSpaceGroupSize;
        hexadecimalSpaceGroupSize = statusOptions.hexadecimalSpaceGroupSize;
    }
}
