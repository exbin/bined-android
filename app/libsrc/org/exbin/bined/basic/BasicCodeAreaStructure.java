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
package org.exbin.bined.basic;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataProvider;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ViewModeCapable;

/**
 * Code area data representation structure for basic variant.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaStructure {

    protected final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    @Nonnull
    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;

    @Nonnull
    protected CodeType codeType = CodeType.HEXADECIMAL;

    protected long dataSize;
    @Nonnull
    protected RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    protected int maxBytesPerLine;
    protected int wrappingBytesGroupSize;

    protected long rowsPerDocument;
    protected int bytesPerRow;
    protected int charactersPerRow;

    public BasicCodeAreaStructure() {
    }

    public void updateCache(DataProvider codeArea, int charactersPerPage) {
        viewMode = ((ViewModeCapable) codeArea).getViewMode();
        codeType = ((CodeTypeCapable) codeArea).getCodeType();
        dataSize = codeArea.getDataSize();
        rowWrapping = ((RowWrappingCapable) codeArea).getRowWrapping();
        maxBytesPerLine = ((RowWrappingCapable) codeArea).getMaxBytesPerRow();
        wrappingBytesGroupSize = ((RowWrappingCapable) codeArea).getWrappingBytesGroupSize();

        bytesPerRow = layout.computeBytesPerRow(this, charactersPerPage);
        charactersPerRow = layout.computeCharactersPerRow(this);
        rowsPerDocument = layout.computeRowsPerDocument(this);
    }

    public int computePositionByte(int rowCharPosition) {
        return layout.computePositionByte(this, rowCharPosition);
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return layout.computeFirstCodeCharacterPos(this, byteOffset);
    }

    @Nonnull
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction, int rowsPerPage) {
        return layout.computeMovePosition(this, position, direction, rowsPerPage);
    }

    @Nonnull
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Nonnull
    public CodeType getCodeType() {
        return codeType;
    }

    public long getDataSize() {
        return dataSize;
    }

    @Nonnull
    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public int getMaxBytesPerLine() {
        return maxBytesPerLine;
    }

    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    public long getRowsPerDocument() {
        return rowsPerDocument;
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public int getCharactersPerRow() {
        return charactersPerRow;
    }
}
