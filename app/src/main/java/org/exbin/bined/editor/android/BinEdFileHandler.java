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

import android.content.ContentResolver;
import android.content.res.XmlResourceParser;
import android.net.Uri;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.EditOperation;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.capability.CharAssessorPainterCapable;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;
import org.exbin.bined.operation.android.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.android.CodeAreaUndoRedo;
import org.exbin.framework.bined.BinEdCodeAreaAssessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * File handler for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFileHandler {

    private CodeArea codeArea;
    private CodeAreaUndoRedo undoRedo;

    private long documentOriginalSize = 0;
    private Uri currentFileUri = null;
    private Uri pickerInitialUri = null;

    public BinEdFileHandler(CodeArea codeArea) {
        // ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        this.codeArea = codeArea;
        codeArea.setContentData(new ByteArrayEditableData());
        codeArea.setEditOperation(EditOperation.INSERT);
        undoRedo = new CodeAreaUndoRedo(codeArea);

        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea.getContext(), codeArea, undoRedo);
        codeArea.setCommandHandler(commandHandler);
        CodeAreaPainter painter = codeArea.getPainter();
        BinEdCodeAreaAssessor codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable) painter).getColorAssessor(), ((CharAssessorPainterCapable) painter).getCharAssessor());
        ((ColorAssessorPainterCapable) painter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable) painter).setCharAssessor(codeAreaAssessor);
        codeArea.setPainter(painter);
    }

    public void newFile() {
        codeArea.setContentData(new ByteArrayEditableData());
        undoRedo.clear();
        currentFileUri = null;

        documentOriginalSize = 0;
    }

    public void openFile(ContentResolver contentResolver, Uri fileUri) {
        ByteArrayEditableData fileData = new ByteArrayEditableData();
        try {
            InputStream inputStream = contentResolver.openInputStream(fileUri);
            if (inputStream == null) {
                return;
            }
            fileData.loadFromStream(inputStream);
            inputStream.close();
            documentOriginalSize = fileData.getDataSize();
            undoRedo.clear();
            codeArea.setContentData(fileData);
            currentFileUri = fileUri;
            pickerInitialUri = fileUri;
        } catch (IOException ex) {
            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveFile(ContentResolver contentResolver, Uri fileUri) {
        BinaryData contentData = codeArea.getContentData();
        try {
            OutputStream outputStream = contentResolver.openOutputStream(fileUri);
            if (outputStream == null) {
                return;
            }
            codeArea.getContentData().saveToStream(outputStream);
            outputStream.close();
            documentOriginalSize = contentData.getDataSize();
            undoRedo.setSyncPosition();
            currentFileUri = fileUri;
            pickerInitialUri = fileUri;
        } catch (IOException ex) {
            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Nonnull
    public CodeArea getCodeArea() {
        return codeArea;
    }

    @Nonnull
    public CodeAreaUndoRedo getUndoRedo() {
        return undoRedo;
    }

    public long getDocumentOriginalSize() {
        return documentOriginalSize;
    }

    @Nullable
    public Uri getCurrentFileUri() {
        return currentFileUri;
    }

    @Nullable
    public Uri getPickerInitialUri() {
        return pickerInitialUri;
    }

    public void clearFileUri() {
        currentFileUri = null;
        pickerInitialUri = null;
    }

    public boolean isModified() {
        return undoRedo.isModified();
    }
}
