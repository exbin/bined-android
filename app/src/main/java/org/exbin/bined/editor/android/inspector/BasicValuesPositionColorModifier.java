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
package org.exbin.bined.editor.android.inspector;

import android.graphics.Color;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.framework.bined.BinEdCodeAreaAssessor;

/**
 * Basic values inspector position color modifier.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicValuesPositionColorModifier implements BinEdCodeAreaAssessor.PositionColorModifier {

    private long position = -1;
    private long length;
    private Integer color;
    private boolean darkMode = false;

    public BasicValuesPositionColorModifier() {
        resetColors();
    }

    @Nullable
    @Override
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        if (position >= 0) {
            long dataPosition = rowDataPosition + byteOnRow;
            if (dataPosition >= position && dataPosition < position + length) {
                return color;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        return null;
    }

    @Override
    public void resetColors() {
        color = darkMode ? 0xFF444400 : Color.YELLOW;
    }

    public void setRange(long position, long length) {
        this.position = position;
        this.length = length;
    }

    public void clearRange() {
        this.position = -1;
        this.length = 0;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }
}
