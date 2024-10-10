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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;

/**
 * Tests for codeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaTest {

    public static final String SAMPLE_FILES_PATH = "/org/exbin/bined/resources/test/";
    public static final String SAMPLE_5BYTES = SAMPLE_FILES_PATH + "5bytes.dat";
    public static final String SAMPLE_10BYTES = SAMPLE_FILES_PATH + "10bytes.dat";
    public static final String SAMPLE_ALLBYTES = SAMPLE_FILES_PATH + "allbytes.dat";

    private CodeAreaTest() {
    }

    @Nonnull
    public static EditableBinaryData getSampleData(String dataPath) {
        ByteArrayEditableData data = new ByteArrayEditableData();
        try (InputStream stream = CodeAreaTest.class.getResourceAsStream(dataPath)) {
            data.loadFromStream(stream);
        } catch (IOException ex) {
            Logger.getLogger(CodeAreaTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }
}
