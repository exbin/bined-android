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

import android.content.Context;
import android.util.AttributeSet;

import androidx.test.core.app.ApplicationProvider;

import org.exbin.bined.android.CodeAreaCore;

import javax.annotation.Nonnull;

/**
 * Tests for CodeArea component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaComponentTest {

    public CodeAreaComponentTest() {
    }

    @Nonnull
    public CodeAreaCore createCodeArea() {
        Context applicationContext = ApplicationProvider.getApplicationContext();
        AttributeSet attributeSet = null;
        return new CodeArea(applicationContext, attributeSet);
    }
}
