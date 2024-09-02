package org.exbin.bined.editor.android;

import android.Manifest;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.basic.CodeArea;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.highlight.android.HighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.operation.android.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.android.CodeAreaUndoRedo;
import org.exbin.framework.bined.BinaryStatusApi;

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

    private static final int CUT_ITEM_ID = 1;
    private static final int COPY_ITEM_ID = 2;
    private static final int PASTE_ITEM_ID = 3;
    private static final int DELETE_ITEM_ID = 4;
    private static final int SELECT_ALL_ITEM_ID = 5;

    private CodeArea codeArea;
    private CodeAreaUndoRedo undoRedo;

    private long documentOriginalSize = 0;
    private Menu menu;
    private static ByteArrayEditableData fileData = null;
    private final BinaryStatusHandler binaryStatus = new BinaryStatusHandler(this);

    private boolean storageReadPermissionGranted;
    private boolean storageWritePermissionGranted;
    private boolean keyboardShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.storageReadPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        this.storageWritePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT > 32) {
            storageReadPermissionGranted = true;
            storageWritePermissionGranted = true;
        }

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

        undoRedo = new CodeAreaUndoRedo(codeArea);
        undoRedo.addChangeListener(() -> {
            if (menu != null) {
                menu.findItem(R.id.app_bar_undo).setEnabled(undoRedo.canUndo());
                menu.findItem(R.id.app_bar_redo).setEnabled(undoRedo.canRedo());
            }
//            View undoAction = findViewById(R.id.app_bar_undo);
//            if (undoAction != null) {
//                undoAction.setEnabled(undoRedo.canUndo());
//            }
//            View redoAction = findViewById(R.id.app_bar_redo);
//            if (redoAction != null) {
//                redoAction.setEnabled(undoRedo.canRedo());
//            }
        });

        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea.getContext(), codeArea, undoRedo);
        codeArea.setCommandHandler(commandHandler);
        codeArea.setPainter(new HighlightNonAsciiCodeAreaPainter(codeArea));

        registerForContextMenu(codeArea);

        if (fileData != null) {
            codeArea.setContentData(fileData);
        } else {
            ByteArrayEditableData basicData = new ByteArrayEditableData();
            /* // Load sample data
            try {
                basicData.loadFromStream(MainActivity.class.getResourceAsStream(EXAMPLE_FILE_PATH));
            } catch (IOException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            } */

            codeArea.setContentData(basicData);
        }

        codeArea.addDataChangedListener(() -> {
//            activeFile.getComponent().notifyDataChanged();
//            if (editorModificationListener != null) {
//                editorModificationListener.modified();
//            }
            updateCurrentDocumentSize();
        });

        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
//            updateClipboardActionsStatus();
        });

        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);

            boolean showKeyboard = codeArea.getActiveSection() == BasicCodeAreaSection.TEXT_PREVIEW;
            if (showKeyboard != keyboardShown) {

                keyboardShown = showKeyboard;
                InputMethodManager im = (InputMethodManager)codeArea.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                codeArea.requestFocus();
                if (showKeyboard) {
                    im.showSoftInput(codeArea, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    im.hideSoftInputFromWindow(codeArea.getWindowToken(), 0);
                }
            }
        });

        codeArea.setOnKeyListener(new View.OnKeyListener() {

            private KeyListener keyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
            private Editable editable = Editable.Factory.getInstance().newEditable("");

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    keyListener.onKeyDown(view, editable, keyCode, keyEvent);
                    processKeys(keyEvent);
                } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    commandHandler.keyPressed(keyEvent);
                } else {
                    keyListener.onKeyOther(view, editable, keyEvent);
                    processKeys(keyEvent);
                }
                return true;
            }

            private void processKeys(KeyEvent keyEvent) {
                int outputCharsLength = editable.length();
                if (outputCharsLength > 0) {
                    for (int i = 0; i < outputCharsLength; i++) {
                        commandHandler.keyTyped(editable.charAt(i), keyEvent);
                    }
                    editable.clear();
                }
            }
        });

        codeArea.addEditModeChangedListener((EditMode mode, EditOperation operation) -> {
            binaryStatus.setEditMode(mode, operation);
        });

        binaryStatus.setEncoding(codeArea.getCharset().toString());

        updateStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        this.menu = menu;
        menu.findItem(R.id.app_bar_undo).setEnabled(undoRedo.canUndo());
        menu.findItem(R.id.app_bar_redo).setEnabled(undoRedo.canRedo());

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuItem cutMenuItem = menu.add(0, CUT_ITEM_ID, 0, "Cut");
        cutMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
        MenuItem copyMenuItem = menu.add(0, COPY_ITEM_ID, 1, "Copy");
        copyMenuItem.setEnabled(codeArea.hasSelection());
        MenuItem pasteMenuItem = menu.add(0, PASTE_ITEM_ID, 2, "Paste");
        pasteMenuItem.setEnabled(codeArea.isEditable() && codeArea.canPaste());
        MenuItem deleteMenuItem = menu.add(0, DELETE_ITEM_ID, 3, "Delete");
        deleteMenuItem.setEnabled(codeArea.isEditable() && codeArea.hasSelection());
        MenuItem selectAllMenuItem = menu.add(0, SELECT_ALL_ITEM_ID, 4, "Select All");
    }

    // menu item select listener
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CUT_ITEM_ID: {
                codeArea.cut();
                break;
            }
            case COPY_ITEM_ID: {
                codeArea.copy();
                break;
            }
            case PASTE_ITEM_ID: {
                codeArea.paste();
                break;
            }
            case DELETE_ITEM_ID: {
                codeArea.delete();
                break;
            }
            case SELECT_ALL_ITEM_ID: {
                codeArea.selectAll();
                break;
            }

            default: {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_new: {
                // TODO Release file

                codeArea.setContentData(new ByteArrayEditableData());
                undoRedo.clear();

                documentOriginalSize = 0;
                return true;
            }

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

            case R.id.code_type: {
                // TODO
                return true;
            }

            case R.id.view_mode: {
                // TODO
                return true;
            }

            case R.id.hex_chars_case: {
                // TODO
                return true;
            }

            case R.id.app_bar_undo: {
                undoRedo.performUndo();
                return true;
            }

            case R.id.app_bar_redo: {
                undoRedo.performRedo();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showPermissionError() {
        Toast.makeText(this, "Storage permission is not granted", Toast.LENGTH_LONG).show();
    }

    public void updateStatus() {
        updateCurrentDocumentSize();
        updateCurrentCaretPosition();
        updateCurrentSelectionRange();
        updateCurrentMemoryMode();
        updateCurrentEditMode();
    }

    private void updateCurrentDocumentSize() {
        if (binaryStatus == null) {
            return;
        }

        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    private void updateCurrentCaretPosition() {
        if (binaryStatus == null) {
            return;
        }

        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        binaryStatus.setCursorPosition(caretPosition);
    }

    private void updateCurrentSelectionRange() {
        if (binaryStatus == null) {
            return;
        }

        SelectionRange selectionRange = codeArea.getSelection();
        binaryStatus.setSelectionRange(selectionRange);
    }

    private void updateCurrentMemoryMode() {
        if (binaryStatus == null) {
            return;
        }

        BinaryStatusApi.MemoryMode newMemoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
        if (((EditModeCapable) codeArea).getEditMode() == EditMode.READ_ONLY) {
            newMemoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } /* else if (codeArea.getContentData() instanceof DeltaDocument) {
            newMemoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        } */

        binaryStatus.setMemoryMode(newMemoryMode);
    }

    private void updateCurrentEditMode() {
        if (binaryStatus == null) {
            return;
        }

        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());
    }

    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        if (dialog instanceof OpenFileDialog) {
            fileData = new ByteArrayEditableData();
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileData.loadFromStream(fileInputStream);
                fileInputStream.close();
                documentOriginalSize = fileData.getDataSize();
                undoRedo.clear();
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
                documentOriginalSize = contentData.getDataSize();
                undoRedo.setSyncPosition();
            } catch (IOException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void buttonAction0(View view) {
        codeArea.getCommandHandler().keyTyped('0', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0));
    }

    public void buttonAction1(View view) {
        codeArea.getCommandHandler().keyTyped('1', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1));
    }

    public void buttonAction2(View view) {
        codeArea.getCommandHandler().keyTyped('2', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_2));
    }

    public void buttonAction3(View view) {
        codeArea.getCommandHandler().keyTyped('3', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_3));
    }

    public void buttonAction4(View view) {
        codeArea.getCommandHandler().keyTyped('4', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_4));
    }

    public void buttonAction5(View view) {
        codeArea.getCommandHandler().keyTyped('5', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_5));
    }

    public void buttonAction6(View view) {
        codeArea.getCommandHandler().keyTyped('6', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_6));
    }

    public void buttonAction7(View view) {
        codeArea.getCommandHandler().keyTyped('7', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_7));
    }

    public void buttonAction8(View view) {
        codeArea.getCommandHandler().keyTyped('8', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0));
    }

    public void buttonAction9(View view) {
        codeArea.getCommandHandler().keyTyped('1', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1));
    }

    public void buttonActionA(View view) {
        codeArea.getCommandHandler().keyTyped('a', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A));
    }

    public void buttonActionB(View view) {
        codeArea.getCommandHandler().keyTyped('b', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B));
    }

    public void buttonActionC(View view) {
        codeArea.getCommandHandler().keyTyped('c', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_C));
    }

    public void buttonActionD(View view) {
        codeArea.getCommandHandler().keyTyped('d', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_D));
    }

    public void buttonActionE(View view) {
        codeArea.getCommandHandler().keyTyped('e', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_E));
    }

    public void buttonActionF(View view) {
        codeArea.getCommandHandler().keyTyped('f', new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F));
    }

    public void buttonActionUp(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
    }

    public void buttonActionDown(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
    }

    public void buttonActionLeft(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
    }

    public void buttonActionRight(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
    }

    public void buttonActionHome(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
    }

    public void buttonActionEnd(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
    }

    public void buttonActionInsert(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_INSERT));
    }

    public void buttonActionDelete(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    public void buttonActionBk(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
    }

    public void buttonActionTab(View view) {
        codeArea.getCommandHandler().keyPressed(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB));
    }
}
