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
package org.exbin.bined;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test CodeAreaUtils class.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaUtilsTest {

    @Test
    public void testInsertHexStringIntoDataHexadecimal() {
        byte[] expectedData = new byte[33];
        for (int i = 0; i < 32; i++) {
            expectedData[i] = (byte) i;
        }
        expectedData[32] = (byte) 0xff;

        EditableBinaryData resultData = new ByteArrayEditableData();
        CodeAreaUtils.insertHexStringIntoData("00 01 02 03 04 05 06 07 08 9 0A 0b c 0D E 0f 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f ff", resultData, CodeType.HEXADECIMAL);
        checkResultData(expectedData, resultData);

        resultData = new ByteArrayEditableData();
        CodeAreaUtils.insertHexStringIntoData("000102030405060708090A0b0c0D0E0f101112131415161718191a1b1c1d1e1fFf", resultData, CodeType.HEXADECIMAL);
        checkResultData(expectedData, resultData);
    }

    @Test
    public void testInsertHexStringIntoDataDecimal() {
        byte[] expectedData = new byte[23];
        for (int i = 0; i < 20; i++) {
            expectedData[i] = (byte) i;
        }
        expectedData[20] = 100;
        expectedData[21] = (byte) 200;
        expectedData[22] = (byte) 255;

        EditableBinaryData resultData = new ByteArrayEditableData();
        CodeAreaUtils.insertHexStringIntoData("00 01 02 03 04 05 06 07 08 9 10 11 12 13 14 15 16 17 18 19 100 200 255", resultData, CodeType.DECIMAL);
        checkResultData(expectedData, resultData);
    }

    public static void checkResultData(byte[] expectedData, BinaryData data) {
        Assert.assertEquals(expectedData.length, data.getDataSize());
        byte[] resultData = new byte[expectedData.length];
        data.copyToArray(0, resultData, 0, expectedData.length);
        Assert.assertArrayEquals(expectedData, resultData);
    }
}
