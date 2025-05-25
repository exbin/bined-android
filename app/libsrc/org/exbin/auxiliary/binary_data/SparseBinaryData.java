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

/**
 * Interface for binary data with missing segments / segments where no data are
 * present.
 * <p>
 * Expected use is to either check presence per position or segments or to read
 * data and handle presence only when {@link DataNotPresentException} is thrown.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface SparseBinaryData {

    /**
     * Returns true if data are present on given position.
     *
     * @param position position
     * @return true if present
     */
    boolean isDataPresent(long position);

    /**
     * Returns closest next subsequent data position where data presence changes
     * or any position closer, but greater than given position.
     * <p>
     * Basic implementation can return position + 1 which forces reading and
     * handling of {@link DataNotPresentException}.
     *
     * @param position position to check from
     * @return next position to check for presence
     */
    long closestNextPossiblePresenceChange(long position);

    /**
     * Returns closest previous preceding data position where data presence
     * changes or any position closer, but lesser than given position.
     * <p>
     * Basic implementation can return position - 1 which forces reading and
     * handling of {@link DataNotPresentException}.
     *
     * @param position position to check from
     * @return previous position to check for presence
     */
    long closestPreviousPossiblePresenceChange(long position);
}
