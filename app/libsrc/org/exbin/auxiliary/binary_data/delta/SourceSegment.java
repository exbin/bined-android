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
package org.exbin.auxiliary.binary_data.delta;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Data segment pointing to source data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class SourceSegment extends DataSegment {

    @Nonnull
    private final DataSource source;
    private long startPosition;
    private long length;

    public SourceSegment(DataSource source, long startPosition, long length) {
        this.source = source;
        this.startPosition = startPosition;
        this.length = length;
    }

    @Nonnull
    public DataSource getSource() {
        return source;
    }

    @Override
    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte getByte(long position) {
        try {
            return source.getByte(position);
        } catch (IOException ex) {
            throw new RuntimeException("Error while processing data source", ex);
        }
    }

    @Nonnull
    @Override
    public DataSegment copy() {
        return new SourceSegment(source, startPosition, length);
    }
}
