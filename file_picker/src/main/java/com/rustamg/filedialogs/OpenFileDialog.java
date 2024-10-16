package com.rustamg.filedialogs;

import java.io.File;

public class OpenFileDialog extends FileDialog {


    @Override
    protected int getLayoutResourceId() {

        return R.layout.dialog_open_file;
    }

    @Override
    public void onFileSelected(File file) {

        if (file.isFile()) {
            sendResult(file);
        } else {
            super.onFileSelected(file);
        }
    }
}
