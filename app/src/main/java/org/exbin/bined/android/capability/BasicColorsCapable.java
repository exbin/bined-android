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
package org.exbin.bined.android.capability;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;

/**
 * Support for basic set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BasicColorsCapable {

    /**
     * Returns basic profile for colors.
     *
     * @return colors profile
     */
    @Nonnull
    Optional<BasicCodeAreaColorsProfile> getBasicColors();

    /**
     * Sets basic profile for colors.
     *
     * @param colorsProfile colors profile
     */
    void setBasicColors(BasicCodeAreaColorsProfile colorsProfile);
}
