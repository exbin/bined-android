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
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.editor.android.R;
import org.exbin.bined.editor.android.RelativePositionMode;
import org.exbin.bined.editor.android.SwitchableBase;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Go to position dialog.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class GoToPositionDialog extends AppCompatDialogFragment {

    protected DialogInterface.OnClickListener positionListener;
    protected View goToPositionView;

    protected long cursorPosition;
    protected long maxPosition;
    protected final SwitchableBase positionSwitchableBase = new SwitchableBase();
    protected RelativePositionMode relativePositionMode = RelativePositionMode.FROM_START;

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
        builder.setTitle(getResources().getString(R.string.go_to_position));
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        goToPositionView = inflater.inflate(R.layout.go_to_position_view, null);

        setCursorPosition(codeArea.getDataPosition());
        setMaxPosition(codeArea.getDataSize());
        final EditText inputNumber = goToPositionView.findViewById(R.id.positionText);
        inputNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputNumber.setText(String.valueOf(codeArea.getDataPosition()));
        inputNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateTargetPosition();
            }
        });

        final RadioButton fromStart = goToPositionView.findViewById(R.id.positionFromStartRadioButton);
        fromStart.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) switchRelativePositionMode(RelativePositionMode.FROM_START);
        });
        final RadioButton fromEnd = goToPositionView.findViewById(R.id.positionFromEndRadioButton);
        fromEnd.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                switchRelativePositionMode(RelativePositionMode.FROM_END);
            }
        });
        final RadioButton relativeToCursor = goToPositionView.findViewById(R.id.positionRelativeToCursorRadioButton);
        relativeToCursor.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                switchRelativePositionMode(RelativePositionMode.FROM_CURSOR);
            }
        });

        Button positionTypeButton = goToPositionView.findViewById(R.id.positionTypeButton);
        positionTypeButton.setOnClickListener(view -> {
            selectNumBase(positionSwitchableBase.getCodeType());
        });
        initFocus();

        builder.setView(goToPositionView);
        builder.setPositiveButton(R.string.button_go_to, positionListener);
        builder.setNegativeButton(R.string.button_cancel, null);
        return builder.create();
    }

    public long getTargetPosition() {
        long absolutePosition;
        long position = getPositionValue();
        switch (relativePositionMode) {
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
                throw CodeAreaUtils.getInvalidTypeException(relativePositionMode);
        }

        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        return absolutePosition;
    }

    public void setTargetPosition(long absolutePosition) {
        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        switch (relativePositionMode) {
            case FROM_START:
                setPositionValue(absolutePosition);
                break;
            case FROM_END:
                setPositionValue(maxPosition - absolutePosition);
                break;
            case FROM_CURSOR:
                setPositionValue(absolutePosition - cursorPosition);
                break;
            default:
                throw CodeAreaUtils.getInvalidTypeException(relativePositionMode);
        }
        updateTargetPosition();
    }

    public long getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(long cursorPosition) {
        this.cursorPosition = cursorPosition;
        setPositionValue(cursorPosition);
        EditText currentPositionText =goToPositionView.findViewById(R.id.currentPositionPreview);
        currentPositionText.setText(String.valueOf(cursorPosition));
    }

    public void setMaxPosition(long maxPosition) {
        this.maxPosition = maxPosition;
        // TODO positionBaseSwitchableSpinnerPanel.setMaximum(maxPosition);
        updateTargetPosition();
    }

    public void initFocus() {
        EditText positionText = goToPositionView.findViewById(R.id.positionText);
        positionText.requestFocus();
        positionText.selectAll();
    }

    private void switchRelativePositionMode(RelativePositionMode relativePositionMode) {
        if (this.relativePositionMode == relativePositionMode) {
            return;
        }

        long absolutePosition = getTargetPosition();
        this.relativePositionMode = relativePositionMode;
        EditText positionText = goToPositionView.findViewById(R.id.positionText);
        switch (relativePositionMode) {
            case FROM_START:
            case FROM_END: {
                setPositionValue(0L);
//                positionBaseSwitchableSpinnerPanel.setMinimum(0L);
//                positionBaseSwitchableSpinnerPanel.setMaximum(maxPosition);
//                positionBaseSwitchableSpinnerPanel.revalidateSpinner();
                positionText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            }
            case FROM_CURSOR: {
                setPositionValue(0L);
//                positionBaseSwitchableSpinnerPanel.setMinimum(-cursorPosition);
//                positionBaseSwitchableSpinnerPanel.setMaximum(maxPosition - cursorPosition);
//                positionBaseSwitchableSpinnerPanel.revalidateSpinner();
                positionText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(relativePositionMode);
        }
        setTargetPosition(absolutePosition);
    }

    private void selectNumBase(PositionCodeType codeType) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.code_type);

        CharSequence[] numBases = Arrays.copyOfRange(getResources().getTextArray(R.array.code_type_entries), 1, 4);
        builder.setSingleChoiceItems(numBases, codeType.ordinal(), (dialog, which) -> {
            PositionCodeType targetCodeType = PositionCodeType.values()[which];
            switchPositionNumBase(targetCodeType);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void switchPositionNumBase(PositionCodeType codeType) {
        long value = getPositionValue();
        CharSequence[] textArray = getResources().getTextArray(R.array.code_type_short);
        Button positionTypeText = goToPositionView.findViewById(R.id.positionTypeButton);
        positionTypeText.setText(textArray[codeType.ordinal() + 1]);
        positionSwitchableBase.setCodeType(codeType);
        setPositionValue(value);
        EditText positionText = goToPositionView.findViewById(R.id.positionText);
        if (codeType == PositionCodeType.HEXADECIMAL) {
            positionText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            switch (relativePositionMode) {
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
                    throw CodeAreaUtils.getInvalidTypeException(relativePositionMode);
            }
        }
    }

    private long getPositionValue() {
        try {
            EditText positionText = goToPositionView.findViewById(R.id.positionText);
            return positionSwitchableBase.valueOfPosition(positionText.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void setPositionValue(long value) {
        EditText positionText = goToPositionView.findViewById(R.id.positionText);
        positionText.setText(positionSwitchableBase.getPositionAsString(value));
        updateTargetPosition();
    }

    private void updateTargetPosition() {
        EditText targetPositionPreview = goToPositionView.findViewById(R.id.targetPositionPreview);
        targetPositionPreview.setText(String.valueOf(getTargetPosition()));
    }
}
