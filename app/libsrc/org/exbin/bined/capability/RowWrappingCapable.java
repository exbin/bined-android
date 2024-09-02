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
package org.exbin.bined.capability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.RowWrappingMode;

/**
 * Row wrapping capability interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface RowWrappingCapable {

    /**
     * Returns row wrapping mode.
     *
     * @return row wrapping mode
     */
    @Nonnull
    RowWrappingMode getRowWrapping();

    /**
     * Sets row wrapping mode.
     *
     * @param rowWrapping row wrapping mode
     */
    void setRowWrapping(RowWrappingMode rowWrapping);

    /**
     * Returns maximum number of bytes per row.
     *
     * @return bytes per row
     */
    int getMaxBytesPerRow();

    /**
     * Sets maximum number of bytes per row.
     *
     * @param maxBytesPerRow bytes per row
     */
    void setMaxBytesPerRow(int maxBytesPerRow);

    /**
     * Returns size of the byte group.
     *
     * @return size of the byte group
     */
    int getWrappingBytesGroupSize();

    /**
     * Sets size of the byte group.
     *
     * @param groupSize size of the byte group
     */
    void setWrappingBytesGroupSize(int groupSize);

    /**
     * Returns minimum length of position section of the code area.
     *
     * @return minimum length
     */
    int getMinRowPositionLength();

    /**
     * Sets minimum length of position section of the code area.
     *
     * @param minRowPositionLength minimum length
     */
    void setMinRowPositionLength(int minRowPositionLength);

    /**
     * Returns maximum length of position section of the code area.
     *
     * @return maximum length
     */
    int getMaxRowPositionLength();

    /**
     * Sets maximum length of position section of the code area.
     *
     * @param maxRowPositionLength maximum length
     */
    void setMaxRowPositionLength(int maxRowPositionLength);
}
