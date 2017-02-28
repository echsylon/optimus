package com.echsylon.optimus.app;

import android.util.Log;

import com.annimon.stream.Stream;

import java.io.Closeable;

/**
 * This is the mandatory utils class.
 */
class Utils {
    private static final String LOG_TAG = Utils.class.getName();

    /**
     * Tries to gracefully close a closeable. This method will consume any
     * thrown exceptions by printing them to the log.
     *
     * @param closeables The closeables to close.
     */
    static void closeSilently(Closeable... closeables) {
        if (closeables != null)
            Stream.of(closeables)
                    .filter(closeable -> closeable != null)
                    .forEach(closeable -> {
                        try {
                            closeable.close();
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Couldn't close closeable: ", e);
                        }
                    });
    }

}
