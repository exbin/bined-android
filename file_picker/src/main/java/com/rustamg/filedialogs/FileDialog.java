package com.rustamg.filedialogs;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.widget.ActionMenuView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.rustamg.filedialogs.utils.KeyboardUtils;

import java.io.File;
import java.io.FileFilter;

import javax.annotation.Nullable;


/**
 * Created at 31/01/15 12:27
 *
 * @author rustamg
 */
public abstract class FileDialog extends DialogFragment implements FileListAdapter.OnFileSelectedListener {

    public static final String ROOT_DIRECTORY = "root_directory";
    public static final String START_DIRECTORY = "start_directory";
    public static final String EXTENSION = "extension";

    private static final String OUT_STATE_CURRENT_DIRECTORY = "out_state_current_dir";
    private static final String EXTERNAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath();

    protected File mCurrentDir;
    protected File mRootDir;
    protected FileFilter mFilesFilter;

    protected Toolbar mToolbar;
    protected ProgressBar mProgress;
    protected RecyclerView mRecyclerView;
    protected int mIconColor;

    private UpdateFilesTask mUpdateFilesTask;
    protected String mExtension;

    private String mRootPathDisplayName; // todo: create an argument for this


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        int[] iconColorAttr = new int[] { R.attr.file_dialog_toolbar_icons_color };
        int indexOfAttrIconColor = 0;
        TypedValue typedValue = new TypedValue();
        TypedArray a = activity.obtainStyledAttributes(typedValue.data, iconColorAttr);
        mIconColor = a.getColor(indexOfAttrIconColor, Color.WHITE);
        a.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        //        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(getLayoutResourceId(), container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_files);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mCurrentDir.getPath().equalsIgnoreCase(EXTERNAL_ROOT_PATH)) {
                    dismiss();
                }
                else {
                    mCurrentDir = mCurrentDir.getParentFile();
                    refresh();
                }
            }
        });

        extractArguments(savedInstanceState);

        initList();
    }

    protected void extractArguments(Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        mRootDir = arguments != null ? (File) arguments.getSerializable(ROOT_DIRECTORY) : null;

        if (savedInstanceState != null) {
            mCurrentDir = (File) savedInstanceState.getSerializable(OUT_STATE_CURRENT_DIRECTORY);
        }
        else {
            mCurrentDir = (File) (arguments != null ? arguments.getSerializable(START_DIRECTORY) : null);
        }

        if (mCurrentDir == null) {
            mCurrentDir = new File(EXTERNAL_ROOT_PATH);
        }

        if (mRootDir == null) {
            mRootDir = mCurrentDir;
        }

        if (arguments != null && arguments.containsKey(EXTENSION)) {
            mExtension = arguments.getString(EXTENSION);
            mFilesFilter = new ExtensionFilter(mExtension);
        }
    }

    private void initList() {

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onStart() {

        super.onStart();

        refresh();
    }

    public void refresh() {

        if (mUpdateFilesTask == null || mUpdateFilesTask.getStatus() == AsyncTask.Status.FINISHED) {

            mUpdateFilesTask = new UpdateFilesTask();
            mUpdateFilesTask.execute(mCurrentDir);
        }
    }

    @Override
    public void onStop() {

        super.onStop();
        mUpdateFilesTask.cancel(false);
    }

    @Override
    public void onFileSelected(File file) {

        if (file.isDirectory()) {
            mCurrentDir = file;
            refresh();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putSerializable(OUT_STATE_CURRENT_DIRECTORY, mCurrentDir);
    }

    protected void sendResult(File file) {

        Fragment targetFragment = getParentFragment();

        if (targetFragment != null && targetFragment instanceof OnFileSelectedListener) {
            ((OnFileSelectedListener) targetFragment).onFileSelected(this, file);
        }
        else {
            Activity activity = getActivity();
            if (activity != null && activity instanceof OnFileSelectedListener) {
                ((OnFileSelectedListener) activity).onFileSelected(this, file);
            }
        }

        dismiss();
    }

    protected abstract int getLayoutResourceId();

    private class UpdateFilesTask extends AsyncTask<File, Void, File[]> {

        private File[] mFileArray;
        private File mDirectory;

        private UpdateFilesTask() {

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected File[] doInBackground(File... files) {

            mDirectory = files[0];
            mFileArray = files[0].listFiles(mFilesFilter);

            return mFileArray;
        }

        @Override
        protected void onPostExecute(File[] localFiles) {

            super.onPostExecute(localFiles);

            if (!isCancelled() && getActivity() != null) {

                Drawable navIcon;

                if (mDirectory.getPath().equalsIgnoreCase(EXTERNAL_ROOT_PATH)) {

                    mToolbar.setTitle(mRootPathDisplayName);
                    navIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cross, null);
                }
                else {
                    mToolbar.setTitle(mCurrentDir.getName());
                    navIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cross, null);
                }

                navIcon.setColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);
                mToolbar.setNavigationIcon(navIcon);

                mRecyclerView.setAdapter(new FileListAdapter(getActivity(), localFiles, FileDialog.this));

                mProgress.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        if (getActivity() != null) {
            KeyboardUtils.hideKeyboard(getActivity());
        }
        super.onDismiss(dialog);
    }

    public interface OnFileSelectedListener {

        void onFileSelected(FileDialog dialog, File file);
    }
}
