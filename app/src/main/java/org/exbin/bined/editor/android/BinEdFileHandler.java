/*
 * Copyright (C) ExBin Project, https://exbin.org
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
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.delta.DeltaDocument;
import org.exbin.auxiliary.binary_data.delta.SegmentsRepository;
import org.exbin.auxiliary.binary_data.android_jna.JnaBufferEditableData;
import org.exbin.auxiliary.binary_data.android_jna.paged.JnaBufferPagedData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.android.capability.CharAssessorPainterCapable;
import org.exbin.bined.android.capability.ColorAssessorPainterCapable;
import org.exbin.bined.operation.android.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.android.CodeAreaUndoRedo;
import org.exbin.bined.component.BinEdCodeAreaAssessor;
import org.exbin.bined.component.FileProcessingMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

/**
 * File handler for binary editor.
 */
@NullMarked
public class BinEdFileHandler {

    private SegmentsRepository segmentsRepository;

    private CodeArea codeArea;
    private CodeAreaUndoRedo undoRedo;
    private BinEdCodeAreaAssessor codeAreaAssessor;
    private CodeAreaTableMapAssessor codeAreaTableMapAssessor = new CodeAreaTableMapAssessor();

    private long documentOriginalSize = 0;
    private @Nullable Uri currentFileUri = null;
    private @Nullable Uri pickerInitialUri = null;
    private long selectionStart = -1;
    private long selectionEnd = -1;

    public BinEdFileHandler(CodeArea codeArea) {
        // ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        this.codeArea = codeArea;

        codeArea.setContentData(new JnaBufferEditableData());
        undoRedo = new CodeAreaUndoRedo(codeArea);

        DefaultCodeAreaCommandHandler defCommandHandler = (DefaultCodeAreaCommandHandler) codeArea.getCommandHandler();
        defCommandHandler.detach();
        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea.getContext(), codeArea, undoRedo);
        commandHandler.setCodeAreaTableMapAssessor(codeAreaTableMapAssessor);
        codeArea.setCommandHandler(commandHandler);
        CodeAreaPainter painter = codeArea.getPainter();
        codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable) painter).getColorAssessor(), codeAreaTableMapAssessor); // ((CharAssessorPainterCapable) painter).getCharAssessor()
        ((ColorAssessorPainterCapable) painter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable) painter).setCharAssessor(codeAreaAssessor);
        codeArea.setPainter(painter);
    }

    public CodeAreaTableMapAssessor getCodeAreaTableMapAssessor() {
        return codeAreaTableMapAssessor;
    }

    public void setNewData(FileProcessingMode fileProcessingMode) {
        if (fileProcessingMode == FileProcessingMode.DELTA) {
            codeArea.setContentData(segmentsRepository.createDocument());
        } else {
            codeArea.setContentData(new JnaBufferPagedData());
        }

        undoRedo.clear();
        currentFileUri = null;

        documentOriginalSize = 0;
    }

    public void openFile(ContentResolver contentResolver, Uri fileUri, FileProcessingMode fileProcessingMode) {
        BinaryData oldData = codeArea.getContentData();
        try {
            if (fileProcessingMode == FileProcessingMode.DELTA) {
                ContentDataSource dataSource = new ContentDataSource(contentResolver, fileUri);
                segmentsRepository.addDataSource(dataSource);
                DeltaDocument document = segmentsRepository.createDocument(dataSource);
                codeArea.setContentData(document);
                oldData.dispose();
            } else {
                BinaryData data = oldData;
                if (!(data instanceof PagedData)) {
                    data = new JnaBufferPagedData();
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

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public CodeAreaUndoRedo getUndoRedo() {
        return undoRedo;
    }

    public BinEdCodeAreaAssessor getCodeAreaAssessor() {
        return codeAreaAssessor;
    }

    public long getDocumentOriginalSize() {
        return documentOriginalSize;
    }

    public FileProcessingMode getFileProcessingMode() {
        return getCodeArea().getContentData() instanceof DeltaDocument ? FileProcessingMode.DELTA : FileProcessingMode.MEMORY;
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

    public void selectionByStart(CodeAreaCaretPosition caretPosition) {
        selectionStart = caretPosition.getDataPosition();
        SelectionRange selection = codeArea.getSelection();
        if (selection.isEmpty()) {
            if (selectionEnd >= 0) {
                codeArea.setSelection(selectionStart, selectionEnd);
            }
        } else {
            codeArea.setSelection(selectionStart, selection.getEnd());
        }
    }

    public void selectionByEnd(CodeAreaCaretPosition caretPosition) {
        selectionEnd = caretPosition.getDataPosition();
        SelectionRange selection = codeArea.getSelection();
        if (selection.isEmpty()) {
            if (selectionStart >= 0) {
                codeArea.setSelection(selectionStart, selectionEnd);
            }
        } else {
            codeArea.setSelection(selection.getStart(), selectionEnd);
        }
    }

    public void clearSelectionPoints() {
        selectionStart = -1;
        selectionEnd = -1;
        codeArea.clearSelection();
    }
}
