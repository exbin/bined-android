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
package org.exbin.bined.editor.android.options.impl;

import org.exbin.bined.component.StatusNumericGrouping;
import org.exbin.bined.editor.android.options.StatusOptions;
import org.exbin.bined.editor.android.preference.StatusPreferences;
import org.exbin.bined.component.StatusCursorPositionFormat;
import org.exbin.bined.component.StatusDocumentSizeFormat;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Status panel options.
 */
@ParametersAreNonnullByDefault
public class StatusOptionsImpl implements StatusOptions {

    private StatusCursorPositionFormat cursorPositionFormat = new StatusCursorPositionFormat();
    private StatusDocumentSizeFormat documentSizeFormat = new StatusDocumentSizeFormat();
    private StatusNumericGrouping statusNumericGrouping = new StatusNumericGrouping();

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
        return statusNumericGrouping.getOctalSpaceGroupSize();
    }

    @Override
    public void setOctalSpaceGroupSize(int octalSpaceGroupSize) {
        statusNumericGrouping.setOctalSpaceGroupSize(octalSpaceGroupSize);
    }

    @Override
    public int getDecimalSpaceGroupSize() {
        return statusNumericGrouping.getDecimalSpaceGroupSize();
    }

    @Override
    public void setDecimalSpaceGroupSize(int decimalSpaceGroupSize) {
        statusNumericGrouping.setDecimalSpaceGroupSize(decimalSpaceGroupSize);
    }

    @Override
    public int getHexadecimalSpaceGroupSize() {
        return statusNumericGrouping.getHexadecimalSpaceGroupSize();
    }

    @Override
    public void setHexadecimalSpaceGroupSize(int hexadecimalSpaceGroupSize) {
        statusNumericGrouping.setHexadecimalSpaceGroupSize(hexadecimalSpaceGroupSize);
    }

    public void loadFromPreferences(StatusPreferences preferences) {
        cursorPositionFormat.setCodeType(preferences.getCursorPositionCodeType());
        cursorPositionFormat.setShowOffset(preferences.isCursorShowOffset());
        documentSizeFormat.setCodeType(preferences.getDocumentSizeCodeType());
        documentSizeFormat.setShowRelative(preferences.isDocumentSizeShowRelative());
        statusNumericGrouping.setOctalSpaceGroupSize(preferences.getOctalSpaceGroupSize());
        statusNumericGrouping.setDecimalSpaceGroupSize(preferences.getDecimalSpaceGroupSize());
        statusNumericGrouping.setHexadecimalSpaceGroupSize(preferences.getHexadecimalSpaceGroupSize());
    }

    public void saveToPreferences(StatusPreferences preferences) {
        preferences.setCursorPositionCodeType(cursorPositionFormat.getCodeType());
        preferences.setCursorShowOffset(cursorPositionFormat.isShowOffset());
        preferences.setDocumentSizeCodeType(documentSizeFormat.getCodeType());
        preferences.setDocumentSizeShowRelative(documentSizeFormat.isShowRelative());
        preferences.setOctalSpaceGroupSize(statusNumericGrouping.getOctalSpaceGroupSize());
        preferences.setDecimalSpaceGroupSize(statusNumericGrouping.getDecimalSpaceGroupSize());
        preferences.setHexadecimalSpaceGroupSize(statusNumericGrouping.getHexadecimalSpaceGroupSize());
    }

    public void setOptions(StatusOptionsImpl statusOptions) {
        cursorPositionFormat = statusOptions.cursorPositionFormat;
        documentSizeFormat = statusOptions.documentSizeFormat;
        statusNumericGrouping = statusOptions.statusNumericGrouping;
    }
}
