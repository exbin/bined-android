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

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.annotation.Nonnull;
import javax.swing.JScrollPane;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.android.CodeArea;
import org.exbin.bined.android.CodeAreaCommandHandler;

/**
 * Code Area component mouse listener.
 *
 * @version 0.2.0 2018/01/01
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

    public static final int MOUSE_SCROLL_LINES = 3;

    private final CodeArea codeArea;
    private final JScrollPane view;

    private final Cursor defaultCursor = Cursor.getDefaultCursor();
    private final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    private Cursor currentCursor;
    private boolean mouseDown = false;

    public DefaultCodeAreaMouseListener(@Nonnull CodeArea codeArea, @Nonnull JScrollPane view) {
        this.codeArea = codeArea;
        this.view = view;
        currentCursor = codeArea.getCursor();
    }

    @Override
    public void mousePressed(@Nonnull MouseEvent me) {
        codeArea.requestFocus();
        if (codeArea.isEnabled() && me.getButton() == MouseEvent.BUTTON1) {
            moveCaret(me);
            mouseDown = true;
        }
    }

    private void moveCaret(@Nonnull MouseEvent me) {
        boolean selecting = (me.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0;
        codeArea.getCommandHandler().moveCaret(computeRelativeX(me), computeRelativeY(me), selecting);
        ((CaretCapable) codeArea.getWorker()).revealCursor();
    }

    @Override
    public void mouseReleased(@Nonnull MouseEvent me) {
        mouseDown = false;
    }

    @Override
    public void mouseExited(@Nonnull MouseEvent e) {
        currentCursor = defaultCursor;
        codeArea.setCursor(defaultCursor);
    }

    @Override
    public void mouseEntered(@Nonnull MouseEvent e) {
        updateMouseCursor(e);
    }

    @Override
    public void mouseMoved(@Nonnull MouseEvent e) {
        updateMouseCursor(e);
    }

    private void updateMouseCursor(@Nonnull MouseEvent me) {
        int cursorShape = ((CaretCapable) codeArea.getWorker()).getMouseCursorShape(computeRelativeX(me), computeRelativeY(me));

        // Reuse current cursor if unchanged
        Cursor newCursor = cursorShape == 0 ? defaultCursor : textCursor;
        if (newCursor != currentCursor) {
            currentCursor = newCursor;
            codeArea.setCursor(newCursor);
        }
    }

    @Override
    public void mouseDragged(@Nonnull MouseEvent me) {
        updateMouseCursor(me);
        if (codeArea.isEnabled() && mouseDown) {
            codeArea.getCommandHandler().moveCaret(computeRelativeX(me), computeRelativeY(me), true);
            ((CaretCapable) codeArea.getWorker()).revealCursor();
        }
    }

    private int computeRelativeX(@Nonnull MouseEvent me) {
        boolean isDataView = me.getSource() != codeArea;
        return isDataView ? me.getX() + view.getX() : me.getX();
    }

    private int computeRelativeY(@Nonnull MouseEvent me) {
        boolean isDataView = me.getSource() != codeArea;
        return isDataView ? me.getY() + view.getY() : me.getY();
    }

    @Override
    public void mouseWheelMoved(@Nonnull MouseWheelEvent e) {
        if (!codeArea.isEnabled() || e.getWheelRotation() == 0) {
            return;
        }

        codeArea.getCommandHandler().wheelScroll(e.getWheelRotation() > 0 ? MOUSE_SCROLL_LINES : -MOUSE_SCROLL_LINES, e.isShiftDown() ? CodeAreaCommandHandler.ScrollbarOrientation.VERTICAL : CodeAreaCommandHandler.ScrollbarOrientation.HORIZONTAL);
    }
}
