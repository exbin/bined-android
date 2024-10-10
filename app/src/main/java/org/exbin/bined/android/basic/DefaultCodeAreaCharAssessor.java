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
package org.exbin.bined.android.basic;

import java.nio.charset.Charset;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.android.CodeAreaCharAssessor;
import org.exbin.bined.android.CodeAreaPaintState;

/**
 * Default code area character assessor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCharAssessor implements CodeAreaCharAssessor {

    protected final CodeAreaCharAssessor parentCharAssessor;

    @Nullable
    protected Charset charMappingCharset = null;
    @Nonnull
    protected final char[] charMapping = new char[256];

    protected long dataSize;
    protected int maxBytesPerChar;
    protected byte[] rowData;
    protected Charset charset;

    public DefaultCodeAreaCharAssessor() {
        parentCharAssessor = null;
    }

    public DefaultCodeAreaCharAssessor(@Nullable CodeAreaCharAssessor parentCharAssessor) {
        this.parentCharAssessor = parentCharAssessor;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPaintState) {
        dataSize = codeAreaPaintState.getDataSize();
        charset = codeAreaPaintState.getCharset();
        rowData = codeAreaPaintState.getRowData();
        maxBytesPerChar = codeAreaPaintState.getMaxBytesPerChar();

        
        if (parentCharAssessor != null) {
            parentCharAssessor.startPaint(codeAreaPaintState);
        }
    }

    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (maxBytesPerChar > 1) {
            if (rowDataPosition + maxBytesPerChar > dataSize) {
                maxBytesPerChar = (int) (dataSize - rowDataPosition);
            }

            int charDataLength = maxBytesPerChar;
            if (byteOnRow + charDataLength > rowData.length) {
                charDataLength = rowData.length - byteOnRow;
            }
            String displayString = new String(rowData, byteOnRow, charDataLength, charset);
            if (!displayString.isEmpty()) {
                return displayString.charAt(0);
            }
        } else {
            if (charMappingCharset == null || charMappingCharset != charset) {
                buildCharMapping(charset);
            }

            return charMapping[rowData[byteOnRow] & 0xFF];
        }

        if (parentCharAssessor != null) {
            return parentCharAssessor.getPreviewCharacter(rowDataPosition, byteOnRow, charOnRow, section);
        }

        return ' ';
    }

    @Override
    public char getPreviewCursorCharacter(long rowDataPosition, int byteOnRow, int charOnRow, byte[] cursorData, int cursorDataLength, CodeAreaSection section) {
        if (cursorDataLength == 0) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            String displayString = new String(cursorData, 0, cursorDataLength, charset);
            if (!displayString.isEmpty()) {
                return displayString.charAt(0);
            }
        } else {
            if (charMappingCharset == null || charMappingCharset != charset) {
                buildCharMapping(charset);
            }

            return charMapping[cursorData[0] & 0xFF];
        }

        if (parentCharAssessor != null) {
            return parentCharAssessor.getPreviewCursorCharacter(rowDataPosition, byteOnRow, charOnRow, cursorData, cursorDataLength, section);
        }

        return ' ';
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentCharAssessor);
    }

    /**
     * Precomputes widths for basic ascii characters.
     *
     * @param charset character set
     */
    private void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

}
