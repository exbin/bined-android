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
package org.exbin.bined.operation.android;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.SelectionRange;

/**
 * Code area state for operation purposes.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
@Immutable
public class CodeAreaState {

    private final CodeAreaCaretPosition caretPosition;
    private final SelectionRange selection;

    public CodeAreaState(CodeAreaCaretPosition caretPosition, SelectionRange selection) {
        this.caretPosition = caretPosition;
        this.selection = selection;
    }

    @Nonnull
    public CodeAreaCaretPosition getCaretPosition() {
        return caretPosition;
    }

    @Nonnull
    public SelectionRange getSelection() {
        return selection;
    }
}
