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
 * Binary search service.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinarySearchService {

    void performFind(SearchParameters dialogSearchParameters, SearchStatusListener searchStatusListener);

    void setMatchPosition(int matchPosition);

    void performFindAgain(SearchStatusListener searchStatusListener);

    void performReplace(SearchParameters searchParameters, ReplaceParameters replaceParameters);

    @Nonnull
    SearchParameters getLastSearchParameters();

    void clearMatches();

    @ParametersAreNonnullByDefault
    public interface SearchStatusListener {

        void setStatus(FoundMatches foundMatches, SearchParameters.MatchMode matchMode);

        void clearStatus();
    }

    public static class FoundMatches {

        private int matchesCount;
        private int matchPosition;

        public FoundMatches() {
            matchesCount = 0;
            matchPosition = -1;
        }

        public FoundMatches(int matchesCount, int matchPosition) {
            if (matchPosition >= matchesCount) {
                throw new IllegalStateException("Match position is out of range");
            }

            this.matchesCount = matchesCount;
            this.matchPosition = matchPosition;
        }

        public int getMatchesCount() {
            return matchesCount;
        }

        public int getMatchPosition() {
            return matchPosition;
        }

        public void setMatchesCount(int matchesCount) {
            this.matchesCount = matchesCount;
        }

        public void setMatchPosition(int matchPosition) {
            this.matchPosition = matchPosition;
        }

        public void next() {
            if (matchPosition == matchesCount - 1) {
                throw new IllegalStateException("Cannot find next on last match");
            }

            matchPosition++;
        }

        public void prev() {
            if (matchPosition == 0) {
                throw new IllegalStateException("Cannot find previous on first match");
            }

            matchPosition--;
        }
    }
}
