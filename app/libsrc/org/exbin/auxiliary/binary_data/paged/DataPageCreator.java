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
package org.exbin.auxiliary.binary_data.paged;

import javax.annotation.Nonnull;
import org.exbin.auxiliary.binary_data.EditableBinaryData;

/**
 * Data page creator.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface DataPageCreator {

    /**
     * Creates new data page with specified size.
     *
     * @param dataSize data size
     * @return data page
     */
    @Nonnull
    EditableBinaryData createPage(int dataSize);
}
