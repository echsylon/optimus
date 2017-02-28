package com.echsylon.optimus.app;

import android.content.Context;
import android.util.AttributeSet;

import org.androidannotations.annotations.EViewGroup;


@EViewGroup(R.layout.layout_infoview_code)
public class CodeInfoView extends InfoView {

    public CodeInfoView(Context context) {
        super(context);
    }

    public CodeInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CodeInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
