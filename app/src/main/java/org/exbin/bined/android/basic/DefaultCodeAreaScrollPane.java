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
package org.exbin.bined.android.basic;

import android.content.Context;
import android.util.AttributeSet;

import com.examples.customtouch.widget.TwoDimensionScrollView;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default scroll pane for binary component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaScrollPane extends TwoDimensionScrollView {

    protected volatile boolean scrollingByUser = false;
    protected volatile boolean scrollingUpdate = false;

    public DefaultCodeAreaScrollPane(Context context) {
        super(context);
        init();
    }

    public DefaultCodeAreaScrollPane(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DefaultCodeAreaScrollPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setScrollContainer(true);
//        setHorizontalScrollBarEnabled(true);
//        setVerticalScrollBarEnabled(true);
//        setHorizontalFadingEdgeEnabled(false);
    }

    public void updateScrollBars(int verticalScrollValue, int horizontalScrollValue) {
        scrollingUpdate = true;
        scrollTo(horizontalScrollValue, verticalScrollValue);
//        awakenScrollBars();
        scrollingUpdate = false;
    }
}
