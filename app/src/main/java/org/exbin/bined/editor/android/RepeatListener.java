/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.editor.android;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Listener to repeat action when holding button.
 */
@NullMarked
public class RepeatListener implements OnTouchListener {

    protected final Handler handler = new Handler(Looper.getMainLooper());
    protected final int initialInterval = 500;
    protected final int repeatInterval = 100;
    protected final View.OnClickListener clickListener;
    protected @Nullable View touchedView;

    protected final Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (touchedView != null && touchedView.isEnabled()) {
                // Schedule the next repeat
                handler.postDelayed(this, repeatInterval);
                // Trigger the click action
                clickListener.onClick(touchedView);
            } else {
                stopRepeating();
            }
        }
    };

    public RepeatListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopRepeating();
                touchedView = view;
                // Trigger immediate initial click
                clickListener.onClick(view);
                // Schedule the first repeating step after the initial long-press delay
                handler.postDelayed(handlerRunnable, initialInterval);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopRepeating();
                return true;
        }
        return false;
    }

    private void stopRepeating() {
        handler.removeCallbacks(handlerRunnable);
        if (touchedView != null) {
            touchedView.setPressed(false);
            touchedView = null;
        }
    }
}
