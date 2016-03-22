package com.nissi.nissi.tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.nissi.nissi.R;

public class AlertDialogFragment extends DialogFragment {

    private String mTitle;
    private String mBody;
    private String mButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setMessage(mBody)
                .setPositiveButton(getResources().getString(R.string.ok_button), null);
        AlertDialog dialog = builder.create();
        return dialog;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public String getButton() {
        return mButton;
    }

    public void setButton(String button) {
        mButton = button;
    }
}
