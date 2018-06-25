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
package org.exbin.bined.android;

import android.graphics.Canvas;
import android.view.MotionEvent;

import javax.annotation.Nonnull;
import org.exbin.bined.CaretPosition;

/**
 * Hexadecimal editor worker interface.
 *
 * @version 0.2.0 2018/05/03
 * @author ExBin Project (http://exbin.org)
 */
public interface CodeAreaWorker {

    /**
     * Returns code area used by this worker.
     *
     * @return code area
     */
    @Nonnull
    CodeArea getCodeArea();

    /**
     * Returns true if painter was initialized.
     *
     * @return true if initialized
     */
    boolean isInitialized();

    /**
     * Paints the main component.
     *
     * @param g Canvas
     */
    void paintComponent(@Nonnull Canvas g);

    /**
     * Rebuilds colors after UIManager change.
     */
    void resetColors();

    /**
     * Resets painter state for new painting.
     */
    void reset();

    /**
     * Requests update of the component layout.
     *
     * Notifies worker, that change of parameters will affect layout and it
     * should be recomputed and updated if necessary.
     */
    void updateLayout();

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    @Nonnull
    CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction);

    boolean onTouchEvent(MotionEvent event);

    interface CodeAreaWorkerFactory {

        @Nonnull
        CodeAreaWorker createWorker(@Nonnull CodeArea codeArea);
    }
}
