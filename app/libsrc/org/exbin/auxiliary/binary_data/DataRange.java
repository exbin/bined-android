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
package org.exbin.auxiliary.binary_data;

import javax.annotation.concurrent.Immutable;

/**
 * Simple representation of data range.
 *
 * @author ExBin Project (https://exbin.org)
 */
@Immutable
public class DataRange {

    protected final long startPosition;
    protected final long endPosition;

    public DataRange(long startPosition, long endPosition) {
        if (endPosition > startPosition) {
            throw new IllegalStateException("Invalid data range");
        }

        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public long getLength() {
        return endPosition - startPosition + 1;
    }
}
