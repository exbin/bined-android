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
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.editor.android.R;
import org.exbin.bined.editor.android.RelativePositionMode;

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
    protected RelativePositionMode relativePositionMode = RelativePositionMode.FROM_START;

    protected CodeArea codeArea;

    public void setPositiveListener(DialogInterface.OnClickListener positionListener) {
        this.positionListener = positionListener;
    }

    public void initFromCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Nonnull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
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
        EditText currentPositionText =goToPositionView.findViewById(R.id.currentPositionText);
        currentPositionText.setText(String.valueOf(cursorPosition));
    }

    public void setMaxPosition(long maxPosition) {
        this.maxPosition = maxPosition;
        // TODO positionBaseSwitchableSpinnerPanel.setMaximum(maxPosition);
        updateTargetPosition();
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

    private long getPositionValue() {
        try {
            EditText positionText = goToPositionView.findViewById(R.id.positionText);
            return Long.parseLong(positionText.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void setPositionValue(long value) {
        EditText positionText = goToPositionView.findViewById(R.id.positionText);
        positionText.setText(String.valueOf(value));
        updateTargetPosition();
    }

    private void updateTargetPosition() {
        EditText targetPositionText = goToPositionView.findViewById(R.id.targetPositionText);
        targetPositionText.setText(String.valueOf(getTargetPosition()));
    }
}
