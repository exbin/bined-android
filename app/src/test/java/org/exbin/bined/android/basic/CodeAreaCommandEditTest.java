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

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaTest;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.android.CodeAreaCore;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for CodeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaCommandEditTest extends CodeAreaComponentTest {

    public CodeAreaCommandEditTest() {
    }
/*
    @Test
    public void testOverwriteCodeBegin() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize;
        byte[] expectedData = new byte[expectedSize];
        expectedData[0] = (byte) 0xa0;
        sampleData.copyToArray(1, expectedData, 1, dataSize - 1);

        codeArea.setContentData(sampleData);

        emulateKeyTyped(codeArea, KeyEvent.VK_UNDEFINED, 'a');

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testInsertCodeBegin() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize + 1;
        byte[] expectedData = new byte[expectedSize];
        expectedData[0] = (byte) 0xa0;
        sampleData.copyToArray(0, expectedData, 1, dataSize);
        ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);

        codeArea.setContentData(sampleData);

        emulateKeyTyped(codeArea, KeyEvent.VK_UNDEFINED, 'a');

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testOverwriteCodeEnd() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize + 1;
        byte[] expectedData = new byte[expectedSize];
        expectedData[256] = (byte) 0xa0;
        sampleData.copyToArray(0, expectedData, 0, dataSize);

        codeArea.setContentData(sampleData);
        ((CaretCapable) codeArea).setActiveCaretPosition(256);

        emulateKeyTyped(codeArea, KeyEvent.VK_UNDEFINED, 'a');

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(256, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testInsertCodeEnd() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize + 1;
        byte[] expectedData = new byte[expectedSize];
        expectedData[256] = (byte) 0xa0;
        sampleData.copyToArray(0, expectedData, 0, dataSize);
        ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);

        codeArea.setContentData(sampleData);
        ((CaretCapable) codeArea).setActiveCaretPosition(256);

        emulateKeyTyped(codeArea, KeyEvent.VK_UNDEFINED, 'a');

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(1, caretPosition.getCodeOffset());
        Assert.assertEquals(256, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testDeleteKeyEmpty() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);

        emulateKeyPressed(codeArea, KeyEvent.VK_UNDEFINED, KeyEvent.VK_DELETE);

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
        Assert.assertEquals(0, codeArea.getDataSize());
    }

    @Test
    public void testDeleteKeyBegin() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize - 1;
        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(1, expectedData, 0, dataSize - 1);

        codeArea.setContentData(sampleData);

        emulateKeyPressed(codeArea, KeyEvent.VK_UNDEFINED, KeyEvent.VK_DELETE);

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testDeleteKeyEnd() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize;
        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(0, expectedData, 0, dataSize);
        ((CaretCapable) codeArea).setActiveCaretPosition(256);

        emulateKeyPressed(codeArea, KeyEvent.VK_UNDEFINED, KeyEvent.VK_DELETE);

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(256, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testBackspaceKeyEmpty() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);

        emulateKeyPressed(codeArea, KeyEvent.VK_UNDEFINED, KeyEvent.VK_BACK_SPACE);

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
        Assert.assertEquals(0, codeArea.getDataSize());
    }

    @Test
    public void testBackspaceKeyBegin() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize;
        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(0, expectedData, 0, dataSize);

        codeArea.setContentData(sampleData);

        emulateKeyPressed(codeArea, KeyEvent.VK_UNDEFINED, KeyEvent.VK_BACK_SPACE);

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(0, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    @Test
    public void testBackspaceKeyEnd() {
        CodeAreaCore codeArea = createCodeArea();
        JFrame frame = new JFrame();
        frame.add(codeArea);
        frame.setVisible(true);
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        codeArea.setContentData(sampleData);
        int dataSize = (int) sampleData.getDataSize();
        int expectedSize = dataSize - 1;
        byte[] expectedData = new byte[expectedSize];
        sampleData.copyToArray(0, expectedData, 0, dataSize - 1);
        ((CaretCapable) codeArea).setActiveCaretPosition(256);

        emulateKeyPressed(codeArea, KeyEvent.VK_UNDEFINED, KeyEvent.VK_BACK_SPACE);

        CodeAreaCaretPosition caretPosition = ((CaretCapable) codeArea).getActiveCaretPosition();
        Assert.assertEquals(0, caretPosition.getCodeOffset());
        Assert.assertEquals(255, caretPosition.getDataPosition());
        checkResultData(expectedData, codeArea.getContentData());
    }

    private static void emulateKeyTyped(Component component, int keyEvent, char keyChar) {
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, keyEvent, keyChar));
    }

    private static void emulateKeyPressed(Component component, int keyCode, int keyEvent) {
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), keyCode, keyEvent, ' '));
    }

    public static void checkResultData(byte[] expectedData, BinaryData data) {
        Assert.assertEquals(expectedData.length, data.getDataSize());
        byte[] resultData = new byte[expectedData.length];
        data.copyToArray(0, resultData, 0, expectedData.length);
        Assert.assertArrayEquals(expectedData, resultData);
    } */
}
