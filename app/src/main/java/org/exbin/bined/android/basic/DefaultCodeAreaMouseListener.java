/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.android.basic;

import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import org.exbin.bined.android.CodeAreaCommandHandler.SelectingMode;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.capability.ScrollingCapable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Component mouse listener for code area.
 *
 * @version 0.2.0 2021/12/01
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaMouseListener implements View.OnTouchListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    private final CodeAreaCore codeArea;
    private final DefaultCodeAreaScrollPane view;

    private final int defaultCursor = 0;
    private final int textCursor = 1;
    private int currentCursor;
    private boolean mouseDown = false;

    public DefaultCodeAreaMouseListener(CodeAreaCore codeArea, DefaultCodeAreaScrollPane view) {
        this.codeArea = codeArea;
        this.view = view;
        currentCursor = 0;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) {
            performClick();
        }

        if (codeArea.isEnabled() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
            moveCaret(motionEvent);
            return true;
        }

        return false;
    }

    private void performClick() {
    }

    private void moveCaret(MotionEvent me) {
        SelectingMode selecting = SelectingMode.NONE; //SELECTING(me.getActionButton()getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0 ? SelectingMode.SELECTING : SelectingMode.NONE;
        codeArea.getCommandHandler().moveCaret((int) computeRelativeX(me), (int) computeRelativeY(me), selecting);
        ((ScrollingCapable) codeArea).revealCursor();
    }

    private float computeRelativeX(MotionEvent me) {
        return me.getX() - view.getScrollX() + view.getX();
//        boolean isDataView = true; // me.getSource() != codeArea;
//        return isDataView ? me.getX() - view.getX() : me.getX();
    }

    private float computeRelativeY(MotionEvent me) {
        return me.getY() - view.getScrollY() + view.getY();
//        boolean isDataView = true; // me.getSource() != codeArea;
//        return isDataView ? me.getY() - view.getY() : me.getY();
    }
}
