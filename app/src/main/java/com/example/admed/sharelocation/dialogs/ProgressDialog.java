package com.example.admed.sharelocation.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Window;

import com.example.admed.sharelocation.R;

/**
 * Created by Luis Eduardo on 02/09/2017.
 */

public class ProgressDialog extends DialogFragment {

    public ProgressDialog() {
    }

    public static ProgressDialog newInstance(Boolean cancelable) {
        ProgressDialog dialog = new ProgressDialog();
        dialog.setCancelable(cancelable);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_dialog, null));

        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }
}
