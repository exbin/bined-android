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
package org.exbin.bined.editor.android.inspector;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.buffer.BufferEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretListener;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.editor.android.R;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.android.command.CodeAreaCompoundCommand;
import org.exbin.bined.operation.android.command.InsertDataCommand;
import org.exbin.bined.operation.android.command.ModifyDataCommand;
import org.exbin.bined.operation.undo.BinaryDataUndoRedo;
import org.exbin.bined.operation.undo.BinaryDataUndoRedoChangeListener;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.InputMismatchException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Values side panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BasicValuesInspector {

    public static final int UBYTE_MAX_VALUE = 255;
    public static final int SWORD_MIN_VALUE = -32768;
    public static final int SWORD_MAX_VALUE = 32767;
    public static final int UWORD_MAX_VALUE = 65535;
    public static final long UINT_MAX_VALUE = 4294967295L;
    public static final BigInteger ULONG_MAX_VALUE = new BigInteger("4294967295");
    public static final BigInteger BIG_INTEGER_BYTE_MASK = BigInteger.valueOf(255);
    public static final String VALUE_OUT_OF_RANGE = "Value is out of range";
    public static int CACHE_SIZE = 250;

    private View view;
    private CodeArea codeArea;
    private BinaryDataUndoRedo undoRedo;
    private long dataPosition;
    private DataChangedListener dataChangedListener;
    private CodeAreaCaretListener caretMovedListener;
    private BinaryDataUndoRedoChangeListener undoRedoChangeListener;

    private final byte[] valuesCache = new byte[CACHE_SIZE];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);
    private final ValuesUpdater valuesUpdater = new ValuesUpdater();

    public BasicValuesInspector() {
    }

    public void setCodeArea(CodeArea codeArea, @Nullable BinaryDataUndoRedo undoRedo, View view) {
        this.codeArea = codeArea;
        this.undoRedo = undoRedo;
        this.view = view;

        RadioButton littleEndianRadioButton = view.findViewById(R.id.radioButtonLittleEndian);
        littleEndianRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateValues());
        RadioButton bigEndianRadioButton = view.findViewById(R.id.radioButtonBigEndian);
        bigEndianRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateValues());
        RadioButton signedRadioButton = view.findViewById(R.id.radioButtonSigned);
        signedRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateValues());
        RadioButton unsignedRadioButton = view.findViewById(R.id.radioButtonUnsigned);
        unsignedRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateValues());

        ((CheckBox) view.findViewById(R.id.checkBoxBit0)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox0ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit1)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox1ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit2)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox2ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit3)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox3ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit4)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox4ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit5)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox5ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit6)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox6ActionPerformed(isChecked));
        ((CheckBox) view.findViewById(R.id.checkBoxBit7)).setOnCheckedChangeListener((buttonView, isChecked) -> binaryCheckBox7ActionPerformed(isChecked));

        ((EditText) view.findViewById(R.id.editTextByte)).setOnEditorActionListener(this::byteTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextWord)).setOnEditorActionListener(this::wordTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextInteger)).setOnEditorActionListener(this::intTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextLong)).setOnEditorActionListener(this::longTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextFloat)).setOnEditorActionListener(this::floatTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextDouble)).setOnEditorActionListener(this::doubleTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextCharacter)).setOnEditorActionListener(this::characterTextFieldKeyReleased);
        ((EditText) view.findViewById(R.id.editTextString)).setOnEditorActionListener(this::stringTextFieldKeyReleased);
    }

    private void binaryCheckBox0ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x80) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x80);
            modifyValues(1);
        }
    }

    private void binaryCheckBox1ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x40) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x40);
            modifyValues(1);
        }
    }

    private void binaryCheckBox2ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x20) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x20);
            modifyValues(1);
        }
    }

    private void binaryCheckBox3ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x10) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x10);
            modifyValues(1);
        }
    }

    private void binaryCheckBox4ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x8) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x8);
            modifyValues(1);
        }
    }

    private void binaryCheckBox5ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x4) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x4);
            modifyValues(1);
        }
    }

    private void binaryCheckBox6ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x2) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x2);
            modifyValues(1);
        }
    }

    private void binaryCheckBox7ActionPerformed(boolean checked) {
        if (!valuesUpdater.isUpdateInProgress() && ((valuesCache[0] & 0x1) > 0 != checked)) {
            valuesCache[0] = (byte) (valuesCache[0] ^ 0x1);
            modifyValues(1);
        }
    }

    private boolean byteTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                int intValue = Integer.parseInt(((EditText) view.findViewById(R.id.editTextByte)).getText().toString());
                if (isSigned()) {
                    if (intValue < Byte.MIN_VALUE || intValue > Byte.MAX_VALUE) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }
                } else {
                    if (intValue < 0 || intValue > UBYTE_MAX_VALUE) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }
                }

                valuesCache[0] = (byte) intValue;
                modifyValues(1);
                updateValues();
            } catch (NumberFormatException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean wordTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                int intValue = Integer.parseInt(((EditText) view.findViewById(R.id.editTextWord)).getText().toString());
                if (isSigned()) {
                    if (intValue < SWORD_MIN_VALUE || intValue > SWORD_MAX_VALUE) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }
                } else {
                    if (intValue < 0 || intValue > UWORD_MAX_VALUE) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }
                }

                if (getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                    valuesCache[0] = (byte) (intValue & 0xff);
                    valuesCache[1] = (byte) ((intValue >> 8) & 0xff);
                } else {
                    valuesCache[0] = (byte) ((intValue >> 8) & 0xff);
                    valuesCache[1] = (byte) (intValue & 0xff);
                }
                modifyValues(2);
                updateValues();
            } catch (NumberFormatException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean intTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                long longValue = Long.parseLong(((EditText) view.findViewById(R.id.editTextInteger)).getText().toString());
                if (isSigned()) {
                    if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }
                } else {
                    if (longValue < 0 || longValue > UINT_MAX_VALUE) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }
                }

                if (getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                    valuesCache[0] = (byte) (longValue & 0xff);
                    valuesCache[1] = (byte) ((longValue >> 8) & 0xff);
                    valuesCache[2] = (byte) ((longValue >> 16) & 0xff);
                    valuesCache[3] = (byte) ((longValue >> 24) & 0xff);
                } else {
                    valuesCache[0] = (byte) ((longValue >> 24) & 0xff);
                    valuesCache[1] = (byte) ((longValue >> 16) & 0xff);
                    valuesCache[2] = (byte) ((longValue >> 8) & 0xff);
                    valuesCache[3] = (byte) (longValue & 0xff);
                }
                modifyValues(4);
                updateValues();
            } catch (NumberFormatException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean longTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                ByteOrder byteOrder = getByteOrder();
                if (isSigned()) {
                    long longValue = Long.parseLong(((EditText) view.findViewById(R.id.editTextLong)).getText().toString());

                    byteBuffer.rewind();
                    if (byteBuffer.order() != byteOrder) {
                        byteBuffer.order(byteOrder);
                    }

                    byteBuffer.putLong(longValue);
                } else {
                    BigInteger bigInteger = new BigInteger(((EditText) view.findViewById(R.id.editTextLong)).getText().toString());
                    if (bigInteger.compareTo(BigInteger.ZERO) == -1 || bigInteger.compareTo(ULONG_MAX_VALUE) == 1) {
                        throw new NumberFormatException(VALUE_OUT_OF_RANGE);
                    }

                    if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                        for (int i = 0; i < 7; i++) {
                            BigInteger nextByte = bigInteger.and(BIG_INTEGER_BYTE_MASK);
                            valuesCache[7 - i] = nextByte.byteValue();
                            bigInteger = bigInteger.shiftRight(8);
                        }
                    } else {
                        for (int i = 0; i < 7; i++) {
                            BigInteger nextByte = bigInteger.and(BIG_INTEGER_BYTE_MASK);
                            valuesCache[i] = nextByte.byteValue();
                            bigInteger = bigInteger.shiftRight(8);
                        }
                    }
                }

                modifyValues(8);
                updateValues();
            } catch (NumberFormatException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean floatTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                ByteOrder byteOrder = getByteOrder();
                float floatValue = Float.parseFloat(((EditText) view.findViewById(R.id.editTextFloat)).getText().toString());

                byteBuffer.rewind();
                if (byteBuffer.order() != byteOrder) {
                    byteBuffer.order(byteOrder);
                }

                byteBuffer.putFloat(floatValue);

                modifyValues(4);
                updateValues();
            } catch (NumberFormatException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean doubleTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                ByteOrder byteOrder = getByteOrder();
                double doubleValue = Double.parseDouble(((EditText) view.findViewById(R.id.editTextDouble)).getText().toString());

                byteBuffer.rewind();
                if (byteBuffer.order() != byteOrder) {
                    byteBuffer.order(byteOrder);
                }

                byteBuffer.putDouble(doubleValue);

                modifyValues(8);
                updateValues();
            } catch (NumberFormatException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean characterTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                String characterText = ((EditText) view.findViewById(R.id.editTextCharacter)).getText().toString();
                if (characterText.isEmpty()) {
                    throw new InputMismatchException("Empty value not valid");
                }

                if (characterText.length() > 1) {
                    throw new InputMismatchException("Only single character allowed");
                }

                byte[] bytes = characterText.getBytes(codeArea.getCharset());
                System.arraycopy(bytes, 0, valuesCache, 0, bytes.length);

                modifyValues(bytes.length);
                updateValues();
            } catch (InputMismatchException ex) {
                showException(ex);
            }
        }

        return false;
    }

    private boolean stringTextFieldKeyReleased(TextView v, int actionId, KeyEvent event) {
        if (isEnterValue(actionId, event) && isEditable()) {
            try {
                String characterText = ((EditText) view.findViewById(R.id.editTextString)).getText().toString();
                if (characterText.isEmpty()) {
                    throw new InputMismatchException("Empty value not valid");
                }

                byte[] bytes = characterText.getBytes(codeArea.getCharset());
                if (bytes.length > CACHE_SIZE) {
                    throw new InputMismatchException("String is too long");
                }
                System.arraycopy(bytes, 0, valuesCache, 0, bytes.length);

                modifyValues(bytes.length);
                updateValues();
            } catch (InputMismatchException ex) {
                showException(ex);
            }
        }

        return false;
    }
    
    private boolean isEnterValue(int actionId, @Nullable KeyEvent event) {
        return actionId > EditorInfo.IME_ACTION_NONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    public void enableUpdate() {
        dataChangedListener = () -> {
            updateEditMode();
            updateValues();
        };
        codeArea.addDataChangedListener(dataChangedListener);
        caretMovedListener = (CodeAreaCaretPosition caretPosition) -> updateValues();
        codeArea.addCaretMovedListener(caretMovedListener);
        undoRedoChangeListener = new BinaryDataUndoRedoChangeListener() {
            @Override
            public void undoChanged() {
                updateValues();
            }
        };
        if (undoRedo != null) {
            undoRedo.addChangeListener(undoRedoChangeListener);
        }
        updateEditMode();
        updateValues();
    }

    public void disableUpdate() {
        codeArea.removeDataChangedListener(dataChangedListener);
        codeArea.removeCaretMovedListener(caretMovedListener);
        if (undoRedo != null) {
            undoRedo.addChangeListener(undoRedoChangeListener);
        }
    }

    public void updateEditMode() {
        boolean editable = isEditable();
        view.findViewById(R.id.checkBoxBit0).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit1).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit2).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit3).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit4).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit5).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit6).setEnabled(editable);
        view.findViewById(R.id.checkBoxBit7).setEnabled(editable);
        view.findViewById(R.id.editTextByte).setEnabled(editable);
        view.findViewById(R.id.editTextWord).setEnabled(editable);
        view.findViewById(R.id.editTextInteger).setEnabled(editable);
        view.findViewById(R.id.editTextLong).setEnabled(editable);
        view.findViewById(R.id.editTextFloat).setEnabled(editable);
        view.findViewById(R.id.editTextDouble).setEnabled(editable);
        view.findViewById(R.id.editTextCharacter).setEnabled(editable);
        view.findViewById(R.id.editTextString).setEnabled(editable);
    }

    public void updateValues() {
        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        dataPosition = caretPosition.getDataPosition();
        long dataSize = codeArea.getDataSize();

        if (dataPosition < dataSize) {
            int availableData = dataSize - dataPosition >= CACHE_SIZE ? CACHE_SIZE : (int) (dataSize - dataPosition);
            BinaryData contentData = codeArea.getContentData();
            contentData.copyToArray(dataPosition, valuesCache, 0, availableData);
            if (availableData < CACHE_SIZE) {
                Arrays.fill(valuesCache, availableData, CACHE_SIZE, (byte) 0);
            }
        }

        valuesUpdater.schedule();
    }

    private void modifyValues(int bytesCount) {
        EditableBinaryData binaryData = new BufferEditableData();
        binaryData.insert(0, valuesCache, 0, bytesCount);
        long oldDataPosition = dataPosition;
        if (dataPosition == codeArea.getDataSize()) {
            InsertDataCommand insertCommand = new InsertDataCommand(codeArea, dataPosition, ((CaretCapable) codeArea).getCodeOffset(), binaryData);
            if (undoRedo != null) {
                undoRedo.execute(insertCommand);
            }
        } else {
            BinaryDataCommand command;
            if (dataPosition + binaryData.getDataSize() > codeArea.getDataSize()) {
                long modifiedDataSize = codeArea.getDataSize() - dataPosition;
                EditableBinaryData modifiedData = (EditableBinaryData) binaryData.copy(0, modifiedDataSize);
                EditableBinaryData insertedData = (EditableBinaryData) binaryData.copy(modifiedDataSize, binaryData.getDataSize() - modifiedDataSize);
                command = new CodeAreaCompoundCommand(codeArea);
                ((CodeAreaCompoundCommand) command).addCommand(new ModifyDataCommand(codeArea, dataPosition, modifiedData));
                ((CodeAreaCompoundCommand) command).addCommand(new InsertDataCommand(codeArea, dataPosition + modifiedDataSize, ((CaretCapable) codeArea).getCodeOffset(), insertedData));
            } else {
                command = new ModifyDataCommand(codeArea, dataPosition, binaryData);
            }

            if (undoRedo != null) {
                undoRedo.execute(command);
            }
        }
        codeArea.setActiveCaretPosition(oldDataPosition);
        codeArea.repaint();
    }

    private boolean isSigned() {
        return ((RadioButton) view.findViewById(R.id.radioButtonSigned)).isChecked();
    }

    private boolean isEditable() {
        return codeArea.isEditable();
    }

    @Nonnull
    private ByteOrder getByteOrder() {
        return ((RadioButton) view.findViewById(R.id.radioButtonLittleEndian)).isChecked() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    private void showException(Exception ex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle(R.string.error_invalid_input);
        builder.setMessage(ex.getMessage());
        builder.setNegativeButton(R.string.button_ok, null);
        builder.show();
    }

    public enum ValuesPanelField {
        BINARY0,
        BINARY1,
        BINARY2,
        BINARY3,
        BINARY4,
        BINARY5,
        BINARY6,
        BINARY7,
        BYTE,
        WORD,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        CHARACTER,
        STRING
    }

    public void registerFocusPainter(BasicValuesPositionColorModifier colorModifier) {
        view.findViewById(R.id.editTextByte).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 1));
        view.findViewById(R.id.editTextWord).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 2));
        view.findViewById(R.id.editTextInteger).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 4));
        view.findViewById(R.id.editTextLong).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 8));
        view.findViewById(R.id.editTextFloat).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 4));
        view.findViewById(R.id.editTextDouble).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 8));
        view.findViewById(R.id.editTextCharacter).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 1));
        view.findViewById(R.id.editTextString).setOnFocusChangeListener(new ValueFocusListener(colorModifier, 1));
    }

    @ParametersAreNonnullByDefault
    private class ValuesUpdater {

        private boolean updateInProgress = false;
        private boolean updateTerminated = false;
        private boolean scheduleUpdate = false;
        private boolean clearFields = true;

        private boolean signed;
        private ByteOrder byteOrder;
        private byte[] values;

        private synchronized void schedule() {
            if (updateInProgress) {
                updateTerminated = true;
            }
            if (!scheduleUpdate) {
                scheduleUpdate = true;
                scheduleNextStep(ValuesPanelField.values()[0]);
            }
        }

        private void scheduleNextStep(final ValuesPanelField valuesPanelField) {
            new Handler(Looper.getMainLooper()).post(() -> updateValue(valuesPanelField));
        }

        public boolean isUpdateInProgress() {
            return updateInProgress;
        }

        private void updateValue(final ValuesPanelField valuesPanelField) {
            if (valuesPanelField.ordinal() == 0) {
                long dataSize = codeArea.getDataSize();
                clearFields = dataPosition >= dataSize;
                byteOrder = getByteOrder();
                signed = isSigned();
                values = valuesCache;
                if (clearFields) {
                    values[0] = 0;
                }
                updateStarted();
            }

            if (updateTerminated) {
                stopUpdate();
                return;
            }

            if (clearFields) {
                clearField(valuesPanelField);
            } else {
                updateField(valuesPanelField);
            }

            final ValuesPanelField[] panelFields = ValuesPanelField.values();
            ValuesPanelField lastValue = panelFields[panelFields.length - 1];
            if (valuesPanelField == lastValue) {
                stopUpdate();
            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    ValuesPanelField nextValue = panelFields[valuesPanelField.ordinal() + 1];
                    updateValue(nextValue);
                });
            }
        }

        private void updateField(ValuesPanelField valuesPanelField) {
            switch (valuesPanelField) {
                case BINARY0: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit0)).setChecked((values[0] & 0x80) > 0);
                    break;
                }
                case BINARY1: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit1)).setChecked((values[0] & 0x40) > 0);
                    break;
                }
                case BINARY2: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit2)).setChecked((values[0] & 0x20) > 0);
                    break;
                }
                case BINARY3: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit3)).setChecked((values[0] & 0x10) > 0);
                    break;
                }
                case BINARY4: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit4)).setChecked((values[0] & 0x8) > 0);
                    break;
                }
                case BINARY5: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit5)).setChecked((values[0] & 0x4) > 0);
                    break;
                }
                case BINARY6: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit6)).setChecked((values[0] & 0x2) > 0);
                    break;
                }
                case BINARY7: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit7)).setChecked((values[0] & 0x1) > 0);
                    break;
                }
                case BYTE: {
                    ((EditText) view.findViewById(R.id.editTextByte)).setText(String.valueOf(signed ? values[0] : values[0] & 0xff));
                    break;
                }
                case WORD: {
                    int wordValue = signed
                            ? (byteOrder == ByteOrder.LITTLE_ENDIAN
                            ? (values[0] & 0xff) | (values[1] << 8)
                            : (values[1] & 0xff) | (values[0] << 8))
                            : (byteOrder == ByteOrder.LITTLE_ENDIAN
                            ? (values[0] & 0xff) | ((values[1] & 0xff) << 8)
                            : (values[1] & 0xff) | ((values[0] & 0xff) << 8));
                    ((EditText) view.findViewById(R.id.editTextWord)).setText(String.valueOf(wordValue));
                    break;
                }
                case INTEGER: {
                    long intValue = signed
                            ? (byteOrder == ByteOrder.LITTLE_ENDIAN
                            ? (values[0] & 0xffL) | ((values[1] & 0xffL) << 8) | ((values[2] & 0xffL) << 16) | (values[3] << 24)
                            : (values[3] & 0xffL) | ((values[2] & 0xffL) << 8) | ((values[1] & 0xffL) << 16) | (values[0] << 24))
                            : (byteOrder == ByteOrder.LITTLE_ENDIAN
                            ? (values[0] & 0xffL) | ((values[1] & 0xffL) << 8) | ((values[2] & 0xffL) << 16) | ((values[3] & 0xffL) << 24)
                            : (values[3] & 0xffL) | ((values[2] & 0xffL) << 8) | ((values[1] & 0xffL) << 16) | ((values[0] & 0xffL) << 24));
                    ((EditText) view.findViewById(R.id.editTextInteger)).setText(String.valueOf(intValue));
                    break;
                }
                case LONG: {
                    if (signed) {
                        byteBuffer.rewind();
                        if (byteBuffer.order() != byteOrder) {
                            byteBuffer.order(byteOrder);
                        }

                        ((EditText) view.findViewById(R.id.editTextLong)).setText(String.valueOf(byteBuffer.getLong()));
                    } else {
                        long longValue = byteOrder == ByteOrder.LITTLE_ENDIAN
                                ? (values[0] & 0xffL) | ((values[1] & 0xffL) << 8) | ((values[2] & 0xffL) << 16) | ((values[3] & 0xffL) << 24)
                                | ((values[4] & 0xffL) << 32) | ((values[5] & 0xffL) << 40) | ((values[6] & 0xffL) << 48)
                                : (values[7] & 0xffL) | ((values[6] & 0xffL) << 8) | ((values[5] & 0xffL) << 16) | ((values[4] & 0xffL) << 24)
                                | ((values[3] & 0xffL) << 32) | ((values[2] & 0xffL) << 40) | ((values[1] & 0xffL) << 48);
                        BigInteger bigInt1 = BigInteger.valueOf(values[byteOrder == ByteOrder.LITTLE_ENDIAN ? 7 : 0] & 0xffL);
                        BigInteger bigInt2 = bigInt1.shiftLeft(56);
                        BigInteger bigInt3 = bigInt2.add(BigInteger.valueOf(longValue));
                        ((EditText) view.findViewById(R.id.editTextLong)).setText(bigInt3.toString());
                    }
                    break;
                }
                case FLOAT: {
                    byteBuffer.rewind();
                    if (byteBuffer.order() != byteOrder) {
                        byteBuffer.order(byteOrder);
                    }

                    ((EditText) view.findViewById(R.id.editTextFloat)).setText(String.valueOf(byteBuffer.getFloat()));
                    break;
                }
                case DOUBLE: {
                    byteBuffer.rewind();
                    if (byteBuffer.order() != byteOrder) {
                        byteBuffer.order(byteOrder);
                    }

                    ((EditText) view.findViewById(R.id.editTextDouble)).setText(String.valueOf(byteBuffer.getDouble()));
                    break;
                }
                case CHARACTER: {
                    String strValue = new String(values, codeArea.getCharset());
                    if (!strValue.isEmpty()) {
                        ((EditText) view.findViewById(R.id.editTextCharacter)).setText(strValue.substring(0, 1));
                    } else {
                        ((EditText) view.findViewById(R.id.editTextCharacter)).setText("");
                    }
                    break;
                }
                case STRING: {
                    String strValue = new String(values, codeArea.getCharset());
                    for (int i = 0; i < strValue.length(); i++) {
                        char charAt = strValue.charAt(i);
                        if (charAt == '\r' || charAt == '\n' || charAt == 0) {
                            strValue = strValue.substring(0, i);
                            break;
                        }
                    }
                    ((EditText) view.findViewById(R.id.editTextString)).setText(strValue);
                    // TODO ((EditText) view.findViewById(R.id.editTextString)).setCaretPosition(0);
                    break;
                }
            }
        }

        private void clearField(ValuesPanelField valuesPanelField) {
            switch (valuesPanelField) {
                case BINARY0: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit0)).setChecked(false);
                    break;
                }
                case BINARY1: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit1)).setChecked(false);
                    break;
                }
                case BINARY2: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit2)).setChecked(false);
                    break;
                }
                case BINARY3: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit3)).setChecked(false);
                    break;
                }
                case BINARY4: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit4)).setChecked(false);
                    break;
                }
                case BINARY5: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit5)).setChecked(false);
                    break;
                }
                case BINARY6: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit6)).setChecked(false);
                    break;
                }
                case BINARY7: {
                    ((CheckBox) view.findViewById(R.id.checkBoxBit7)).setChecked(false);
                    break;
                }
                case BYTE: {
                    ((EditText) view.findViewById(R.id.editTextByte)).setText("");
                    break;
                }
                case WORD: {
                    ((EditText) view.findViewById(R.id.editTextWord)).setText("");
                    break;
                }
                case INTEGER: {
                    ((EditText) view.findViewById(R.id.editTextInteger)).setText("");
                    break;
                }
                case LONG: {
                    ((EditText) view.findViewById(R.id.editTextLong)).setText("");
                    break;
                }
                case FLOAT: {
                    ((EditText) view.findViewById(R.id.editTextFloat)).setText("");
                    break;
                }
                case DOUBLE: {
                    ((EditText) view.findViewById(R.id.editTextDouble)).setText("");
                    break;
                }
                case CHARACTER: {
                    ((EditText) view.findViewById(R.id.editTextCharacter)).setText("");
                    break;
                }
                case STRING: {
                    ((EditText) view.findViewById(R.id.editTextString)).setText("");
                    break;
                }
            }
        }

        private synchronized void updateStarted() {
            updateInProgress = true;
            scheduleUpdate = false;
        }

        private synchronized void stopUpdate() {
            updateInProgress = false;
            updateTerminated = false;
        }
    }

    @ParametersAreNonnullByDefault
    private class ValueFocusListener implements View.OnFocusChangeListener {

        private BasicValuesPositionColorModifier colorModifier;
        private int length;

        public ValueFocusListener(BasicValuesPositionColorModifier colorModifier, int length) {
            this.colorModifier = colorModifier;
            this.length = length;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                colorModifier.setRange(dataPosition, length);
                codeArea.repaint();
            } else {
                colorModifier.clearRange();
                codeArea.repaint();
            }
        }
    }
}
