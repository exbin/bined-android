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

import android.view.KeyEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaTest;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.editor.android.R;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for CodeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaCommandMovementTest extends CodeAreaComponentTest {

    private static final char CHAR_UNDEFINED = ' ';

    public CodeAreaCommandMovementTest() {
    }

    @Test
    public void testMoveBeginLeft() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_LEFT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(0, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveMiddleLeft() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(128);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_LEFT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(1, caretPosition.getCodeOffset());
            Assert.assertEquals(127, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveEndLeft() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(257);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_LEFT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(1, caretPosition.getCodeOffset());
            Assert.assertEquals(256, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveBeginRight() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_RIGHT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(1, caretPosition.getCodeOffset());
            Assert.assertEquals(0, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveBeginRightTwice() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_RIGHT, CHAR_UNDEFINED);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_RIGHT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(1, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveMiddleRight() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(128);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_RIGHT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(1, caretPosition.getCodeOffset());
            Assert.assertEquals(128, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveEndRight() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(257);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_RIGHT, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(257, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveBeginUp() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_UP, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(0, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveMiddleUp() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(128);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_UP, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(112, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveEndUp() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(257);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_UP, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(241, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveBeginDown() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_DOWN, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(16, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveMiddleDown() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(128);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_DOWN, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(144, caretPosition.getDataPosition());
        });
    }

    @Test
    public void testMoveEndDown() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));

            ((CaretCapable) codeArea).setActiveCaretPosition(257);
            emulateKeyPressed(codeArea, KeyEvent.KEYCODE_DPAD_DOWN, CHAR_UNDEFINED);

            CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
            Assert.assertEquals(0, caretPosition.getCodeOffset());
            Assert.assertEquals(257, caretPosition.getDataPosition());
        });
    }

    private void emulateKeyPressed(CodeAreaCore component, int keyEvent, char keyChar) {
        component.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
    }

    private static void resetCodeArea(CodeAreaCore codeArea) {
        codeArea.setContentData(null);
    }
}
