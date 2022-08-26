/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.android.handler;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.editor.android.R;
import org.exbin.framework.bined.StatusCursorPositionFormat;
import org.exbin.framework.bined.StatusDocumentSizeFormat;
import org.exbin.framework.bined.BinaryStatusApi;

import javax.annotation.Nonnull;

/**
 * Binary editor status handler.
 *
 * @version 0.2.1 2021/12/09
 * @author ExBin Project (http://exbin.org)
 */
public class BinaryStatusHandler implements BinaryStatusApi {

    public static final String INSERT_EDIT_MODE_LABEL = "INS";
    public static final String OVERWRITE_EDIT_MODE_LABEL = "OVR";
    public static final String READONLY_EDIT_MODE_LABEL = "RO";
    public static final String INPLACE_EDIT_MODE_LABEL = "INP";

    public static final String OCTAL_CODE_TYPE_LABEL = "OCT";
    public static final String DECIMAL_CODE_TYPE_LABEL = "DEC";
    public static final String HEXADECIMAL_CODE_TYPE_LABEL = "HEX";

    public static int DEFAULT_OCTAL_SPACE_GROUP_SIZE = 4;
    public static int DEFAULT_DECIMAL_SPACE_GROUP_SIZE = 3;
    public static int DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE = 4;

    private static final String BR_TAG = "<br>";

    private final AppCompatActivity app;
    private StatusCursorPositionFormat cursorPositionFormat = new StatusCursorPositionFormat();
    private StatusDocumentSizeFormat documentSizeFormat = new StatusDocumentSizeFormat();
    private int octalSpaceGroupSize = DEFAULT_OCTAL_SPACE_GROUP_SIZE;
    private int decimalSpaceGroupSize = DEFAULT_DECIMAL_SPACE_GROUP_SIZE;
    private int hexadecimalSpaceGroupSize = DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE;

    private EditOperation editOperation;
    private CodeAreaCaretPosition caretPosition;
    private SelectionRange selectionRange;
    private long documentSize;
    private long initialDocumentSize;

    public BinaryStatusHandler(AppCompatActivity app) {
        this.app = app;
    }

    public void updateStatus() {
        updateCaretPosition();
        updateCursorPositionToolTip();
        updateDocumentSize();
        updateDocumentSizeToolTip();

//        switch (cursorPositionFormat.getCodeType()) {
//            case OCTAL: {
//                octalCursorPositionModeRadioButtonMenuItem.setSelected(true);
//                break;
//            }
//            case DECIMAL: {
//                decimalCursorPositionModeRadioButtonMenuItem.setSelected(true);
//                break;
//            }
//            case HEXADECIMAL: {
//                hexadecimalCursorPositionModeRadioButtonMenuItem.setSelected(true);
//                break;
//            }
//            default:
//                throw CodeAreaUtils.getInvalidTypeException(cursorPositionFormat.getCodeType());
//        }
//        cursorPositionShowOffsetCheckBoxMenuItem.setSelected(cursorPositionFormat.isShowOffset());
//
//        switch (documentSizeFormat.getCodeType()) {
//            case OCTAL: {
//                octalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
//                break;
//            }
//            case DECIMAL: {
//                decimalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
//                break;
//            }
//            case HEXADECIMAL: {
//                hexadecimalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
//                break;
//            }
//            default:
//                throw CodeAreaUtils.getInvalidTypeException(documentSizeFormat.getCodeType());
//        }
//        documentSizeShowRelativeCheckBoxMenuItem.setSelected(documentSizeFormat.isShowRelative());
    }

    @Override
    public void setCursorPosition(CodeAreaCaretPosition caretPosition) {
        this.caretPosition = caretPosition;
        updateCaretPosition();
        updateCursorPositionToolTip();
    }

    @Override
    public void setSelectionRange(SelectionRange selectionRange) {
        this.selectionRange = selectionRange;
        updateCaretPosition();
        updateCursorPositionToolTip();
        updateDocumentSize();
        updateDocumentSizeToolTip();
    }

    @Override
    public void setCurrentDocumentSize(long documentSize, long initialDocumentSize) {
        this.documentSize = documentSize;
        this.initialDocumentSize = initialDocumentSize;
        updateDocumentSize();
        updateDocumentSizeToolTip();
    }

//    @Nonnull
//    @Override
//    public String getEncoding() {
//        return encodingLabel.getText();
//    }
//
//    @Override
    public void setEncoding(String encodingName) {
        final TextView charsetTextView = (TextView) app.findViewById(R.id.charset);
        charsetTextView.setText(encodingName);
//        encodingLabel.setText(encodingName + " ^");
    }

    @Override
    public void setEditMode(EditMode editMode, EditOperation editOperation) {
        this.editOperation = editOperation;

        final TextView editModeLabel = (TextView) app.findViewById(R.id.editModeLabel);

        switch (editMode) {
            case READ_ONLY: {
                editModeLabel.setText(READONLY_EDIT_MODE_LABEL);
                break;
            }
            case EXPANDING:
            case CAPPED: {
                switch (editOperation) {
                    case INSERT: {
                        editModeLabel.setText(INSERT_EDIT_MODE_LABEL);
                        break;
                    }
                    case OVERWRITE: {
                        editModeLabel.setText(OVERWRITE_EDIT_MODE_LABEL);
                        break;
                    }
                    default:
                        throw CodeAreaUtils.getInvalidTypeException(editOperation);
                }
                break;
            }
            case INPLACE: {
                editModeLabel.setText(INPLACE_EDIT_MODE_LABEL);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(editMode);
        }
    }

    @Override
    public void setMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
        final TextView memoryModeLabel = (TextView) app.findViewById(R.id.memoryModeLabel);
        memoryModeLabel.setText(memoryMode.getDisplayChar());
//        boolean enabled = memoryMode != MemoryMode.READ_ONLY;
//        deltaMemoryModeRadioButtonMenuItem.setEnabled(enabled);
//        ramMemoryModeRadioButtonMenuItem.setEnabled(enabled);
//        if (memoryMode == MemoryMode.DELTA_MODE) {
//            deltaMemoryModeRadioButtonMenuItem.setSelected(true);
//        } else {
//            ramMemoryModeRadioButtonMenuItem.setSelected(true);
//        }
    }

    private void updateCaretPosition() {
        final TextView cursorPositionLabel = (TextView) app.findViewById(R.id.cursorPositionLabel);
        if (caretPosition == null) {
            cursorPositionLabel.setText("-");
        } else {
            StringBuilder labelBuilder = new StringBuilder();
            if (selectionRange != null && !selectionRange.isEmpty()) {
                long first = selectionRange.getFirst();
                long last = selectionRange.getLast();
                labelBuilder.append(numberToPosition(first, cursorPositionFormat.getCodeType()));
                labelBuilder.append(" to ");
                labelBuilder.append(numberToPosition(last, cursorPositionFormat.getCodeType()));
            } else {
                labelBuilder.append(numberToPosition(caretPosition.getDataPosition(), cursorPositionFormat.getCodeType()));
                if (cursorPositionFormat.isShowOffset()) {
                    labelBuilder.append(":");
                    labelBuilder.append(caretPosition.getCodeOffset());
                }
            }
            cursorPositionLabel.setText(labelBuilder.toString());
        }
    }

    private void updateCursorPositionToolTip() {
        final TextView cursorPositionLabel = (TextView) app.findViewById(R.id.cursorPositionLabel);

//        StringBuilder builder = new StringBuilder();
//        builder.append("<html>");
//        if (caretPosition == null) {
//            builder.append(resourceBundle.getString("cursorPositionLabel.toolTipText"));
//        } else {
//            if (selectionRange != null && !selectionRange.isEmpty()) {
//                long first = selectionRange.getFirst();
//                long last = selectionRange.getLast();
//                builder.append(resourceBundle.getString("selectionFromLabel.toolTipText")).append(BR_TAG);
//                builder.append(OCTAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(first, PositionCodeType.OCTAL)).append(BR_TAG);
//                builder.append(DECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(first, PositionCodeType.DECIMAL)).append(BR_TAG);
//                builder.append(HEXADECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(first, PositionCodeType.HEXADECIMAL)).append(BR_TAG);
//                builder.append(BR_TAG);
//                builder.append(resourceBundle.getString("selectionToLabel.toolTipText")).append(BR_TAG);
//                builder.append(OCTAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(last, PositionCodeType.OCTAL)).append(BR_TAG);
//                builder.append(DECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(last, PositionCodeType.DECIMAL)).append(BR_TAG);
//                builder.append(HEXADECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(first, PositionCodeType.HEXADECIMAL)).append(BR_TAG);
//            } else {
//                long dataPosition = caretPosition.getDataPosition();
//                builder.append(resourceBundle.getString("cursorPositionLabel.toolTipText")).append(BR_TAG);
//                builder.append(OCTAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(dataPosition, PositionCodeType.OCTAL)).append(BR_TAG);
//                builder.append(DECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(dataPosition, PositionCodeType.DECIMAL)).append(BR_TAG);
//                builder.append(HEXADECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(dataPosition, PositionCodeType.HEXADECIMAL));
//                builder.append("</html>");
//            }
//        }
//
//        cursorPositionLabel.setToolTipText(builder.toString());
    }

    private void updateDocumentSize() {
        final TextView documentSizeLabel = (TextView) app.findViewById(R.id.documentSizeLabel);

        if (documentSize == -1) {
            documentSizeLabel.setText(documentSizeFormat.isShowRelative() ? "0 (0)" : "0");
        } else {
            StringBuilder labelBuilder = new StringBuilder();
            if (selectionRange != null && !selectionRange.isEmpty()) {
                labelBuilder.append(numberToPosition(selectionRange.getLength(), documentSizeFormat.getCodeType()));
                labelBuilder.append(" of ");
                labelBuilder.append(numberToPosition(documentSize, documentSizeFormat.getCodeType()));
            } else {
                labelBuilder.append(numberToPosition(documentSize, documentSizeFormat.getCodeType()));
                if (documentSizeFormat.isShowRelative()) {
                    long difference = documentSize - initialDocumentSize;
                    labelBuilder.append(difference > 0 ? " (+" : " (");
                    labelBuilder.append(numberToPosition(difference, documentSizeFormat.getCodeType()));
                    labelBuilder.append(")");

                }
            }

            documentSizeLabel.setText(labelBuilder.toString());
        }
    }

    private void updateDocumentSizeToolTip() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("<html>");
//        if (selectionRange != null && !selectionRange.isEmpty()) {
//            long length = selectionRange.getLength();
//            builder.append(resourceBundle.getString("selectionLengthLabel.toolTipText")).append(BR_TAG);
//            builder.append(OCTAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(length, PositionCodeType.OCTAL)).append(BR_TAG);
//            builder.append(DECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(length, PositionCodeType.DECIMAL)).append(BR_TAG);
//            builder.append(HEXADECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(length, PositionCodeType.HEXADECIMAL)).append(BR_TAG);
//            builder.append(BR_TAG);
//        }
//
//        builder.append(resourceBundle.getString("documentSizeLabel.toolTipText")).append(BR_TAG);
//        builder.append(OCTAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(documentSize, PositionCodeType.OCTAL)).append(BR_TAG);
//        builder.append(DECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(documentSize, PositionCodeType.DECIMAL)).append(BR_TAG);
//        builder.append(HEXADECIMAL_CODE_TYPE_LABEL + ": ").append(numberToPosition(documentSize, PositionCodeType.HEXADECIMAL));
//        builder.append("</html>");
//        documentSizeLabel.setToolTipText(builder.toString());
    }

    @Nonnull
    private String numberToPosition(long value, PositionCodeType codeType) {
        if (value == 0) {
            return "0";
        }

        int spaceGroupSize = 0;
        switch (codeType) {
            case OCTAL: {
                spaceGroupSize = octalSpaceGroupSize;
                break;
            }
            case DECIMAL: {
                spaceGroupSize = decimalSpaceGroupSize;
                break;
            }
            case HEXADECIMAL: {
                spaceGroupSize = hexadecimalSpaceGroupSize;
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(codeType);
        }

        long remainder = value > 0 ? value : -value;
        StringBuilder builder = new StringBuilder();
        int base = codeType.getBase();
        int groupSize = spaceGroupSize == 0 ? -1 : spaceGroupSize;
        while (remainder > 0) {
            if (groupSize >= 0) {
                if (groupSize == 0) {
                    builder.insert(0, ' ');
                    groupSize = spaceGroupSize - 1;
                } else {
                    groupSize--;
                }
            }

            int digit = (int) (remainder % base);
            remainder = remainder / base;
            builder.insert(0, CodeAreaUtils.UPPER_HEX_CODES[digit]);
        }

        if (value < 0) {
            builder.insert(0, "-");
        }
        return builder.toString();
    }
}
