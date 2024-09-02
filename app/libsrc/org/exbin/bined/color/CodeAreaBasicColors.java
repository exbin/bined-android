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
package org.exbin.bined.color;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Enumeration of color types for main group.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum CodeAreaBasicColors implements CodeAreaColorType {

    TEXT_COLOR("textColor", BasicCodeAreaColorGroup.MAIN),
    TEXT_BACKGROUND("textBackground", BasicCodeAreaColorGroup.MAIN),
    SELECTION_COLOR("selectionColor", BasicCodeAreaColorGroup.SELECTION),
    SELECTION_BACKGROUND("selectionBackground", BasicCodeAreaColorGroup.SELECTION),
    SELECTION_MIRROR_COLOR("selectionMirrorColor", BasicCodeAreaColorGroup.SELECTION),
    SELECTION_MIRROR_BACKGROUND("selectionMirrorBackground", BasicCodeAreaColorGroup.SELECTION),
    ALTERNATE_COLOR("alternateColor", BasicCodeAreaColorGroup.MAIN),
    ALTERNATE_BACKGROUND("alternateBackground", BasicCodeAreaColorGroup.MAIN),
    CURSOR_COLOR("cursorColor", null),
    CURSOR_NEGATIVE_COLOR("cursorNegativeColor", null);

    @Nonnull
    private final String typeId;
    @Nullable
    private final BasicCodeAreaColorGroup group;

    CodeAreaBasicColors(String typeId, @Nullable BasicCodeAreaColorGroup group) {
        this.typeId = typeId;
        this.group = group;
    }

    @Nonnull
    @Override
    public String getId() {
        return typeId;
    }

    @Nonnull
    @Override
    public Optional<CodeAreaColorGroup> getGroup() {
        return Optional.ofNullable(group);
    }
}
