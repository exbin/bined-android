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
import android.net.Uri;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.delta.DeltaDocument;
import org.exbin.auxiliary.binary_data.delta.SegmentsRepository;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.EditOperation;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.capability.CharAssessorPainterCapable;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;
import org.exbin.bined.operation.android.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.android.CodeAreaUndoRedo;
import org.exbin.framework.bined.BinEdCodeAreaAssessor;
import org.exbin.framework.bined.FileHandlingMode;

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

    private SegmentsRepository segmentsRepository;

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

    public void setNewData(FileHandlingMode fileHandlingMode) {
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            codeArea.setContentData(segmentsRepository.createDocument());
        } else {
            codeArea.setContentData(new PagedData());
        }

        undoRedo.clear();
        currentFileUri = null;

        documentOriginalSize = 0;
    }

    public void openFile(ContentResolver contentResolver, Uri fileUri, FileHandlingMode fileHandlingMode) {
        BinaryData oldData = codeArea.getContentData();
        try {
            if (fileHandlingMode == FileHandlingMode.DELTA) {
                ContentDataSource dataSource = new ContentDataSource(contentResolver, fileUri);
                segmentsRepository.addDataSource(dataSource);
                DeltaDocument document = segmentsRepository.createDocument(dataSource);
                codeArea.setContentData(document);
                oldData.dispose();
            } else {
                BinaryData data = oldData;
                if (!(data instanceof PagedData)) {
                    data = new PagedData();
                    oldData.dispose();
                }
                InputStream inputStream = contentResolver.openInputStream(fileUri);
                if (inputStream == null) {
                    return;
                }
                ((EditableBinaryData) data).loadFromStream(inputStream);
                inputStream.close();
                codeArea.setContentData(data);
            }

            undoRedo.clear();
            currentFileUri = fileUri;
            pickerInitialUri = fileUri;
            fileSync();
        } catch (IOException ex) {
            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveFile(ContentResolver contentResolver, Uri fileUri) {
        BinaryData contentData = codeArea.getContentData();
        try {
            if (contentData instanceof DeltaDocument) {
                // TODO freezes window / replace with progress bar
                DeltaDocument document = (DeltaDocument) contentData;
                ContentDataSource fileSource = (ContentDataSource) document.getDataSource();
                if (fileSource == null || !fileUri.equals(fileSource.getFileUri())) {
                    fileSource = new ContentDataSource(contentResolver, fileUri);
                    segmentsRepository.addDataSource(fileSource);
                    document.setDataSource(fileSource);
                }
                if (fileSource == null) {
                    throw new IllegalStateException("Unexpected state");
                }
                segmentsRepository.saveDocument(document);

                fileSync();
                currentFileUri = fileUri;
                pickerInitialUri = fileUri;
            } else {
                OutputStream outputStream = contentResolver.openOutputStream(fileUri);
                if (outputStream == null) {
                    return;
                }
                contentData.saveToStream(outputStream);
                outputStream.close();

                fileSync();
                currentFileUri = fileUri;
                pickerInitialUri = fileUri;
            }
        } catch (IOException ex) {
            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fileSync() {
        documentOriginalSize = getCodeArea().getDataSize();
        undoRedo.setSyncPosition();
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

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return getCodeArea().getContentData() instanceof DeltaDocument ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY;
    }

    public void setSegmentsRepository(SegmentsRepository segmentsRepository) {
        this.segmentsRepository = segmentsRepository;
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
