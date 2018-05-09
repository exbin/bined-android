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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.android.basic.DefaultCodeAreaWorker;
import org.exbin.bined.android.capability.FontCapable;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Hexadecimal viewer/editor component.
 *
 * @version 0.2.0 2018/05/08
 * @author ExBin Project (http://exbin.org)
 */
public class CodeArea extends View implements CodeAreaControl {

    @Nullable
    private BinaryData contentData;

    @Nonnull
    private CodeAreaWorker worker;
    @Nonnull
    private CodeAreaCommandHandler commandHandler;

    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     */
    public CodeArea(Context context, AttributeSet attrs) {
        this(context, attrs, null, null);
    }

    /**
     * Creates new instance with provided command handler and worker factory
     * methods.
     *
     * @param workerFactory code area worker or null for default worker
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeArea(Context context, AttributeSet attrs, @Nullable CodeAreaWorker.CodeAreaWorkerFactory workerFactory, @Nullable CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(context, attrs);
        this.worker = workerFactory == null ? new DefaultCodeAreaWorker(this) : workerFactory.createWorker(this);
        this.commandHandler = commandHandlerFactory == null ? new DefaultCodeAreaCommandHandler(context, this) : commandHandlerFactory.createCommandHandler(this);
        init();
    }

    private void init() {
        // TODO: Use swing color instead
//        setBackgroundColor(ColorUtils.WHITE);
        setFocusable(true);
//        setFocusTraversalKeysEnabled(false);
    }

    @Nonnull
    public CodeAreaWorker getWorker() {
        return worker;
    }

    public void setWorker(@Nonnull CodeAreaWorker worker) {
        if (worker == null) throw new NullPointerException("Worker cannot be null");

        this.worker = worker;
    }

    @Nonnull
    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(@Nonnull CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    protected void onDraw(@Nullable Canvas g) {
        super.onDraw(g);
        if (g == null) {
            return;
        }

        if (!worker.isInitialized()) {
            ((FontCapable) worker).setFont(Font.fromPaint(new Paint()));
        }
        worker.paintComponent(g);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // TODO reset layout instead
        resetPainter();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        commandHandler.keyTyped(keyEvent);
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        commandHandler.keyPressed(keyEvent);
        return super.onKeyUp(keyCode, keyEvent);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @android.support.annotation.Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        repaint();
    }

    public void repaint() {
        invalidate();
    }

    @Override
    public void copy() {
        commandHandler.copy();
    }

    @Override
    public void copyAsCode() {
        commandHandler.copyAsCode();
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
    public void pasteFromCode() {
        commandHandler.pasteFromCode();
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
        return ((SelectionCapable) worker).hasSelection();
    }

    @Nullable
    public BinaryData getContentData() {
        return contentData;
    }

    public void setContentData(@Nullable BinaryData contentData) {
        this.contentData = contentData;
        notifyDataChanged();
        repaint();
    }

    public long getDataSize() {
        return contentData == null ? 0 : contentData.getDataSize();
    }

    /**
     * Notifies component, that internal data was changed.
     */
    public void notifyDataChanged() {
        resetPainter();

        for (DataChangedListener listener : dataChangedListeners) {
            listener.dataChanged();
        }
    }

    public void addDataChangedListener(@Nonnull DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    public void removeDataChangedListener(@Nonnull DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    public void resetPainter() {
        worker.reset();
    }
}
