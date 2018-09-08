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

/**
 * Basic code area set of colors.
 *
 * @version 0.2.0 2018/09/08
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaColors {

    private int foreground;
    private int background;
    private int selectionForeground;
    private int selectionBackground;
    private int selectionMirrorForeground;
    private int selectionMirrorBackground;
    private int cursor;
    private int negativeCursor;
    private int cursorMirror;
    private int negativeCursorMirror;
    private int decorationLine;
    private int stripes;

    public BasicCodeAreaColors() {
    }

    public int getForeground() {
        return foreground;
    }

    public void setForeground(int foreground) {
        this.foreground = foreground;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getSelectionForeground() {
        return selectionForeground;
    }

    public void setSelectionForeground(int selectionForeground) {
        this.selectionForeground = selectionForeground;
    }

    public int getSelectionBackground() {
        return selectionBackground;
    }

    public void setSelectionBackground(int selectionBackground) {
        this.selectionBackground = selectionBackground;
    }

    public int getSelectionMirrorForeground() {
        return selectionMirrorForeground;
    }

    public void setSelectionMirrorForeground(int selectionMirrorForeground) {
        this.selectionMirrorForeground = selectionMirrorForeground;
    }

    public int getSelectionMirrorBackground() {
        return selectionMirrorBackground;
    }

    public void setSelectionMirrorBackground(int selectionMirrorBackground) {
        this.selectionMirrorBackground = selectionMirrorBackground;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public int getNegativeCursor() {
        return negativeCursor;
    }

    public void setNegativeCursor(int negativeCursor) {
        this.negativeCursor = negativeCursor;
    }

    public int getCursorMirror() {
        return cursorMirror;
    }

    public void setCursorMirror(int cursorMirror) {
        this.cursorMirror = cursorMirror;
    }

    public int getNegativeCursorMirror() {
        return negativeCursorMirror;
    }

    public void setNegativeCursorMirror(int negativeCursorMirror) {
        this.negativeCursorMirror = negativeCursorMirror;
    }

    public int getDecorationLine() {
        return decorationLine;
    }

    public void setDecorationLine(int decorationLine) {
        this.decorationLine = decorationLine;
    }

    public int getStripes() {
        return stripes;
    }

    public void setStripes(int stripes) {
        this.stripes = stripes;
    }
}
