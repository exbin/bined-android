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
package org.exbin.bined.basic;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Enumeration of modes for enter key handling.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum EnterKeyHandlingMode {
    /**
     * Handle enter using java platform detection (default).
     */
    PLATFORM_SPECIFIC(""),
    /**
     * Single character 13 (0d).
     */
    CR("\r"),
    /**
     * Single character 10 (0a).
     */
    LF("\n"),
    /**
     * Two characters 13 10 (0d0a).
     */
    CRLF("\r\n"),
    /**
     * Don't handle enter key.
     */
    IGNORE("");

    private final String sequence;

    private EnterKeyHandlingMode(String sequence) {
        this.sequence = sequence;
    }

    @Nonnull
    public String getSequence() {
        if (this == PLATFORM_SPECIFIC) {
            return System.lineSeparator();
        }

        return sequence;
    }
}
