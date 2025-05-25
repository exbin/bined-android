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

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exception for data reading or writing when range of data is not present.
 * <p>
 * Exception can optionally provide information about range of data which are
 * not present, but it doesn't have to be full segment.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DataNotPresentException extends RuntimeException {

    @Nullable
    protected final DataRange dataRange;

    public DataNotPresentException() {
        dataRange = null;
    }

    public DataNotPresentException(@Nullable DataRange dataRange) {
        this.dataRange = dataRange;
    }

    public DataNotPresentException(@Nullable String message) {
        super(message);
        dataRange = null;
    }

    public DataNotPresentException(@Nullable String message, @Nullable DataRange dataRange) {
        super(message);
        this.dataRange = dataRange;
    }

    public DataNotPresentException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
        dataRange = null;
    }

    public DataNotPresentException(@Nullable Throwable cause) {
        super(cause);
        dataRange = null;
    }

    public DataNotPresentException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        dataRange = null;
    }

    @Nonnull
    public Optional<DataRange> getDataRange() {
        return Optional.ofNullable(dataRange);
    }
}
