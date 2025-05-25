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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.android.CodeAreaColorAssessor;
import org.exbin.bined.android.CodeAreaPaintState;
import org.exbin.framework.bined.BinEdCodeAreaAssessor;

import java.util.Optional;

/**
 * Basic values inspector position color modifier.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicValuesPositionColorModifier implements CodeAreaColorAssessor {

    protected CodeAreaColorAssessor parentAssessor;
    protected long position = -1;
    protected long length;
    protected Integer color;
    protected boolean darkMode = false;

    public BasicValuesPositionColorModifier() {
        this(null);
    }

    public BasicValuesPositionColorModifier(@Nullable CodeAreaColorAssessor parentAssessor) {
        this.parentAssessor = parentAssessor;
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

    @Nonnull
    @Override
    public Optional<CodeAreaColorAssessor> getParentColorAssessor() {
        return Optional.ofNullable(parentAssessor);
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean inSelection) {
        return null;
    }

    @Override
    public void startPaint(CodeAreaPaintState codeAreaPaintState) {
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
