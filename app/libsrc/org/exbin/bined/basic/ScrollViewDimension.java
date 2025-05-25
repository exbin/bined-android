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
package org.exbin.bined.basic;

/**
 * Scrolling view dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class ScrollViewDimension {

    protected int dataViewWidth;
    protected int dataViewHeight;
    protected int width;
    protected int height;

    public ScrollViewDimension() {
    }

    public void setDimension(int dataViewWidth, int dataViewHeight, int width, int height) {
        this.dataViewWidth = dataViewWidth;
        this.dataViewHeight = dataViewHeight;
        this.width = width;
        this.height = height;
    }

    public int getDataViewWidth() {
        return dataViewWidth;
    }

    public int getDataViewHeight() {
        return dataViewHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
