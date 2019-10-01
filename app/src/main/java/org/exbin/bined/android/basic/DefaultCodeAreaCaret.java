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

import org.exbin.bined.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.capability.CaretCapable;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default implementation of code area caret.
 *
 * @version 0.2.0 2018/09/07
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCaret implements CodeAreaCaret {

    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private static final int DEFAULT_BLINK_RATE = 450;

    @Nonnull
    private final CodeArea codeArea;
    private final DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();

    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private boolean cursorVisible = true;

    @Nonnull
    private CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    public DefaultCodeAreaCaret(CodeArea codeArea) {
        this.codeArea = CodeAreaUtils.requireNonNull(codeArea);
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public static int getCursorThickness(CursorShape cursorShape, int characterWidth, int lineHeight) {
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
    public CodeAreaCaretPosition getCaretPosition() {
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
        ((CaretCapable) codeArea).notifyCaretChanged();
    }

    @Override
    public void setCaretPosition(@Nullable CodeAreaCaretPosition caretPosition) {
        if (caretPosition != null) {
            this.caretPosition.setPosition(caretPosition);
        } else {
            this.caretPosition.reset();
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

    public void setCaretPosition(long dataPosition, int codeOffset, CodeAreaSection section) {
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

    public CodeAreaSection getSection() {
        CodeAreaSection section = caretPosition.getSection();
        return section == null ? BasicCodeAreaSection.CODE_MATRIX : section;
    }

    public void setSection(CodeAreaSection section) {
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

    public void setRenderingMode(CursorRenderingMode renderingMode) {
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
