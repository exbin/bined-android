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

import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.DefaultCodeAreaCaretPosition;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default implementation of code area caret.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DefaultCodeAreaCaret implements CodeAreaCaret {

    protected static final int DOUBLE_CURSOR_WIDTH = 2;
    protected static final int DEFAULT_BLINK_RATE = 450;

    @Nonnull
    protected final CaretChangeListener changeListener;
    protected final DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();

    protected int blinkRate = 0;
    protected Timer blinkTimer = null;
    protected boolean cursorVisible = true;

    @Nonnull
    protected CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    public DefaultCodeAreaCaret(CaretChangeListener changeListener) {
        CodeAreaUtils.requireNonNull(changeListener, "Change listener cannot be null");

        this.changeListener = changeListener;
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
        changeListener.notifyCaretChanged();
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

    @Nonnull
    @Override
    public CodeAreaSection getSection() {
        return caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
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

    /**
     * Interface for changes listener.
     */
    public interface CaretChangeListener {

        /**
         * Caret changed.
         */
        void notifyCaretChanged();
    }
}
