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
package org.exbin.framework.bined;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.PositionCodeType;

/**
 * Cursor position format for status.
 *
 * @version 0.2.0 2019/03/15
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatusCursorPositionFormat {

    private PositionCodeType positionCodeType = PositionCodeType.DECIMAL;
    private boolean showOffset = true;

    public StatusCursorPositionFormat() {
    }

    public StatusCursorPositionFormat(PositionCodeType positionCodeType, boolean showOffset) {
        this.positionCodeType = positionCodeType;
        this.showOffset = showOffset;
    }

    @Nonnull
    public PositionCodeType getCodeType() {
        return positionCodeType;
    }

    public void setCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = Objects.requireNonNull(positionCodeType);
    }

    public boolean isShowOffset() {
        return showOffset;
    }

    public void setShowOffset(boolean showOffset) {
        this.showOffset = showOffset;
    }
}
