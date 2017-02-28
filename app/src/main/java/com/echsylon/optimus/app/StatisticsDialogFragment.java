package com.echsylon.optimus.app;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static android.view.View.VISIBLE;

/**
 * Created by laszlo on 2017-02-26.
 */
@EFragment(R.layout.fragment_statistics)
public class StatisticsDialogFragment extends DialogFragment {

    @FragmentArg
    CharSequence title;

    @FragmentArg
    int titleRes;

    @FragmentArg
    long charCount;

    @FragmentArg
    long filterDuration;


    @ViewById(R.id.file_size)
    InfoView fileSizeView;

    @ViewById(R.id.char_count)
    InfoView charCountView;

    @ViewById(R.id.duration)
    InfoView durationView;


    private View contentView;

    @AfterViews
    void afterViews() {
        String fileSizeText = Formatter.formatShortFileSize(getContext(), charCount);
        fileSizeView.setText(fileSizeText);

        String charCountText = getString(R.string.nbr_characters, charCount);
        charCountView.setText(charCountText);

        if (filterDuration > 0) {
            String durationText = getString(R.string.nbr_milliseconds, filterDuration);
            durationView.setText(durationText);
            durationView.setVisibility(VISIBLE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        contentView = layoutInflater.inflate(R.layout.fragment_statistics, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(android.R.string.ok, (dialog, button) -> dismiss());
        builder.setView(contentView);

        if (title != null)
            builder.setTitle(title);

        if (titleRes > 0)
            builder.setTitle(titleRes);

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return contentView;
    }
}
