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
package org.exbin.bined.editor.android.options;

import org.exbin.bined.component.StatusCursorPositionFormat;
import org.exbin.bined.component.StatusDocumentSizeFormat;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Status panel options.
 */
@ParametersAreNonnullByDefault
public interface StatusOptions {

    @Nonnull
    StatusCursorPositionFormat getCursorPositionFormat();

    int getDecimalSpaceGroupSize();

    @Nonnull
    StatusDocumentSizeFormat getDocumentSizeFormat();

    int getHexadecimalSpaceGroupSize();

    int getOctalSpaceGroupSize();

    void setCursorPositionFormat(StatusCursorPositionFormat cursorPositionFormat);

    void setDecimalSpaceGroupSize(int decimalSpaceGroupSize);

    void setDocumentSizeFormat(StatusDocumentSizeFormat documentSizeFormat);

    void setHexadecimalSpaceGroupSize(int hexadecimalSpaceGroupSize);

    void setOctalSpaceGroupSize(int octalSpaceGroupSize);

}
