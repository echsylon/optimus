package com.echsylon.optimus.app;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.androidannotations.annotations.EBean;

@EBean
class SnackbarHandler {
    private Snackbar snackbar;

    void showSnackbar(final View anchor, final int message) {
        showSnackbar(anchor, message, Snackbar.LENGTH_LONG, 0, null);
    }

    void showSnackbarForever(final View anchor, final int message) {
        showSnackbar(anchor, message, Snackbar.LENGTH_INDEFINITE, 0, null);
    }

    void showSnackbar(final View anchor, final int message, final Runnable retryAction) {
        showSnackbar(anchor, message, Snackbar.LENGTH_INDEFINITE, R.string.retry, retryAction);
    }

    private void showSnackbar(@NonNull final View anchor,
                              final int message,
                              final int length,
                              final int actionLabel,
                              final Runnable retryAction) {
        if (snackbar != null)
            snackbar.dismiss();

        if (retryAction == null) {
            snackbar = Snackbar.make(anchor, message, length);
            snackbar.show();
        } else {
            snackbar = Snackbar.make(anchor, message, length);
            snackbar.setAction(actionLabel, view -> retryAction.run());
            snackbar.show();
        }
    }

    void dismissSnackbar() {
        if (snackbar != null) {
            if (snackbar.isShown())
                snackbar.dismiss();
            snackbar = null;
        }
    }
}
