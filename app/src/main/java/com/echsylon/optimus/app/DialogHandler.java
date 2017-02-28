package com.echsylon.optimus.app;

import android.app.ProgressDialog;
import android.content.Context;

import org.androidannotations.annotations.EBean;

@EBean
public class DialogHandler {
    private ProgressDialog progressDialog;

    public void showProgressDialog(final Context context, final int message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(context, null, context.getText(message), true, false);
        } else {
            progressDialog.setMessage(context.getText(message));
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
