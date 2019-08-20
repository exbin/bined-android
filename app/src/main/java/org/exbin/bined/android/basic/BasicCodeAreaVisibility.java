/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.android.basic;

import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.basic.BasicCodeAreaLayout;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Basic code area component characters visibility in scroll window.
 *
 * @version 0.2.0 2019/08/18
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicCodeAreaVisibility {

    private int splitLinePos;

    private int skipToCode;
    private int skipToChar;
    private int skipToPreview;
    private int skipRestFromCode;
    private int skipRestFromChar;
    private int skipRestFromPreview;

    private boolean codeSectionVisible;
    private boolean previewSectionVisible;

    private int charactersPerCodeSection;
    private int codeLastCharPos;
    private int previewCharPos;
    private int previewRelativeX;

    public void recomputeCharPositions(BasicCodeAreaMetrics metrics, BasicCodeAreaStructure structure, BasicCodeAreaDimensions dimensions, BasicCodeAreaLayout layout, BasicCodeAreaScrolling scrolling) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int charsPerByte = structure.getCodeType().getMaxDigitsForByte() + 1;
        previewRelativeX = previewCharPos * characterWidth;

        CodeAreaViewMode viewMode = structure.getViewMode();

        int invisibleFromLeftX = scrolling.getHorizontalScrollX(characterWidth);
        int invisibleFromRightX = invisibleFromLeftX + dimensions.getDataViewWidth();

        charactersPerCodeSection = layout.computeFirstCodeCharacterPos(structure, bytesPerRow);

        // Compute first and last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            codeLastCharPos = bytesPerRow * charsPerByte - 1;
        } else {
            codeLastCharPos = 0;
        }

        if (viewMode == CodeAreaViewMode.DUAL) {
            previewCharPos = bytesPerRow * charsPerByte;
        } else {
            previewCharPos = 0;
        }

        skipToCode = 0;
        skipToChar = 0;
        skipToPreview = 0;
        skipRestFromCode = -1;
        skipRestFromChar = -1;
        skipRestFromPreview = -1;
        codeSectionVisible = viewMode != CodeAreaViewMode.TEXT_PREVIEW;
        previewSectionVisible = viewMode != CodeAreaViewMode.CODE_MATRIX;

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            skipToChar = invisibleFromLeftX / characterWidth;
            if (skipToChar < 0) {
                skipToChar = 0;
            }
            skipRestFromChar = (invisibleFromRightX + characterWidth - 1) / characterWidth;
            if (skipRestFromChar > structure.getCharactersPerRow()) {
                skipRestFromChar = structure.getCharactersPerRow();
            }
            skipToCode = structure.computePositionByte(skipToChar);
            skipRestFromCode = structure.computePositionByte(skipRestFromChar - 1) + 1;
            if (skipRestFromCode > bytesPerRow) {
                skipRestFromCode = bytesPerRow;
            }
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            skipToPreview = invisibleFromLeftX / characterWidth - previewCharPos;
            if (skipToPreview < 0) {
                skipToPreview = 0;
            }
            if (skipToPreview > 0) {
                skipToChar = skipToPreview + previewCharPos;
            }
            skipRestFromPreview = (invisibleFromRightX + characterWidth - 1) / characterWidth - previewCharPos;
            if (skipRestFromPreview > bytesPerRow) {
                skipRestFromPreview = bytesPerRow;
            }
            if (skipRestFromPreview >= 0) {
                skipRestFromChar = skipRestFromPreview + previewCharPos;
            }
        }
    }

    /**
     * Returns pixel position of slit line relative to data view or 0 if not in
     * use.
     *
     * @return x-position or 0
     */
    public int getSplitLinePos() {
        return splitLinePos;
    }

    public int getSkipToCode() {
        return skipToCode;
    }

    public int getSkipToChar() {
        return skipToChar;
    }

    public int getSkipToPreview() {
        return skipToPreview;
    }

    public int getSkipRestFromCode() {
        return skipRestFromCode;
    }

    public int getSkipRestFromChar() {
        return skipRestFromChar;
    }

    public int getSkipRestFromPreview() {
        return skipRestFromPreview;
    }

    public boolean isCodeSectionVisible() {
        return codeSectionVisible;
    }

    public boolean isPreviewSectionVisible() {
        return previewSectionVisible;
    }

    public int getMaxRowDataChars() {
        return skipRestFromChar - skipToChar;
    }

    public int getCharactersPerCodeSection() {
        return charactersPerCodeSection;
    }

    public int getCodeLastCharPos() {
        return codeLastCharPos;
    }

    public int getPreviewCharPos() {
        return previewCharPos;
    }

    public int getPreviewRelativeX() {
        return previewRelativeX;
    }
}
