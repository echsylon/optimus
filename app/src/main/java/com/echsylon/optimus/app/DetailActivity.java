package com.echsylon.optimus.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSource;
import okio.Okio;

import static com.echsylon.optimus.app.Utils.closeSilently;

@EActivity(R.layout.activity_details)
@OptionsMenu(R.menu.details)
public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getName();


    @ViewById(R.id.coordinator_layout)
    View anchor;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.preview)
    CodeInfoView previewInfoView;


    @Bean
    StreamSuppliers streams;

    @Bean
    SnackbarHandler snackbars;

    @Bean
    DialogHandler dialogs;


    @Extra
    CharSequence title;

    @Extra
    int titleResId;

    @Extra
    long filterDuration;

    @Extra
    boolean showStatsAfterLaunch;


    long charCount;

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_cross);
        toolbar.setNavigationOnClickListener(view -> finish());

        if (title != null)
            setTitle(title);

        if (titleResId > 0)
            setTitle(titleResId);

        Intent intent = getIntent();
        readFileContent(intent.getData());
    }

    @OptionsItem(R.id.stats)
    void onStatsAction() {
        StatisticsDialogFragment_.builder()
                .titleRes(R.string.statistics)
                .filterDuration(filterDuration)
                .charCount(charCount)
                .build()
                .show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onDestroy() {
        dialogs.dismissProgressDialog();
        snackbars.dismissSnackbar();
        super.onDestroy();
    }


    private void readFileContent(@NonNull final Uri uri) {
        new AsyncTask<Uri, Void, Pair<Integer, String>>() {
            @Override
            protected void onPreExecute() {
                dialogs.showProgressDialog(DetailActivity.this, R.string.reading_file_content);
                previewInfoView.setText(null);
            }

            @Override
            protected Pair<Integer, String> doInBackground(Uri... uri) {
                if (uri == null || uri.length != 1)
                    return Pair.create(1, null);

                InputStream resultStream = streams.newInputStream(DetailActivity.this, uri[0]).get();
                BufferedSource bufferedSource = null;

                try {
                    bufferedSource = Okio.buffer(Okio.source(resultStream));
                    return Pair.create(0, bufferedSource.readUtf8());
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Couldn't read data: " + uri[0], e);
                    return Pair.create(2, null);
                } finally {
                    closeSilently(resultStream, bufferedSource);
                }
            }

            @Override
            protected void onPostExecute(final Pair<Integer, String> result) {
                dialogs.dismissProgressDialog();

                if (result.first != 0) {
                    snackbars.showSnackbar(anchor, R.string.embarrassing_error,
                            () -> readFileContent(uri));
                } else {
                    String content = result.second;
                    charCount = content.length();
                    previewInfoView.setText(content.length() > 2000 ?
                            content.substring(0, 1999) + "…" :
                            content);

                    if (showStatsAfterLaunch) {
                        onStatsAction();
                    }
                }
            }
        }.execute(uri);
    }
}
