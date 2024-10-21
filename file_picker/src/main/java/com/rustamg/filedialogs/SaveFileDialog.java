package com.rustamg.filedialogs;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import java.io.File;

import javax.annotation.Nullable;


/**
 * Created at 31/01/15 12:07
 *
 * @author rustamg
 */
public class SaveFileDialog extends FileDialog implements Toolbar.OnMenuItemClickListener {


    protected AppCompatEditText mFileNameText;

    @Override
    protected int getLayoutResourceId() {

        return R.layout.dialog_save_file;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        mFileNameText = (AppCompatEditText) view.findViewById(R.id.et_filename);

        mToolbar.inflateMenu(R.menu.dialog_save);
        mToolbar.getMenu().findItem(R.id.menu_apply).getIcon().setColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);
        mToolbar.setOnMenuItemClickListener(this);

        View saveButton = view.findViewById(R.id.button_save);
        saveButton.setOnClickListener(this::buttonSave);

        //        mFileNameText.addValidator(new FileNameValidator(getString(R.string.error_invalid_file_name),
//                getString(R.string.error_empty_field)));
    }

    @Override
    public void onFileSelected(final File file) {

        if (file.isFile()) {

            confirmOverwrite(file);
        }
        else {
            super.onFileSelected(file);
        }
    }

    private void confirmOverwrite(final File file) {

        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.confirm_overwrite_file)
                .setPositiveButton(R.string.label_button_overwrite, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendResult(file);
                    }
                })
                .setNegativeButton(R.string.label_button_cancel, null)
                .create().show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.menu_apply) { // mFileNameText.validate()

            String input = mFileNameText.getText().toString();
            String inputExtension = "";

            if(mExtension != null) {
                if (!input.endsWith("." + mExtension)) {
                    inputExtension = "." + mExtension;
                }
            }

            File result = new File(mCurrentDir, mFileNameText.getText() + inputExtension);


            if (result.exists()) {
                confirmOverwrite(result);
            }
            else {
                sendResult(result);
            }
        }

        return false;
    }

    public void buttonSave(View view) {
        String input = mFileNameText.getText().toString();
        String inputExtension = "";

        if(mExtension != null) {
            if (!input.endsWith("." + mExtension)) {
                inputExtension = "." + mExtension;
            }
        }

        File result = new File(mCurrentDir, mFileNameText.getText() + inputExtension);


        if (result.exists()) {
            confirmOverwrite(result);
        }
        else {
            sendResult(result);
        }
    }
}
