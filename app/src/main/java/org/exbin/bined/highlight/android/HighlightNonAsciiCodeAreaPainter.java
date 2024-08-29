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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.android.CodeAreaCore;

/**
 * Experimental support for highlighting of non-ascii characters.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class HighlightNonAsciiCodeAreaPainter extends HighlightCodeAreaPainter {

    private Integer controlCodes;
    private Integer upperCodes;
    private Integer textColor;
    private boolean nonAsciiHighlightingEnabled = true;

    public HighlightNonAsciiCodeAreaPainter(CodeAreaCore codeArea) {
        super(codeArea);

        textColor = Color.BLACK; // TODO codeArea.getForeground(); //MainColors().getTextColor();
        if (textColor == null) {
            textColor = Color.BLACK;
        }

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

        controlCodes = Color.rgb(
                controlCodesRed,
                downShift(Color.green(textColor), controlCodesBlueDiff + controlCodesRedDiff),
                controlCodesBlue);

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

        upperCodes = Color.rgb(
                downShift(Color.red(textColor), upperCodesGreenDiff + upperCodesBlueDiff),
                upperCodesGreen, upperCodesBlue);
    }

    private int downShift(int color, int diff) {
        if (color < diff) {
            return 0;
        }

        return color - diff;
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, @Nonnull CodeAreaSection section) {
        Integer color = super.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
        if (nonAsciiHighlightingEnabled && section == BasicCodeAreaSection.CODE_MATRIX) {
            if (color == null || textColor.equals(color)) {
                long dataPosition = rowDataPosition + byteOnRow;
                if (dataPosition < codeArea.getDataSize()) {
                    byte value = codeArea.getContentData().getByte(dataPosition);
                    if (value < 0) {
                        color = upperCodes;
                    } else if (value < 0x20) {
                        color = controlCodes;
                    }
                }
            }
        }

        return color;
    }

    @Nonnull
    public Integer getControlCodes() {
        return controlCodes;
    }

    public void setControlCodes(Integer controlCodes) {
        this.controlCodes = controlCodes;
    }

    @Nonnull
    public Integer getUpperCodes() {
        return upperCodes;
    }

    public void setUpperCodes(Integer upperCodes) {
        this.upperCodes = upperCodes;
    }

    public boolean isNonAsciiHighlightingEnabled() {
        return nonAsciiHighlightingEnabled;
    }

    public void setNonAsciiHighlightingEnabled(boolean nonAsciiHighlightingEnabled) {
        this.nonAsciiHighlightingEnabled = nonAsciiHighlightingEnabled;
    }
}
