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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.PositionCodeType;

/**
 * Document size format for status.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatusDocumentSizeFormat {

    private PositionCodeType positionCodeType = PositionCodeType.DECIMAL;
    private boolean showRelative = true;

    public StatusDocumentSizeFormat() {

    }

    public StatusDocumentSizeFormat(PositionCodeType positionCodeType, boolean showRelative) {
        this.positionCodeType = positionCodeType;
        this.showRelative = showRelative;
    }

    @Nonnull
    public PositionCodeType getCodeType() {
        return positionCodeType;
    }

    public void setCodeType(PositionCodeType positionCodeType) {
        this.positionCodeType = Objects.requireNonNull(positionCodeType);
    }

    public boolean isShowRelative() {
        return showRelative;
    }

    public void setShowRelative(boolean showRelativeSize) {
        this.showRelative = showRelativeSize;
    }
}
