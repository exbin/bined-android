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

/**
 * Enumeration of modes for tab key handling.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum TabKeyHandlingMode {
    /**
     * Handle tab key using java platform detection (default).
     */
    PLATFORM_SPECIFIC,
    /**
     * Insert tab character \t.
     */
    INSERT_TAB,
    /**
     * Insert space characters.
     */
    INSERT_SPACES,
    /**
     * Jump to next code area section.
     */
    CYCLE_TO_NEXT_SECTION,
    /**
     * Jump to previous code area section.
     */
    CYCLE_TO_PREVIOUS_SECTION,
    /**
     * Don't handle tab key.
     */
    IGNORE;
}
