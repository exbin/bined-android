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

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.android.CodeArea;

/**
 * Default implementation of code area caret.
 *
 * @version 0.2.0 2018/05/09
 * @author ExBin Project (http://exbin.org)
 */
public class DefaultCodeAreaCaret implements CodeAreaCaret {

    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    @Nonnull
    private final CodeArea codeArea;
    private final CodeAreaCaretPosition caretPosition = new CodeAreaCaretPosition();

    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;

    @Nonnull
    private CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    public DefaultCodeAreaCaret(@Nonnull CodeArea codeArea) {
        CodeAreaUtils.requireNonNull(codeArea);

        this.codeArea = codeArea;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public static int getCursorThickness(@Nonnull CursorShape cursorShape, int characterWidth, int lineHeight) {
        switch (cursorShape) {
            case INSERT:
                return DOUBLE_CURSOR_WIDTH;
            case OVERWRITE:
            case MIRROR:
                return characterWidth;
        }

        return -1;
    }

    @Nonnull
    @Override
    public CaretPosition getCaretPosition() {
        return caretPosition;
    }

    public void resetBlink() {
        if (blinkTimer != null) {
            cursorVisible = true;
            resetTimer();
        }
    }

    public synchronized void resetTimer() {
        blinkTimer.cancel();
        blinkTimer = new Timer();
        blinkTimer.scheduleAtFixedRate(new Blink(), blinkRate, blinkRate);
    }

    private void notifyCaredChanged() {
        // TODO limit to cursor repaint
        ((CaretCapable) codeArea.getWorker()).notifyCaretChanged();
    }

    @Override
    public void setCaretPosition(@Nullable CaretPosition caretPosition) {
        if (caretPosition != null) {
            this.caretPosition.setPosition(caretPosition);
        } else {
            this.caretPosition.clear();
        }
        resetBlink();
    }

    @Override
    public void setCaretPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(0);
        resetBlink();
    }

    @Override
    public void setCaretPosition(long dataPosition, int codeOffset) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public void setCaretPosition(long dataPosition, int codeOffset, int section) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
        caretPosition.setSection(section);
        resetBlink();
    }

    public long getDataPosition() {
        return caretPosition.getDataPosition();
    }

    public void setDataPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        resetBlink();
    }

    public int getCodeOffset() {
        return caretPosition.getCodeOffset();
    }

    public void setCodeOffset(int codeOffset) {
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public int getSection() {
        return caretPosition.getSection();
    }

    public void setSection(int section) {
        caretPosition.setSection(section);
        resetBlink();
    }

    public int getBlinkRate() {
        return blinkRate;
    }

    public void setBlinkRate(int blinkRate) {
        privateSetBlinkRate(blinkRate);
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    @Nonnull
    public CursorRenderingMode getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(@Nonnull CursorRenderingMode renderingMode) {
        CodeAreaUtils.requireNonNull(renderingMode);

        this.renderingMode = renderingMode;
        notifyCaredChanged();
    }

    private synchronized void privateSetBlinkRate(int blinkRate) {
        if (blinkRate < 0) {
            throw new IllegalArgumentException("Blink rate cannot be negative");
        }

        this.blinkRate = blinkRate;
        if (blinkTimer != null) {
            if (blinkRate == 0) {
                blinkTimer.cancel();
                blinkTimer = null;
                cursorVisible = true;
                notifyCaredChanged();
            } else {
                blinkTimer.cancel();
                blinkTimer = new Timer();
                blinkTimer.scheduleAtFixedRate(new Blink(), blinkRate, blinkRate);
            }
        } else if (blinkRate > 0) {
            blinkTimer = new Timer();
            blinkTimer.scheduleAtFixedRate(new Blink(), blinkRate, blinkRate);
        }
    }

    private class Blink extends TimerTask {

        @Override
        public void run() {
            cursorVisible = !cursorVisible;
            notifyCaredChanged();
        }
    }

    /**
     * Enumeration of supported cursor shapes.
     */
    public enum CursorShape {
        INSERT, OVERWRITE, MIRROR
    }

    /**
     * Method for rendering cursor into CodeArea component.
     */
    public enum CursorRenderingMode {
        /**
         * Cursor is just painted.
         */
        PAINT,
        /**
         * Cursor is painted using pixels inversion.
         */
        XOR,
        /**
         * Underlying character is painted using negative color to cursor
         * cursor.
         */
        NEGATIVE
    }
}
