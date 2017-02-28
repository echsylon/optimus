package com.echsylon.optimus.app;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.function.Supplier;

import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class can create suppliers that in turn can open up inputView and
 * outputView streams to read and write data to and from.
 */
@EBean(scope = EBean.Scope.Singleton)
class StreamSuppliers {
    private static final String FILE_SCHEME = "file";
    private static final String LOG_TAG = StreamSuppliers.class.getName();

    /**
     * Returns a supplier which in turn can produce an inputView stream to read
     * data from.
     *
     * @param context The context to read assets from.
     * @param uri     The uri describing the source.
     * @return Always an input stream supplier.
     */
    Supplier<InputStream> newInputStream(@NonNull final Context context,
                                         @NonNull final Uri uri) {
        return () -> {
            try {
                return context.getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                Log.d(LOG_TAG, "Couldn't open input stream: " + uri, e);
                return null;
            }
        };
    }

    /**
     * Returns a supplier which in turn can produce an outputView stream to
     * write data to.
     *
     * @param context The context to read assets from.
     * @param uri     The uri describing the destination.
     * @return An outputView stream or null if path is invalid or doesn't exist.
     */
    Supplier<OutputStream> newOutputStream(@NonNull final Context context,
                                           @NonNull final Uri uri) {
        return () -> {
            try {
                switch (uri.getScheme()) {
                    case FILE_SCHEME:
                        return new FileOutputStream(new File(uri.getPath()));
                    default:
                        throw new IllegalArgumentException("Unknown destination");
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "Couldn't open output stream: " + uri, e);
                return null;
            }
        };
    }

}
