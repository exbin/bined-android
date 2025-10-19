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
package org.exbin.bined.editor.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.editor.android.R;
import org.exbin.bined.editor.android.RelativePositionMode;
import org.exbin.bined.editor.android.SwitchableBase;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Edit selection dialog.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditSelectionDialog extends AppCompatDialogFragment {

    protected DialogInterface.OnClickListener positionListener;
    protected View editSelectionView;

    protected volatile boolean activeUpdate = false;
    protected long cursorPosition;
    protected long maxPosition;
    protected final SwitchableBase startSwitchableBase = new SwitchableBase();
    protected RelativePositionMode startPosMode = RelativePositionMode.FROM_START;
    protected final SwitchableBase endSwitchableBase = new SwitchableBase();
    protected RelativePositionMode endPosMode = RelativePositionMode.FROM_START;
    protected final SwitchableBase lengthSwitchableBase = new SwitchableBase();

    protected CodeArea codeArea;

    public void setPositiveListener(DialogInterface.OnClickListener positionListener) {
        this.positionListener = positionListener;
    }

    @Nonnull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        codeArea = activity.findViewById(R.id.codeArea);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.edit_selection));
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        editSelectionView = inflater.inflate(R.layout.edit_selection_view, null);

        setCursorPosition(codeArea.getDataPosition());
        setMaxPosition(codeArea.getDataSize());
        setSelectionRange(codeArea.getSelection());
        {
            final EditText inputNumber = editSelectionView.findViewById(R.id.startPositionText);
            inputNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!activeUpdate) {
                        updateStartTargetPosition();
                    }
                }
            });
        }
        {
            final EditText inputNumber = editSelectionView.findViewById(R.id.endPositionText);
            inputNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!activeUpdate) {
                        updateEndTargetPosition();
                    }
                }
            });
        }
        {
            final EditText inputNumber = editSelectionView.findViewById(R.id.selectionLengthText);
            inputNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!activeUpdate) {
                        updateLengthTargetPosition();
                    }
                }
            });
        }

        {
            final RadioButton fromStart = editSelectionView.findViewById(R.id.startFromStartRadioButton);
            fromStart.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) switchStartPositionMode(RelativePositionMode.FROM_START);
            });
            final RadioButton fromEnd = editSelectionView.findViewById(R.id.startFromEndRadioButton);
            fromEnd.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    switchStartPositionMode(RelativePositionMode.FROM_END);
                }
            });
            final RadioButton relativeToCursor = editSelectionView.findViewById(R.id.startRelativeToCursorRadioButton);
            relativeToCursor.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    switchStartPositionMode(RelativePositionMode.FROM_CURSOR);
                }
            });
        }
        {
            final RadioButton fromStart = editSelectionView.findViewById(R.id.endFromStartRadioButton);
            fromStart.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) switchEndPositionMode(RelativePositionMode.FROM_START);
            });
            final RadioButton fromEnd = editSelectionView.findViewById(R.id.endFromEndRadioButton);
            fromEnd.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    switchEndPositionMode(RelativePositionMode.FROM_END);
                }
            });
            final RadioButton relativeToCursor = editSelectionView.findViewById(R.id.endRelativeToCursorRadioButton);
            relativeToCursor.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    switchEndPositionMode(RelativePositionMode.FROM_CURSOR);
                }
            });
        }

        {
            Button positionTypeButton = editSelectionView.findViewById(R.id.startPositionTypeButton);
            positionTypeButton.setOnClickListener(view -> {
                selectNumBase(startSwitchableBase.getCodeType(), this::switchStartPositionNumBase);
            });
        }
        {
            Button positionTypeButton = editSelectionView.findViewById(R.id.endPositionTypeButton);
            positionTypeButton.setOnClickListener(view -> {
                selectNumBase(endSwitchableBase.getCodeType(), this::switchEndPositionNumBase);
            });
        }
        {
            Button positionTypeButton = editSelectionView.findViewById(R.id.selectionLengthTypeButton);
            positionTypeButton.setOnClickListener(view -> {
                selectNumBase(lengthSwitchableBase.getCodeType(), this::switchLengthPositionNumBase);
            });
        }
        initFocus();

        builder.setView(editSelectionView);
        builder.setPositiveButton(R.string.button_set, positionListener);
        builder.setNegativeButton(R.string.button_cancel, null);
        return builder.create();
    }

    public long getStartTargetPosition() {
        long absolutePosition;
        long position = getStartPositionValue();
        switch (startPosMode) {
            case FROM_START:
                absolutePosition = position;
                break;
            case FROM_END:
                absolutePosition = maxPosition - position;
                break;
            case FROM_CURSOR:
                absolutePosition = cursorPosition + position;
                break;
            default:
                throw CodeAreaUtils.getInvalidTypeException(startPosMode);
        }

        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        return absolutePosition;
    }

    public long getEndTargetPosition() {
        long absolutePosition;
        long position = getEndPositionValue();
        switch (endPosMode) {
            case FROM_START:
                absolutePosition = position;
                break;
            case FROM_END:
                absolutePosition = maxPosition - position;
                break;
            case FROM_CURSOR:
                absolutePosition = cursorPosition + position;
                break;
            default:
                throw CodeAreaUtils.getInvalidTypeException(endPosMode);
        }

        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        return absolutePosition;
    }

    public void setStartTargetPosition(long absolutePosition) {
        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        switch (startPosMode) {
            case FROM_START:
                setStartPositionValue(absolutePosition);
                break;
            case FROM_END:
                setStartPositionValue(maxPosition - absolutePosition);
                break;
            case FROM_CURSOR:
                setStartPositionValue(absolutePosition - cursorPosition);
                break;
            default:
                throw CodeAreaUtils.getInvalidTypeException(startPosMode);
        }
        updateStartTargetPosition();
    }

    public void setEndTargetPosition(long absolutePosition) {
        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        switch (endPosMode) {
            case FROM_START:
                setEndPositionValue(absolutePosition);
                break;
            case FROM_END:
                setEndPositionValue(maxPosition - absolutePosition);
                break;
            case FROM_CURSOR:
                setEndPositionValue(absolutePosition - cursorPosition);
                break;
            default:
                throw CodeAreaUtils.getInvalidTypeException(endPosMode);
        }
        updateEndTargetPosition();
    }

    public long getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(long cursorPosition) {
        this.cursorPosition = cursorPosition;
        EditText currentPositionText = editSelectionView.findViewById(R.id.currentPositionPreview);
        currentPositionText.setText(String.valueOf(cursorPosition));
    }

    public void setMaxPosition(long maxPosition) {
        this.maxPosition = maxPosition;
        // TODO positionBaseSwitchableSpinnerPanel.setMaximum(maxPosition);
        updateStartTargetPosition();
    }

    private void updateStartTargetPosition() {
        EditText startPositionPreview = editSelectionView.findViewById(R.id.startPositionPreview);
        startPositionPreview.setText(String.valueOf(getStartTargetPosition()));
    }

    private void updateEndTargetPosition() {
        EditText endPositionPreview = editSelectionView.findViewById(R.id.endPositionPreview);
        endPositionPreview.setText(String.valueOf(getEndTargetPosition()));
    }

    private void updateLengthTargetPosition() {
        activeUpdate = true;
        long endPosition = getStartPositionValue() + getSelectionLengthValue();
        EditText positionText = editSelectionView.findViewById(R.id.endPositionText);
        positionText.setText(endSwitchableBase.getPositionAsString(endPosition));
        activeUpdate = false;
        updateEndTargetPosition();
    }

    public void initFocus() {
        EditText startPositionText = editSelectionView.findViewById(R.id.startPositionText);
        startPositionText.requestFocus();
        startPositionText.selectAll();
    }

    @Nonnull
    public Optional<SelectionRange> getSelectionRange() {
        return Optional.of(new SelectionRange(getStartTargetPosition(), getEndTargetPosition()));
    }

    public void setSelectionRange(@Nullable SelectionRange selection) {
        if (selection == null) {
            setStartTargetPosition(0);
            setEndTargetPosition(0);
            setSelectionLengthValue(0);
        } else {
            setStartTargetPosition(selection.getStart());
            setEndTargetPosition(selection.getEnd());
            setSelectionLengthValue(selection.getLength());
        }
    }

    private void switchStartPositionMode(RelativePositionMode positionMode) {
        if (this.startPosMode == positionMode) {
            return;
        }

        long absolutePosition = getStartTargetPosition();
        this.startPosMode = positionMode;
        switch (positionMode) {
            case FROM_START:
            case FROM_END: {
                setStartPositionValue(0L);
//                startPositionBaseSwitchableSpinnerPanel.setMinimum(0L);
//                startPositionBaseSwitchableSpinnerPanel.setMaximum(maxPosition);
//                startPositionBaseSwitchableSpinnerPanel.revalidateSpinner();
                break;
            }
            case FROM_CURSOR: {
                setStartPositionValue(0L);
//                startPositionBaseSwitchableSpinnerPanel.setMinimum(-cursorPosition);
//                startPositionBaseSwitchableSpinnerPanel.setMaximum(maxPosition - cursorPosition);
//                startPositionBaseSwitchableSpinnerPanel.revalidateSpinner();
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(positionMode);
        }
        setStartTargetPosition(absolutePosition);
    }

    private void switchEndPositionMode(RelativePositionMode positionMode) {
        if (this.endPosMode == positionMode) {
            return;
        }

        long absolutePosition = getEndTargetPosition();
        this.endPosMode = positionMode;
        switch (positionMode) {
            case FROM_START:
            case FROM_END: {
                setEndPositionValue(0L);
//                startPositionBaseSwitchableSpinnerPanel.setMinimum(0L);
//                startPositionBaseSwitchableSpinnerPanel.setMaximum(maxPosition);
//                startPositionBaseSwitchableSpinnerPanel.revalidateSpinner();
                break;
            }
            case FROM_CURSOR: {
                setEndPositionValue(0L);
//                startPositionBaseSwitchableSpinnerPanel.setMinimum(-cursorPosition);
//                startPositionBaseSwitchableSpinnerPanel.setMaximum(maxPosition - cursorPosition);
//                startPositionBaseSwitchableSpinnerPanel.revalidateSpinner();
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(positionMode);
        }
        setEndTargetPosition(absolutePosition);
    }

    private void selectNumBase(PositionCodeType codeType, PositionCodeTypeSwitcher codeTypeSwitcher) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.code_type);

        CharSequence[] numBases = Arrays.copyOfRange(getResources().getTextArray(R.array.code_type_entries), 1, 4);
        builder.setSingleChoiceItems(numBases, codeType.ordinal(), (dialog, which) -> {
            PositionCodeType targetCodeType = PositionCodeType.values()[which];
            codeTypeSwitcher.switchCodeType(targetCodeType);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void switchStartPositionNumBase(PositionCodeType codeType) {
        long value = getStartPositionValue();
        CharSequence[] textArray = getResources().getTextArray(R.array.code_type_short);
        Button positionTypeText = editSelectionView.findViewById(R.id.startPositionTypeButton);
        positionTypeText.setText(textArray[codeType.ordinal() + 1]);
        startSwitchableBase.setCodeType(codeType);
        setStartPositionValue(value);
        EditText positionText = editSelectionView.findViewById(R.id.startPositionText);
        if (codeType == PositionCodeType.HEXADECIMAL) {
            positionText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            switch (startPosMode) {
                case FROM_START:
                case FROM_END: {
                    positionText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                }
                case FROM_CURSOR: {
                    positionText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(startPosMode);
            }
        }
    }

    private void switchEndPositionNumBase(PositionCodeType codeType) {
        long value = getEndPositionValue();
        CharSequence[] textArray = getResources().getTextArray(R.array.code_type_short);
        Button positionTypeText = editSelectionView.findViewById(R.id.endPositionTypeButton);
        positionTypeText.setText(textArray[codeType.ordinal() + 1]);
        endSwitchableBase.setCodeType(codeType);
        setEndPositionValue(value);
        EditText positionText = editSelectionView.findViewById(R.id.endPositionText);
        if (codeType == PositionCodeType.HEXADECIMAL) {
            positionText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            switch (endPosMode) {
                case FROM_START:
                case FROM_END: {
                    positionText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                }
                case FROM_CURSOR: {
                    positionText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(endPosMode);
            }
        }
    }

    private void switchLengthPositionNumBase(PositionCodeType codeType) {
        long value = getSelectionLengthValue();
        CharSequence[] textArray = getResources().getTextArray(R.array.code_type_short);
        Button positionTypeText = editSelectionView.findViewById(R.id.selectionLengthTypeButton);
        positionTypeText.setText(textArray[codeType.ordinal() + 1]);
        lengthSwitchableBase.setCodeType(codeType);
        setSelectionLengthValue(value);
        EditText positionText = editSelectionView.findViewById(R.id.endPositionText);
        if (codeType == PositionCodeType.HEXADECIMAL) {
            positionText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            switch (endPosMode) {
                case FROM_START:
                case FROM_END: {
                    positionText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                }
                case FROM_CURSOR: {
                    positionText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(endPosMode);
            }
        }
    }

    private long getStartPositionValue() {
        try {
            EditText positionText = editSelectionView.findViewById(R.id.startPositionText);
            return startSwitchableBase.valueOfPosition(positionText.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private long getEndPositionValue() {
        try {
            EditText positionText = editSelectionView.findViewById(R.id.endPositionText);
            return endSwitchableBase.valueOfPosition(positionText.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private long getSelectionLengthValue() {
        try {
            EditText positionText = editSelectionView.findViewById(R.id.selectionLengthText);
            return lengthSwitchableBase.valueOfPosition(positionText.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void setStartPositionValue(long value) {
        EditText positionText = editSelectionView.findViewById(R.id.startPositionText);
        positionText.setText(startSwitchableBase.getPositionAsString(value));
        setSelectionLengthValue(getEndPositionValue() - value);
        updateStartTargetPosition();
    }

    private void setEndPositionValue(long value) {
        EditText positionText = editSelectionView.findViewById(R.id.endPositionText);
        positionText.setText(endSwitchableBase.getPositionAsString(value));
        setSelectionLengthValue(value - getStartPositionValue());
        updateEndTargetPosition();
    }

    private void setSelectionLengthValue(long value) {
        EditText positionText = editSelectionView.findViewById(R.id.selectionLengthText);
        positionText.setText(lengthSwitchableBase.getPositionAsString(value));
    }

    private interface PositionCodeTypeSwitcher {
        void switchCodeType(PositionCodeType positionCodeType);
    }
}
