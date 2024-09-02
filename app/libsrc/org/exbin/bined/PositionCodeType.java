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
 * Enumeration of supported position code types.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum PositionCodeType {

    /**
     * Represent code as number in base 8.
     * <p>
     * Code is represented as characters of range 0 to 7.
     */
    OCTAL(8, 3),
    /**
     * Represent code as number in base 10.
     * <p>
     * Code is represented as characters of range 0 to 9.
     */
    DECIMAL(10, 3),
    /**
     * Represent code as number in base 16.
     * <p>
     * Code is represented as characters of range 0 to 9 and A to F.
     */
    HEXADECIMAL(16, 2);

    private final int base;
    private final double baseLog;
    private final int maxDigitsForByte;

    PositionCodeType(int base, int maxDigitsForByte) {
        this.base = base;
        baseLog = Math.log(base);
        this.maxDigitsForByte = maxDigitsForByte;
    }

    /**
     * Returns numerical base.
     *
     * @return numerical base
     */
    public int getBase() {
        return base;
    }

    /**
     * Returns natural logarithm of the base.
     *
     * @return natural logarithm of the base
     */
    public double getBaseLog() {
        return baseLog;
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
