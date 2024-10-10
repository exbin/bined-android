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

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.android.CodeAreaPaintState;
import org.exbin.bined.android.CodeAreaColorAssessor;
import org.exbin.bined.android.basic.color.CodeAreaColorsProfile;

/**
 * Default code area color assessor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaColorAssessor implements CodeAreaColorAssessor {

    protected final CodeAreaColorAssessor parentColorAssessor;

    protected CodeAreaSection activeSection = null;
    protected int codeLastCharPos;
    protected Integer selectionColor;
    protected Integer selectionMirrorColor;
    protected Integer selectionBackground;
    protected Integer selectionMirrorBackground;

    public DefaultCodeAreaColorAssessor() {
        parentColorAssessor = null;
    }

    public DefaultCodeAreaColorAssessor(@Nullable CodeAreaColorAssessor parentColorAssessor) {
        this.parentColorAssessor = parentColorAssessor;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPainterState) {
        activeSection = codeAreaPainterState.getActiveSection();
        codeLastCharPos = codeAreaPainterState.getCodeLastCharPos();
        CodeAreaColorsProfile colorsProfile = codeAreaPainterState.getColorsProfile();
        selectionColor = colorsProfile.getColor(CodeAreaBasicColors.SELECTION_COLOR);
        selectionMirrorColor = colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_COLOR);
        selectionBackground = colorsProfile.getColor(CodeAreaBasicColors.SELECTION_BACKGROUND);
        selectionMirrorBackground = colorsProfile.getColor(CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND);

        if (parentColorAssessor != null) {
            parentColorAssessor.startPaint(codeAreaPainterState);
        }
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (inSelection) {
            return section == activeSection ? selectionColor : selectionMirrorColor;
        }

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Nullable
    @Override
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (inSelection && (section == BasicCodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == activeSection ? selectionBackground : selectionMirrorBackground;
        }

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentColorAssessor);
    }
}
