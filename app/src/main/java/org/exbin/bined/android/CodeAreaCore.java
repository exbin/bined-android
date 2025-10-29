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
package org.exbin.bined.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.RelativeLayout;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EmptyBinaryData;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.basic.SelectingMode;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Binary viewer/editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public abstract class CodeAreaCore extends ViewGroup implements CodeAreaControl {

    @Nonnull
    protected BinaryData contentData = EmptyBinaryData.getInstance();

    @Nonnull
    protected final PrimaryView primaryView;

    @Nonnull
    protected CodeAreaCommandHandler commandHandler;

    protected final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    protected float touchPositionX;
    protected float touchPositionY;

    /**
     * Creates new instance with default command handler and painter.
     */
    public CodeAreaCore(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeAreaCore(Context context, AttributeSet attrs, @Nullable CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(context, attrs);
        this.commandHandler = commandHandlerFactory == null ? new DefaultCodeAreaCommandHandler(context, this) : createCommandHandler(commandHandlerFactory);
        init();

        primaryView = new PrimaryView(context, attrs);
        RelativeLayout.LayoutParams wrapLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        addView(primaryView, wrapLayout);
    }

    private void init() {
        setContentDescription("Code area");
        setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setSystemUiVisibility(getSystemUiVisibility() | View.FOCUSABLE);
        }
        // TODO: Use swing color instead
//        setBackgroundColor(ColorUtils.WHITE);
//        setFocusTraversalKeysEnabled(false);
    }

    @Nonnull
    private CodeAreaCommandHandler createCommandHandler(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        return commandHandlerFactory.createCommandHandler(this);
    }

    @Nonnull
    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void copy() {
        commandHandler.copy();
    }

    @Override
    public void cut() {
        commandHandler.cut();
    }

    @Override
    public void paste() {
        commandHandler.paste();
    }

    @Override
    public void delete() {
        commandHandler.delete();
    }

    @Override
    public void selectAll() {
        commandHandler.selectAll();
    }

    @Override
    public void clearSelection() {
        commandHandler.clearSelection();
    }

    @Override
    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    @Override
    public boolean hasSelection() {
        if (this instanceof SelectionCapable) {
            return ((SelectionCapable) this).hasSelection();
        }

        return false;
    }

    public boolean isEditable() {
        return false;
    }

    @Nonnull
    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(@Nullable BinaryData contentData) {
        this.contentData = contentData == null ? EmptyBinaryData.getInstance() : contentData;
        notifyDataChanged();
        repaint();
    }

    @Override
    public long getDataSize() {
        return contentData.getDataSize();
    }

    /**
     * Notifies component, that the internal data was changed.
     */
    public void notifyDataChanged() {
        for (DataChangedListener listener : dataChangedListeners) {
            listener.dataChanged();
        }
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public void repaint() {
        Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                invalidate();
                primaryView.invalidate();
            });
        } else {
            invalidate();
            primaryView.invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            primaryView.layout(left, top, right, bottom);
            resetPainter();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        commandHandler.moveCaret((int) event.getX(), (int) event.getY(), SelectingMode.NONE);
        ((ScrollingCapable) this).revealCursor();

        return true;
    }

    public abstract void resetPainter();

    public abstract void updateLayout();

    public abstract void paintView(Canvas g);

    public void setTouchPosition(float touchPositionX, float touchPositionY) {
        this.touchPositionX = touchPositionX;
        this.touchPositionY = touchPositionY;
    }

    public float getTouchPositionX() {
        return touchPositionX;
    }

    public float getTouchPositionY() {
        return touchPositionY;
    }

    @ParametersAreNonnullByDefault
    private class PrimaryView extends View {

        public PrimaryView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setContentDescription("Code area scroll view");
            // setFocusTraversalKeysEnabled(false);
        }

        @Override
        protected void onDraw(@Nullable Canvas g) {
            if (g == null) {
                return;
            }

            super.onDraw(g);
            paintView(g);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            // TODO reset layout instead
            resetPainter();
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
            commandHandler.keyTyped(keyEvent.getUnicodeChar(), keyEvent);
            return super.onKeyDown(keyCode, keyEvent);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
            commandHandler.keyPressed(keyEvent);
            return super.onKeyUp(keyCode, keyEvent);
        }

        @Override
        protected void onFocusChanged(boolean gainFocus, int direction, @androidx.annotation.Nullable Rect previouslyFocusedRect) {
            super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
            if (gainFocus) {
                repaint();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) {
                performClick();
            }

            CodeAreaCore.this.onTouchEvent(event);
            return false;
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }
    }

    @Nonnull
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new CodeAreaInputConnection(this, true);
    }
}
