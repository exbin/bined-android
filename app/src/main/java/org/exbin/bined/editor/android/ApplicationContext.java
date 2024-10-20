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
package org.exbin.bined.editor.android;

import android.app.Application;

import org.exbin.auxiliary.binary_data.delta.SegmentsRepository;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.editor.android.preference.BinaryEditorPreferences;
import org.exbin.bined.editor.android.preference.PreferencesWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Application context.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ApplicationContext extends Application {

    private BinaryEditorPreferences appPreferences;
    private final SegmentsRepository segmentsRepository = new SegmentsRepository();

    private BinEdFileHandler fileHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();

        appPreferences = new BinaryEditorPreferences(new PreferencesWrapper(getApplicationContext()));
    }

    @Nonnull
    public BinaryEditorPreferences getAppPreferences() {
        return appPreferences;
    }

    @Nullable
    public BinEdFileHandler getFileHandler() {
        return fileHandler;
    }

    @Nonnull
    public BinEdFileHandler createFileHandler(CodeArea codeArea) {
        fileHandler = new BinEdFileHandler(codeArea);
        fileHandler.setSegmentsRepository(segmentsRepository);
        fileHandler.setNewData(appPreferences.getEditorPreferences().getFileHandlingMode());
        return fileHandler;
    }
}
