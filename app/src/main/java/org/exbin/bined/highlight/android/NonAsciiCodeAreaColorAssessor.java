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
package org.exbin.bined.highlight.android;

import android.graphics.Color;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.highlight.android.color.CodeAreaColorizationColorType;
import org.exbin.bined.android.CodeAreaPaintState;
import org.exbin.bined.android.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.android.CodeAreaColorAssessor;

/**
 * Support for highlighting of non-ascii characters.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class NonAsciiCodeAreaColorAssessor implements CodeAreaColorAssessor {

    protected final CodeAreaColorAssessor parentAssessor;

    @Nullable
    protected Integer controlCodesColor;
    @Nullable
    protected Integer controlCodesBackground;
    @Nullable
    protected Integer upperCodesColor;
    @Nullable
    protected Integer upperCodesBackground;
    @Nullable
    protected Integer textColor;
    protected boolean nonAsciiHighlightingEnabled = true;

    protected long dataSize;
    protected BinaryData contentData;

    public NonAsciiCodeAreaColorAssessor(@Nullable CodeAreaColorAssessor parentAssessor) {
        this.parentAssessor = parentAssessor;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPaintState) {
        CodeAreaColorsProfile colorsProfile = codeAreaPaintState.getColorsProfile();

        dataSize = codeAreaPaintState.getDataSize();
        contentData = codeAreaPaintState.getContentData();

        textColor = colorsProfile.getColor(CodeAreaBasicColors.TEXT_COLOR);
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        controlCodesColor = colorsProfile.getColor(CodeAreaColorizationColorType.CONTROL_CODES_COLOR);
        if (controlCodesColor == null) {
            int controlCodesRed = Color.red(textColor);
            int controlCodesRedDiff = 0;
            if (controlCodesRed > 32) {
                if (controlCodesRed > 192) {
                    controlCodesRedDiff = controlCodesRed - 192;
                }
                controlCodesRed = 255;
            } else {
                controlCodesRed += 224;
            }

            int controlCodesBlue = Color.blue(textColor);
            int controlCodesBlueDiff = 0;
            if (controlCodesBlue > 32) {
                if (controlCodesBlue > 192) {
                    controlCodesBlueDiff = controlCodesBlue - 192;
                }
                controlCodesBlue = 255;
            } else {
                controlCodesBlue += 224;
            }

            controlCodesColor = Color.rgb(
                    controlCodesRed,
                    downShift(Color.green(textColor), controlCodesBlueDiff + controlCodesRedDiff),
                    controlCodesBlue);
        }
        controlCodesBackground = colorsProfile.getColor(CodeAreaColorizationColorType.CONTROL_CODES_BACKGROUND);

        upperCodesColor = colorsProfile.getColor(CodeAreaColorizationColorType.UPPER_CODES_COLOR);
        if (upperCodesColor == null) {
            int upperCodesGreen = Color.green(textColor);
            int upperCodesGreenDiff = 0;
            if (upperCodesGreen > 64) {
                if (upperCodesGreen > 192) {
                    upperCodesGreenDiff = upperCodesGreen - 192;
                }

                upperCodesGreen = 255;
            } else {
                upperCodesGreen += 192;
            }

            int upperCodesBlue = Color.blue(textColor);
            int upperCodesBlueDiff = 0;
            if (upperCodesBlue > 64) {
                if (upperCodesBlue > 192) {
                    upperCodesBlueDiff = upperCodesBlue - 192;
                }

                upperCodesBlue = 255;
            } else {
                upperCodesBlue += 192;
            }

            upperCodesColor = Color.rgb(
                    downShift(Color.red(textColor), upperCodesGreenDiff + upperCodesBlueDiff),
                    upperCodesGreen, upperCodesBlue);
        }
        upperCodesBackground = colorsProfile.getColor(CodeAreaColorizationColorType.UPPER_CODES_BACKGROUND);

        if (parentAssessor != null) {
            parentAssessor.startPaint(codeAreaPaintState);
        }
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, @Nonnull CodeAreaSection section, boolean inSelection) {
        Integer color = parentAssessor != null ? parentAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection) : null;
        if (nonAsciiHighlightingEnabled && section == BasicCodeAreaSection.CODE_MATRIX) {
            if (color == null || textColor.equals(color)) {
                long dataPosition = rowDataPosition + byteOnRow;
                if (dataPosition < dataSize) {
                    byte value = contentData.getByte(dataPosition);
                    if (value < 0) {
                        color = upperCodesColor;
                    } else if (value < 0x20) {
                        color = controlCodesColor;
                    }
                }
            }
        }

        return color;
    }

    @Nullable
    @Override
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        Integer color = parentAssessor != null ? parentAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection) : null;
        if ((upperCodesBackground != null || controlCodesBackground != null) && nonAsciiHighlightingEnabled && section == BasicCodeAreaSection.CODE_MATRIX) {
            if (color == null || textColor.equals(color)) {
                long dataPosition = rowDataPosition + byteOnRow;
                if (dataPosition < dataSize) {
                    byte value = contentData.getByte(dataPosition);
                    if (upperCodesBackground != null && value < 0) {
                        color = upperCodesBackground;
                    } else if (controlCodesBackground != null && value < 0x20) {
                        color = controlCodesBackground;
                    }
                }
            }
        }

        return color;
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentAssessor);
    }

    public boolean isNonAsciiHighlightingEnabled() {
        return nonAsciiHighlightingEnabled;
    }

    public void setNonAsciiHighlightingEnabled(boolean nonAsciiHighlightingEnabled) {
        this.nonAsciiHighlightingEnabled = nonAsciiHighlightingEnabled;
    }

    private static int downShift(int color, int diff) {
        if (color < diff) {
            return 0;
        }

        return color - diff;
    }
}
