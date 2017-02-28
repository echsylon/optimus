package com.echsylon.optimus.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.IOException;

import static com.echsylon.optimus.app.Utils.closeSilently;


public class AssetProvider extends ContentProvider {
    private final static String LOG_TAG = AssetProvider.class.getName();
    private static final String[] COLUMNS = {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Shameless copy/paste from: FileProvider#query(Uri, String[], String, String[], String).
        AssetFileDescriptor assetFileDescriptor = null;
        long fileSize = 0;

        try {
            assetFileDescriptor = getAssetFileDescriptor(uri);
            fileSize = assetFileDescriptor.getLength();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Can't measure file size: " + uri, e);
        } finally {
            closeSilently(assetFileDescriptor);
        }

        if (projection == null)
            projection = COLUMNS;

        final String[] cols = new String[projection.length];
        final Object[] values = new Object[projection.length];
        int i = 0;

        for (String column : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(column)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = uri.getLastPathSegment();
            } else if (OpenableColumns.SIZE.equals(column)) {
                cols[i] = OpenableColumns.SIZE;
                values[i++] = fileSize;
            }
        }

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Shameless copy/paste from: FileProvider#getType(Uri).
        final String fileName = uri.getLastPathSegment();
        final int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = fileName.substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) throws RuntimeException {
        return getAssetFileDescriptor(uri);
    }

    private AssetFileDescriptor getAssetFileDescriptor(@NonNull Uri uri) throws RuntimeException {
        Context context = getContext();
        if (context == null)
            throw new RuntimeException("Context mustn't be null");

        String path = getRelativePath(uri);
        if (path == null)
            throw new RuntimeException("Invalid uri: " + uri);

        AssetManager assetManager = context.getAssets();
        try {
            return assetManager.openFd(path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't open asset: " + path, e);
        }
    }

    private String getRelativePath(@NonNull Uri uri) {
        String path = uri.getPath();
        return (path.charAt(0) == '/') ?
                path.substring(1) :
                path;
    }
}
