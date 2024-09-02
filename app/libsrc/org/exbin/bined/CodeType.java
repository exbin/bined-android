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
package org.exbin.bined;

/**
 * Enumeration of supported code types.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum CodeType {

    /**
     * Represent code as number in base 2.
     * <p>
     * Code is represented as 8 characters each of range 0 to 1.
     */
    BINARY(2, 8),
    /**
     * Represent code as number in base 8.
     * <p>
     * Code is represented as 3 characters of range 0 to 7 with limit 377.
     */
    OCTAL(8, 3),
    /**
     * Represent code as number in base 10.
     * <p>
     * Code is represented as 3 characters of range 0 to 9 with limit 255.
     */
    DECIMAL(10, 3),
    /**
     * Represent code as number in base 16.
     * <p>
     * Code is represented as 2 characters of range 0 to 9 and A to F.
     */
    HEXADECIMAL(16, 2);

    private final int base;
    private final int maxDigitsForByte;

    CodeType(int base, int maxDigitsForByte) {
        this.base = base;
        this.maxDigitsForByte = maxDigitsForByte;
    }

    /**
     * Returns numeric base for current code type.
     *
     * @return base
     */
    public int getBase() {
        return base;
    }

    /**
     * Returns maximum number of digits per single byte.
     *
     * @return number of digits
     */
    public int getMaxDigitsForByte() {
        return maxDigitsForByte;
    }
}
