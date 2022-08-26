/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.bined;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;

/**
 * Binary editor status interface.
 *
 * @version 0.2.1 2021/08/13
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryStatusApi {

    /**
     * Reports cursor position.
     *
     * @param cursorPosition cursor position
     */
    void setCursorPosition(CodeAreaCaretPosition cursorPosition);

    /**
     * Sets current selection.
     *
     * @param selectionRange current selection
     */
    void setSelectionRange(SelectionRange selectionRange);

    /**
     * Reports currently active edit mode.
     *
     * @param mode edit mode
     * @param operation edit operation
     */
    void setEditMode(EditMode mode, EditOperation operation);

    /**
     * Sets current document size.
     *
     * @param documentSize document size
     * @param initialDocumentSize document size when file was opened
     */
    void setCurrentDocumentSize(long documentSize, long initialDocumentSize);

    /**
     * Sets current memory mode.
     *
     * @param memoryMode memory mode
     */
    void setMemoryMode(MemoryMode memoryMode);

    @ParametersAreNonnullByDefault
    public enum MemoryMode {

        READ_ONLY("R", "read_only"),
        RAM_MEMORY("M", "ram"),
        DELTA_MODE("\u0394", "delta"),
        NATIVE("N", "native");

        private final String displayChar;
        private final String value;

        private MemoryMode(String displayChar, String preferencesValue) {
            this.displayChar = displayChar;
            this.value = preferencesValue;
        }

        @Nonnull
        public String getDisplayChar() {
            return displayChar;
        }

        @Nonnull
        public String getPreferencesValue() {
            return value;
        }

        @Nullable
        public static MemoryMode findByPreferencesValue(String matchValue) {
            for (MemoryMode value : values()) {
                if (value.getPreferencesValue().equals(matchValue)) {
                    return value;
                }
            }
            return null;
        }
    }
}
