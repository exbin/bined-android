/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.component;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Numeric grouping for binary status.
 */
@ParametersAreNonnullByDefault
public class StatusNumericGrouping {

    public static final int DEFAULT_OCTAL_SPACE_GROUP_SIZE = 4;
    public static final int DEFAULT_DECIMAL_SPACE_GROUP_SIZE = 3;
    public static final int DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE = 4;

    protected int octalSpaceGroupSize = DEFAULT_OCTAL_SPACE_GROUP_SIZE;
    protected int decimalSpaceGroupSize = DEFAULT_DECIMAL_SPACE_GROUP_SIZE;
    protected int hexadecimalSpaceGroupSize = DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE;

    public StatusNumericGrouping() {
    }

    public StatusNumericGrouping(int octalSpaceGroupSize, int decimalSpaceGroupSize, int hexadecimalSpaceGroupSize) {
        this.octalSpaceGroupSize = octalSpaceGroupSize;
        this.decimalSpaceGroupSize = decimalSpaceGroupSize;
        this.hexadecimalSpaceGroupSize = hexadecimalSpaceGroupSize;
    }

    public int getOctalSpaceGroupSize() {
        return octalSpaceGroupSize;
    }

    public void setOctalSpaceGroupSize(int octalSpaceGroupSize) {
        this.octalSpaceGroupSize = octalSpaceGroupSize;
    }

    public int getDecimalSpaceGroupSize() {
        return decimalSpaceGroupSize;
    }

    public void setDecimalSpaceGroupSize(int decimalSpaceGroupSize) {
        this.decimalSpaceGroupSize = decimalSpaceGroupSize;
    }

    public int getHexadecimalSpaceGroupSize() {
        return hexadecimalSpaceGroupSize;
    }

    public void setHexadecimalSpaceGroupSize(int hexadecimalSpaceGroupSize) {
        this.hexadecimalSpaceGroupSize = hexadecimalSpaceGroupSize;
    }
}
