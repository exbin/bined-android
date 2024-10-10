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
package org.exbin.bined.android;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.android.basic.color.CodeAreaColorsProfile;

/**
 * Code area paint state.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface CodeAreaPaintState {

    @Nonnull
    CodeAreaSection getActiveSection();

    @Nonnull
    CodeAreaColorsProfile getColorsProfile();

    @Nonnull
    Charset getCharset();

    @Nonnull
    byte[] getRowData();

    int getMaxBytesPerChar();

    int getCodeLastCharPos();

    int getCharactersPerRow();

    int getBytesPerRow();

    long getDataSize();

    // TODO: Replace with row data only?
    @Nonnull
    BinaryData getContentData();

    @Nullable
    CodeAreaSelection getSelectionHandler();
}
