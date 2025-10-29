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

import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Binary viewer/editor component input connection.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaInputConnection extends BaseInputConnection {

    protected final CodeAreaCore codeArea;

    public CodeAreaInputConnection(CodeAreaCore codeArea, boolean fullEditor) {
        super(codeArea, fullEditor);
        this.codeArea = codeArea;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        for (int i = 0; i < text.length(); i++) {
            codeArea.getCommandHandler().keyTyped(text.charAt(i), new KeyEvent(KeyEvent.ACTION_DOWN, text.charAt(i)));
        }
        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        codeArea.dispatchKeyEvent(keyEvent);
        return true;
    }
}
