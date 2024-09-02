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

import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Specifies caret position as combination of data position, section and code
 * offset of code representation.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface CodeAreaCaretPosition {

    /**
     * Returns specific byte position in the document.
     *
     * @return data position
     */
    long getDataPosition();

    /**
     * Returns character offset position in the code on current position.
     *
     * @return code offset
     */
    int getCodeOffset();

    /**
     * Returns active code area section.
     *
     * @return section
     */
    @Nonnull
    Optional<CodeAreaSection> getSection();

    /**
     * Resets caret position.
     */
    void reset();
}
