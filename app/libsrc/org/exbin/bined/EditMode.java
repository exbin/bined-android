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
 * Enumeration of edit modes.
 *
 * @author ExBin Project (https://exbin.org)
 */
public enum EditMode {

    /**
     * Document cannot be changed.
     */
    READ_ONLY,
    /**
     * Default mode expanding data when necessary.
     * <p>
     * Document is extended by size of the inserted data or when replacing data
     * overflows end of the file.
     */
    EXPANDING,
    /**
     * Data are inserted and replaced, but size of the file remains the same
     * cutting out excessive data.
     */
    CAPPED,
    /**
     * Only overwrite edit mode is allowed and size of document cannot be
     * changed.
     */
    INPLACE
}
