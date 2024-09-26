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
package org.exbin.bined.editor.android.search;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Parameters for action to replace for occurrences of text or data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ReplaceParameters {

    private SearchCondition condition = new SearchCondition();
    private boolean performReplace;
    private boolean replaceAll;

    public ReplaceParameters() {
    }

    @Nonnull
    public SearchCondition getCondition() {
        return condition;
    }

    public void setCondition(SearchCondition condition) {
        this.condition = condition;
    }

    public void setFromParameters(ReplaceParameters replaceParameters) {
        condition = replaceParameters.getCondition();
    }

    public boolean isPerformReplace() {
        return performReplace;
    }

    public void setPerformReplace(boolean performReplace) {
        this.performReplace = performReplace;
    }

    public boolean isReplaceAll() {
        return replaceAll;
    }

    public void setReplaceAll(boolean replaceAll) {
        this.replaceAll = replaceAll;
    }
}
