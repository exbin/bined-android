package org.exbin.bined.editor.android;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import org.exbin.bined.android.basic.CodeArea;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayEditableData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MainActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {

    private static final String EXAMPLE_FILE_PATH = "/org/exbin/bined/android/example/resources/lorem_1.txt";

    private CodeArea codeArea;
    private static ByteArrayEditableData fileData = null;

    private boolean storageReadPermissionGranted;
    private boolean storageWritePermissionGranted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.storageReadPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        this.storageWritePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        codeArea = findViewById(R.id.codeArea);

        if (fileData != null) {
            codeArea.setContentData(fileData);
        } else {
            ByteArrayEditableData basicData = new ByteArrayEditableData();
            try {
                basicData.loadFromStream(MainActivity.class.getResourceAsStream(EXAMPLE_FILE_PATH));
            } catch (IOException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }

            codeArea.setContentData(basicData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_open: {
                if (storageReadPermissionGranted) {
                    OpenFileDialog dialog = new OpenFileDialog();
                    dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                    dialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());
                } else {
                    showPermissionError();
                }

                return true;
            }

            case R.id.action_save: {
                if (storageWritePermissionGranted) {
                    FileDialog dialog = new SaveFileDialog();
                    dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                    dialog.show(getSupportFragmentManager(), SaveFileDialog.class.getName());
                } else {
                    showPermissionError();
                }

                return true;
            }

            case R.id.action_settings: {
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;
            }

            case R.id.action_about: {
                AboutDialog aboutDialog = new AboutDialog();
                aboutDialog.show(getSupportFragmentManager(), "aboutDialog");

                return true;
            }

            case R.id.action_exit: {
                // TODO
                System.exit(0);
                return true;
            }

//            case R.id.action_favorite:
//                // User chose the "Favorite" action, mark the current item
//                // as a favorite...
//                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showPermissionError() {
        Toast.makeText(this, "Storage permission is not granted", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        if (dialog instanceof OpenFileDialog) {
            fileData = new ByteArrayEditableData();
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileData.loadFromStream(fileInputStream);
                fileInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }

            codeArea.setContentData(fileData);
        } else {
            BinaryData contentData = codeArea.getContentData();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                contentData.saveToStream(fileOutputStream);
                fileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
