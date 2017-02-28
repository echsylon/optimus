package com.echsylon.optimus.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.echsylon.optimus.Optimus;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import bolts.Task;
import bolts.TaskCompletionSource;

import static com.echsylon.optimus.app.Utils.closeSilently;

@OptionsMenu(R.menu.main)
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int CODE_PICK_INPUT_FILE = 1;
    private static final int CODE_PICK_FILTER_FILE = 2;
    private static final Uri DEFAULT_INPUT_URI = new Uri.Builder()
            .scheme("content")
            .authority("com.echsylon.optimus.app.assets")
            .path("test_input.json")
            .build();
    private static final Uri DEFAULT_FILTER_URI = new Uri.Builder()
            .scheme("content")
            .authority("com.echsylon.optimus.app.assets")
            .path("test_filter.jq")
            .build();

    @ViewById(R.id.coordinator_layout)
    View anchor;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.transform)
    View transformButton;

    @ViewById(R.id.input)
    InfoView inputView;

    @ViewById(R.id.output)
    InfoView outputView;

    @ViewById(R.id.filter)
    InfoView filterView;


    @Bean
    StreamSuppliers streams;

    @Bean
    SnackbarHandler snackbars;

    @Bean
    DialogHandler dialogs;


    @InstanceState
    Uri inputUri;

    @InstanceState
    Uri filterUri;


    private Optimus optimus;

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);

        inputView.setOnClickListener(view -> {
            switch (view.getId()) {
                case R.id.icon:
                    showFilePicker(CODE_PICK_INPUT_FILE);
                    break;
                default:
                    showDetails(R.string.input, getInputUri(), 0L, false);
                    break;
            }
        });

        refreshInput();
        refreshFilter();
        refreshOutputView();

        optimus = new Optimus();
        transformButton.setOnClickListener(view -> {
            dialogs.showProgressDialog(this, R.string.transforming);
            long t = System.currentTimeMillis();
            executeTransformationTask()
                    .onSuccessTask(task -> {
                        long duration = System.currentTimeMillis() - t;
                        showDetails(R.string.output, getOutputUri(), duration, true);
                        return task;
                    })
                    .continueWith(task -> {
                        dialogs.dismissProgressDialog();
                        if (task.isFaulted())
                            snackbars.showSnackbarForever(anchor, R.string.transformation_error);
                        return null;
                    });
        });
    }

    @OptionsItem(R.id.reset)
    void onResetAction() {
        inputUri = DEFAULT_INPUT_URI;
        refreshInput();

        filterUri = DEFAULT_FILTER_URI;
        refreshFilter();

        deleteOutput();
        refreshOutputView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case CODE_PICK_INPUT_FILE:
                    inputUri = data.getData();
                    refreshInput();
                    break;
                case CODE_PICK_FILTER_FILE:
                    filterUri = data.getData();
                    refreshFilter();
                    break;
                default:
                    // Huh?
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        dialogs.dismissProgressDialog();
        snackbars.dismissSnackbar();
        super.onDestroy();
    }


    private void refreshInput() {
        Uri uri = getInputUri();
        getFileName(uri).continueWith(task -> {
            inputView.setText(task.getResult());
            inputView.setOnClickListener(task.isFaulted() ? null : view -> {
                if (view.getId() == R.id.icon)
                    showFilePicker(CODE_PICK_INPUT_FILE);
                else
                    showDetails(R.string.input, getInputUri(), 0L, false);
            });
            return null;
        });
    }

    private void refreshFilter() {
        Uri uri = getFilterUri();
        getFileName(uri).continueWith(task -> {
            filterView.setText(task.getResult());
            filterView.setOnClickListener(task.isFaulted() ? null : view -> {
                if (view.getId() == R.id.icon)
                    showFilePicker(CODE_PICK_FILTER_FILE);
                else
                    showDetails(R.string.filter, getFilterUri(), 0L, false);
            });
            return null;
        });
    }

    private void refreshOutputView() {
        Uri uri = getOutputUri();
        boolean exists = new File(uri.getPath()).exists();
        outputView.setText(exists ? uri.toString() : getText(R.string.unavailable_output));
        outputView.setEnabled(exists);
        outputView.setOnClickListener(!exists ? null : view -> {
            switch (view.getId()) {
                case R.id.icon:
                    deleteOutput();
                    refreshOutputView();
                    break;
                default:
                    showDetails(R.string.output, getOutputUri(), 0L, false);
                    break;
            }
        });
    }

    private void deleteOutput() {
        Uri uri = getOutputUri();
        //noinspection ResultOfMethodCallIgnored
        new File(uri.getPath()).delete();
    }

    private Uri getInputUri() {
        return inputUri != null ? inputUri : DEFAULT_INPUT_URI;
    }

    private Uri getFilterUri() {
        return filterUri != null ? filterUri : DEFAULT_FILTER_URI;
    }

    private Uri getOutputUri() {
        return Uri.fromFile(new File(getExternalCacheDir(), "output.json"));
    }

    private void showFilePicker(final int resultTag) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, resultTag);
    }

    private void showDetails(@StringRes final int titleRes,
                             @NonNull final Uri uri,
                             final long transformationDuration,
                             final boolean showStatsAfterLaunch) {

        DetailActivity_.intent(this)
                .showStatsAfterLaunch(showStatsAfterLaunch)
                .filterDuration(transformationDuration)
                .titleResId(titleRes)
                .data(uri)
                .start();
    }

    private Task<Void> executeTransformationTask() {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        AsyncTask.execute(() -> {
            InputStream inputStream = streams.newInputStream(this, getInputUri()).get();
            InputStream filterStream = streams.newInputStream(this, getFilterUri()).get();
            OutputStream outputStream = streams.newOutputStream(this, getOutputUri()).get();

            int status = optimus.transformJson(getCacheDir(), inputStream, filterStream, outputStream);
            closeSilently(filterStream, inputStream, outputStream);

            if (status == Optimus.SUCCESS) {
                taskCompletionSource.trySetResult(null);
            } else {
                String message = String.format("Couldn't transform data: %s", status);
                RuntimeException cause = new RuntimeException(message);
                Log.d(LOG_TAG, message);
                taskCompletionSource.setError(cause);
            }
        });

        return taskCompletionSource.getTask();
    }

    private Task<String> getFileName(@NonNull final Uri uri) {
        final TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        AsyncTask.execute(() -> {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);
            try {
                if (cursor == null)
                    throw new IllegalArgumentException();

                if (!cursor.moveToFirst())
                    throw new IndexOutOfBoundsException();

                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String fileName = cursor.getString(columnIndex);
                taskCompletionSource.trySetResult(fileName);
            } catch (Exception e) {
                String message = String.format("Couldn't find name for: %s", uri);
                RuntimeException cause = new RuntimeException(message, e);
                taskCompletionSource.trySetError(cause);
            } finally {
                closeSilently(cursor);
            }
        });

        return taskCompletionSource.getTask();
    }
}
