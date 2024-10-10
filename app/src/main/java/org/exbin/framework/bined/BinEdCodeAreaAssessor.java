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
package org.exbin.framework.bined;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.highlight.android.NonAsciiCodeAreaColorAssessor;
import org.exbin.bined.highlight.android.NonprintablesCodeAreaAssessor;
import org.exbin.bined.highlight.android.SearchCodeAreaColorAssessor;
import org.exbin.bined.android.CodeAreaCharAssessor;
import org.exbin.bined.android.CodeAreaColorAssessor;
import org.exbin.bined.android.CodeAreaPaintState;

/**
 * Color assessor for binary editor with registrable modifiers.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdCodeAreaAssessor implements CodeAreaColorAssessor, CodeAreaCharAssessor {

    private final List<PositionColorModifier> priorityColorModifiers = new ArrayList<>();
    private final List<PositionColorModifier> colorModifiers = new ArrayList<>();

    private final CodeAreaColorAssessor parentColorAssessor;
    private final CodeAreaCharAssessor parentCharAssessor;

    public BinEdCodeAreaAssessor(@Nullable CodeAreaColorAssessor parentColorAssessor, @Nullable CodeAreaCharAssessor parentCharAssessor) {
        NonAsciiCodeAreaColorAssessor nonAsciiCodeAreaColorAssessor = new NonAsciiCodeAreaColorAssessor(parentColorAssessor);
        NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = new NonprintablesCodeAreaAssessor(nonAsciiCodeAreaColorAssessor, parentCharAssessor);
        SearchCodeAreaColorAssessor searchCodeAreaColorAssessor = new SearchCodeAreaColorAssessor(nonprintablesCodeAreaAssessor);
        this.parentColorAssessor = searchCodeAreaColorAssessor;
        this.parentCharAssessor = nonprintablesCodeAreaAssessor;
    }

    public void addColorModifier(PositionColorModifier colorModifier) {
        colorModifiers.add(colorModifier);
    }

    public void removeColorModifier(PositionColorModifier colorModifier) {
        colorModifiers.remove(colorModifier);
    }

    public void addPriorityColorModifier(PositionColorModifier colorModifier) {
        priorityColorModifiers.add(colorModifier);
    }

    public void removePriorityColorModifier(PositionColorModifier colorModifier) {
        priorityColorModifiers.remove(colorModifier);
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPaintState) {
        for (PositionColorModifier colorModifier : priorityColorModifiers) {
            colorModifier.resetColors();
        }

        for (PositionColorModifier colorModifier : colorModifiers) {
            colorModifier.resetColors();
        }

        if (parentColorAssessor != null) {
            parentColorAssessor.startPaint(codeAreaPaintState);
        }
    }

    @Nullable
    @Override
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        for (PositionColorModifier colorModifier : priorityColorModifiers) {
            Integer positionBackgroundColor = colorModifier.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
            if (positionBackgroundColor != null) {
                return positionBackgroundColor;
            }
        }

        if (!inSelection) {
            for (PositionColorModifier colorModifier : colorModifiers) {
                Integer positionBackgroundColor = colorModifier.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
                if (positionBackgroundColor != null) {
                    return positionBackgroundColor;
                }
            }
        }

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

//        if (color == null || inSelection) {
//            long dataPosition = rowDataPosition + byteOnRow;
//            if (dataPosition > 100 && dataPosition < 300) {
//                if (inSelection && color != null) {
//                    return new Color(
//                            (((int) (dataPosition * 17) % 255) + color.getRed()) / 2,
//                            (((int) (dataPosition * 37) % 255) + color.getGreen()) / 2,
//                            (((int) (dataPosition * 13) % 255) + color.getBlue()) / 2);
//                }
//                return new Color((int) (dataPosition * 17) % 255, (int) (dataPosition * 37) % 255, (int) (dataPosition * 13) % 255);
//            }
//        }
        return null;
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        for (PositionColorModifier colorModifier : priorityColorModifiers) {
            Integer positionTextColor = colorModifier.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
            if (positionTextColor != null) {
                return positionTextColor;
            }
        }

        if (!inSelection) {
            for (PositionColorModifier colorModifier : colorModifiers) {
                Integer positionTextColor = colorModifier.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
                if (positionTextColor != null) {
                    return positionTextColor;
                }
            }
        }

        if (parentColorAssessor != null) {
            return parentColorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
        }

        return null;
    }

    @Override
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        return parentCharAssessor != null ? parentCharAssessor.getPreviewCharacter(rowDataPosition, byteOnRow, charOnRow, section) : ' ';
    }

    @Override
    public char getPreviewCursorCharacter(long rowDataPosition, int byteOnRow, int charOnRow, byte[] cursorData, int cursorDataLength, CodeAreaSection section) {
        return parentCharAssessor != null ? parentCharAssessor.getPreviewCursorCharacter(rowDataPosition, byteOnRow, charOnRow, cursorData, cursorDataLength, section) : ' ';
    }

    @Nonnull
    @Override
    public Optional<CodeAreaCharAssessor> getParentCharAssessor() {
        return Optional.ofNullable(parentCharAssessor);
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentColorAssessor);
    }

    @ParametersAreNonnullByDefault
    public interface PositionColorModifier {

        @Nullable
        Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection);

        @Nullable
        Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection);

        void resetColors();
    }
}
