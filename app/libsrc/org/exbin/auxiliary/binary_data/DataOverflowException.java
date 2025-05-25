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

import javax.annotation.Nullable;

/**
 * Exception for overflow situation where more data is inserted/added than it is
 * allowed to handle.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DataOverflowException extends RuntimeException {

    public DataOverflowException() {
    }

    public DataOverflowException(@Nullable String message) {
        super(message);
    }

    public DataOverflowException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public DataOverflowException(@Nullable Throwable cause) {
        super(cause);
    }

    public DataOverflowException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
