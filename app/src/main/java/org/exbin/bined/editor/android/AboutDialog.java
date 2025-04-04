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
package org.exbin.bined.editor.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * About application dialog.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class AboutDialog extends AppCompatDialogFragment {

    private static final String DONATE_LINK = "https://bined.exbin.org/?p=donate";

    private String appVersion;

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Nonnull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.application_about));
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        View aboutView = inflater.inflate(R.layout.about_view, null);
        TextView textView = aboutView.findViewById(R.id.textView);
        String htmlString = String.format(getResources().getString(R.string.app_about), appVersion).replace("\n", "<br/>");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT));
            } else {
                textView.setText(Html.fromHtml(htmlString));
            }
        } catch (NoSuchMethodError ex) {
            textView.setText(htmlString, TextView.BufferType.SPANNABLE);
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setView(aboutView);
        builder.setPositiveButton(getResources().getString(R.string.button_close), (dialog, which) -> {
        });
        builder.setNeutralButton(getResources().getString(R.string.button_donate), (dialog, which) -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_LINK));
            startActivity(browserIntent);
        });
        return builder.create();
    }
}
