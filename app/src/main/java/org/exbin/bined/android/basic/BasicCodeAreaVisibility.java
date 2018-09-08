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

import javax.annotation.Nonnull;
import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.CodeAreaScrollPosition;

/**
 * Basic code area component characters visibility in scroll window.
 *
 * @version 0.2.0 2017/08/28
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaVisibility {

    private int previewRelativeX;
    private int visibleCharStart;
    private int visibleCharEnd;
    private int visibleMatrixCharEnd;
    private int visiblePreviewStart;
    private int visiblePreviewEnd;
    private int visibleCodeStart;
    private int visibleCodeEnd;
    private int visibleMatrixCodeEnd;

    public void recomputeCharPositions(@Nonnull BasicCodeAreaMetrics metrics, @Nonnull BasicCodeAreaStructure structure, @Nonnull BasicCodeAreaDimensions dimensions, @Nonnull CodeAreaScrollPosition scrollPosition) {
        int dataViewWidth = dimensions.getDataViewWidth();
        int previewCharPos = structure.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        previewRelativeX = previewCharPos * characterWidth;

        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerCodeSection = structure.getCharactersPerCodeSection();
        int bytesPerRow = structure.getBytesPerRow();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            visibleCharStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleCharStart < 0) {
                visibleCharStart = 0;
            }
            visibleCharEnd = ((scrollPosition.getCharPosition() + dimensions.getCharactersPerRect()) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleCharEnd > structure.getCharactersPerRow()) {
                visibleCharEnd = structure.getCharactersPerRow();
            }
            visibleMatrixCharEnd = (dataViewWidth + (scrollPosition.getCharPosition() + charactersPerCodeSection) * characterWidth + scrollPosition.getCharOffset()) / characterWidth;
            if (visibleMatrixCharEnd > charactersPerCodeSection) {
                visibleMatrixCharEnd = charactersPerCodeSection;
            }
            visibleCodeStart = structure.computePositionByte(visibleCharStart);
            visibleCodeEnd = structure.computePositionByte(visibleCharEnd - 1) + 1;
            visibleMatrixCodeEnd = structure.computePositionByte(visibleMatrixCharEnd - 1) + 1;
        } else {
            visibleCharStart = 0;
            visibleCharEnd = -1;
            visibleCodeStart = 0;
            visibleCodeEnd = -1;
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            visiblePreviewStart = (scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewStart < 0) {
                visiblePreviewStart = 0;
            }
            if (visibleCodeEnd < 0) {
                visibleCharStart = visiblePreviewStart + previewCharPos;
            }
            visiblePreviewEnd = (dataViewWidth + (scrollPosition.getCharPosition() + 1) * characterWidth + scrollPosition.getCharOffset()) / characterWidth - previewCharPos;
            if (visiblePreviewEnd > bytesPerRow) {
                visiblePreviewEnd = bytesPerRow;
            }
            if (visiblePreviewEnd >= 0) {
                visibleCharEnd = visiblePreviewEnd + previewCharPos;
            }
        } else {
            visiblePreviewStart = 0;
            visiblePreviewEnd = -1;
        }
    }

    public int getPreviewRelativeX() {
        return previewRelativeX;
    }

    public int getVisibleCharStart() {
        return visibleCharStart;
    }

    public int getVisibleCharEnd() {
        return visibleCharEnd;
    }

    public int getVisibleMatrixCharEnd() {
        return visibleMatrixCharEnd;
    }

    public int getVisiblePreviewStart() {
        return visiblePreviewStart;
    }

    public int getVisiblePreviewEnd() {
        return visiblePreviewEnd;
    }

    public int getVisibleCodeStart() {
        return visibleCodeStart;
    }

    public int getVisibleCodeEnd() {
        return visibleCodeEnd;
    }

    public int getVisibleMatrixCodeEnd() {
        return visibleMatrixCodeEnd;
    }
}
