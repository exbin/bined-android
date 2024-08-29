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
package org.exbin.bined.highlight.android;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.android.basic.DefaultCodeAreaPainter;

/**
 * Hexadecimal component painter supporting search matches highlighting.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class HighlightCodeAreaPainter extends DefaultCodeAreaPainter {

    /**
     * Matches must be ordered by position.
     */
    private final List<SearchMatch> matches = new ArrayList<>();
    private int currentMatchIndex = -1;
    private int matchIndex = 0;
    private long matchPosition = -1;

    private Integer foundMatchesColor;
    private Integer currentMatchColor;

    public HighlightCodeAreaPainter(CodeAreaCore codeArea) {
        super(codeArea);

        foundMatchesColor = Color.rgb(180, 255, 180);
        currentMatchColor = Color.rgb(255, 210, 180);
    }

    @Override
    public void paintMainArea(Canvas g) {
        matchIndex = 0;
        super.paintMainArea(g);
    }

    @Nullable
    @Override
    public Integer getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        return super.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
    }

    @Nullable
    @Override
    public Integer getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        if (!matches.isEmpty() && charOnRow < getCharactersPerRow() - 1) {
            long dataPosition = rowDataPosition + byteOnRow;
            if (currentMatchIndex >= 0) {
                SearchMatch currentMatch = matches.get(currentMatchIndex);
                if (dataPosition >= currentMatch.position && dataPosition < currentMatch.position + currentMatch.length
                        && (section == BasicCodeAreaSection.TEXT_PREVIEW || charOnRow != ((currentMatch.position + currentMatch.length) - rowDataPosition) * getCharactersPerRow() - 1)) {
                    return currentMatchColor;
                }
            }

            if (matchPosition < rowDataPosition) {
                matchIndex = 0;
            }
            int lineMatchIndex = matchIndex;
            while (lineMatchIndex < matches.size()) {
                SearchMatch match = matches.get(lineMatchIndex);
                if (dataPosition >= match.position && dataPosition < match.position + match.length
                        && (section == BasicCodeAreaSection.TEXT_PREVIEW || charOnRow != ((match.position + match.length) - rowDataPosition) * getCharactersPerRow() - 1)) {
                    if (byteOnRow == 0) {
                        matchIndex = lineMatchIndex;
                        matchPosition = match.position;
                    }
                    return foundMatchesColor;
                }

                if (match.position > dataPosition) {
                    break;
                }

                if (byteOnRow == 0) {
                    matchIndex = lineMatchIndex;
                    matchPosition = match.position;
                }
                lineMatchIndex++;
            }
        }

        return super.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
    }

    @Nonnull
    public List<SearchMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<SearchMatch> matches) {
        this.matches.clear();
        this.matches.addAll(matches);
        currentMatchIndex = -1;
    }

    public void clearMatches() {
        this.matches.clear();
        currentMatchIndex = -1;
    }

    @Nullable
    public SearchMatch getCurrentMatch() {
        if (currentMatchIndex >= 0) {
            return matches.get(currentMatchIndex);
        }

        return null;
    }

    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }

    public void setCurrentMatchIndex(int currentMatchIndex) {
        this.currentMatchIndex = currentMatchIndex;
    }

    @Nonnull
    public Integer getFoundMatchesBackgroundColor() {
        return foundMatchesColor;
    }

    public void setFoundMatchesBackgroundColor(Integer foundMatchesBackgroundColor) {
        this.foundMatchesColor = foundMatchesBackgroundColor;
    }

    @Nonnull
    public Integer getCurrentMatchBackgroundColor() {
        return currentMatchColor;
    }

    public void setCurrentMatchBackgroundColor(Integer currentMatchBackgroundColor) {
        this.currentMatchColor = currentMatchBackgroundColor;
    }

    /**
     * Simple POJO class for search match.
     */
    public static class SearchMatch {

        private long position;
        private long length;

        public SearchMatch() {
        }

        public SearchMatch(long position, long length) {
            this.position = position;
            this.length = length;
        }

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }
    }
}
