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
package org.exbin.bined.editor.android.options;

import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.basic.CodeAreaViewMode;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Code area options.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CodeAreaOptions {

    @Nonnull
    CodeCharactersCase getCodeCharactersCase();

    @Nonnull
    CodeType getCodeType();

    int getMaxBytesPerRow();

    int getMaxRowPositionLength();

    int getMinRowPositionLength();

    @Nonnull
    PositionCodeType getPositionCodeType();

    @Nonnull
    RowWrappingMode getRowWrappingMode();

    @Nonnull
    CodeAreaViewMode getViewMode();

    boolean isCodeColorization();

    boolean isShowNonprintables();

    void setCodeCharactersCase(CodeCharactersCase codeCharactersCase);

    void setCodeColorization(boolean codeColorization);

    void setCodeType(CodeType codeType);

    void setMaxBytesPerRow(int maxBytesPerRow);

    void setMaxRowPositionLength(int maxRowPositionLength);

    void setMinRowPositionLength(int minRowPositionLength);

    void setPositionCodeType(PositionCodeType positionCodeType);

    void setRowWrappingMode(RowWrappingMode rowWrappingMode);

    void setShowNonprintables(boolean showUnprintables);

    void setViewMode(CodeAreaViewMode viewMode);
}
