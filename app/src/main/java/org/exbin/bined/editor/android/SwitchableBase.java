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
package org.exbin.bined.editor.android;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.PositionCodeType;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SwitchableBase {

    protected static final int LENGTH_LIMIT = 21;
    protected final char[] cache = new char[LENGTH_LIMIT];

    protected PositionCodeType codeType = PositionCodeType.DECIMAL;

    @Nonnull
    public PositionCodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(PositionCodeType codeType) {
        this.codeType = codeType;
    }

    @Nonnull
    public String getPositionAsString(long position) {
        if (position < 0) {
            return "-" + getNonNegativePositionAsString(-position);
        }
        return getNonNegativePositionAsString(position);
    }

    @Nonnull
    public String getNonNegativePositionAsString(long position) {
        Arrays.fill(cache, ' ');
        CodeAreaUtils.longToBaseCode(cache, 0, position, codeType.getBase(), LENGTH_LIMIT, false, CodeCharactersCase.LOWER);
        return new String(cache).trim();
    }

    public long valueOfPosition(String position) {
        return Long.parseLong(position, codeType.getBase());
    }
}