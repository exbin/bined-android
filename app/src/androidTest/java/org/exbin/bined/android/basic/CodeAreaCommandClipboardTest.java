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

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaTest;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.editor.android.R;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Tests clipboard actions for CodeArea command component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaCommandClipboardTest extends CodeAreaComponentTest {

    public CodeAreaCommandClipboardTest() {
    }

    @Test
    public void testDeleteAll() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            codeArea.setContentData(CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES));
            codeArea.selectAll();
            codeArea.delete();

            Assert.assertEquals(0, codeArea.getDataSize());
        });
    }

/*    @Test
    public void testCopySelection() {
        CodeAreaCore codeArea = createCodeArea();
        EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
        byte[] expectedData = new byte[2];
        sampleData.copyToArray(5, expectedData, 0, 2);

        codeArea.setContentData(sampleData);
        ((SelectionCapable) codeArea).setSelection(5, 7);
        codeArea.copy();

        try {
            String text = (String) CodeAreaAndroidUtils.getClipboard().getData(DataFlavor.stringFlavor);

            Assert.assertArrayEquals(expectedData, text.getBytes("UTF-8"));
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(CodeAreaCommandClipboardTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail();
        }
    } */

    @Test
    public void testCopyPasteAllInOverwriteMode() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            codeArea.setContentData(sampleData);
            ((EditModeCapable) codeArea).setEditOperation(EditOperation.OVERWRITE);
            long dataSize = sampleData.getDataSize();
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            codeArea.paste();

            Assert.assertEquals(dataSize, codeArea.getDataSize());
        });
    }

    @Test
    public void testCopyPasteAllInInsertMode() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            codeArea.setContentData(sampleData);
            ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
            long dataSize = sampleData.getDataSize();
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            codeArea.paste();

            Assert.assertEquals(dataSize * 2, codeArea.getDataSize());
        });
    }

    @Test
    public void testCopyPasteAllInInplaceMode() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            ((EditModeCapable) codeArea).setEditMode(EditMode.INPLACE);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            codeArea.setContentData(sampleData);
            long dataSize = sampleData.getDataSize();
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            codeArea.paste();

            Assert.assertEquals(dataSize, codeArea.getDataSize());
        });
    }

    @Test
    public void testCopyPasteInOverwriteAtTheEnd() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            codeArea.setContentData(sampleData);
            ((EditModeCapable) codeArea).setEditOperation(EditOperation.OVERWRITE);
            long dataSize = sampleData.getDataSize();
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(dataSize);
            codeArea.paste();

            Assert.assertEquals(dataSize * 2, codeArea.getDataSize());
        });
    }

    @Test
    public void testCopyPasteInInsertAtTheEnd() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            codeArea.setContentData(sampleData);
            ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
            long dataSize = sampleData.getDataSize();
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(dataSize);
            codeArea.paste();

            Assert.assertEquals(dataSize * 2, codeArea.getDataSize());
        });
    }

    @Test
    public void testCopyPasteInInplaceAtTheEnd() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            ((EditModeCapable) codeArea).setEditMode(EditMode.INPLACE);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            codeArea.setContentData(sampleData);
            long dataSize = sampleData.getDataSize();
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(dataSize);
            codeArea.paste();

            Assert.assertEquals(dataSize, codeArea.getDataSize());
        });
    }

    @Test
    public void testCopyPasteInOverwriteWithOverflow() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            int dataSize = (int) sampleData.getDataSize();
            int expectedSize = (int) (dataSize / 2 + dataSize);

            byte[] expectedData = new byte[expectedSize];
            sampleData.copyToArray(0, expectedData, 0, dataSize / 2);
            sampleData.copyToArray(0, expectedData, dataSize / 2, dataSize);

            codeArea.setContentData(sampleData);
            ((EditModeCapable) codeArea).setEditOperation(EditOperation.OVERWRITE);
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(dataSize / 2);
            codeArea.paste();

            Assert.assertEquals(expectedSize, codeArea.getDataSize());
            checkResultData(expectedData, codeArea.getContentData());
        });
    }

    @Test
    public void testCopyPasteInInsertWithOverflow() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            int dataSize = (int) sampleData.getDataSize();
            int expectedSize = dataSize * 2;

            byte[] expectedData = new byte[expectedSize];
            sampleData.copyToArray(0, expectedData, 0, dataSize / 2);
            sampleData.copyToArray(0, expectedData, dataSize / 2, dataSize);
            sampleData.copyToArray(dataSize / 2, expectedData, dataSize + dataSize / 2, dataSize / 2);

            codeArea.setContentData(sampleData);
            ((EditModeCapable) codeArea).setEditOperation(EditOperation.INSERT);
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(dataSize / 2);
            codeArea.paste();

            Assert.assertEquals(expectedSize, codeArea.getDataSize());
            checkResultData(expectedData, codeArea.getContentData());
        });
    }

    @Test
    public void testCopyPasteInInplaceWithOverflow() {
        rule.getScenario().onActivity(activity -> {
            CodeAreaCore codeArea = activity.findViewById(R.id.codeArea);
            resetCodeArea(codeArea);
            ((EditModeCapable) codeArea).setEditMode(EditMode.INPLACE);
            EditableBinaryData sampleData = CodeAreaTest.getSampleData(CodeAreaTest.SAMPLE_ALLBYTES);
            int dataSize = (int) sampleData.getDataSize();
            int expectedSize = dataSize;
            byte[] expectedData = new byte[expectedSize];
            sampleData.copyToArray(0, expectedData, 0, dataSize / 2);
            sampleData.copyToArray(0, expectedData, dataSize / 2, dataSize / 2);

            codeArea.setContentData(sampleData);
            codeArea.selectAll();
            codeArea.copy();
            codeArea.clearSelection();
            ((CaretCapable) codeArea).setActiveCaretPosition(dataSize / 2);
            codeArea.paste();

            Assert.assertEquals(expectedSize, codeArea.getDataSize());
            checkResultData(expectedData, codeArea.getContentData());
        });
    }

    public static void checkResultData(byte[] expectedData, BinaryData data) {
        Assert.assertEquals(expectedData.length, data.getDataSize());
        byte[] resultData = new byte[expectedData.length];
        data.copyToArray(0, resultData, 0, expectedData.length);
        Assert.assertArrayEquals(expectedData, resultData);
    }

    private static void resetCodeArea(CodeAreaCore codeArea) {
        codeArea.setContentData(null);
        ((EditModeCapable) codeArea).setEditMode(EditMode.EXPANDING);
    }
}
