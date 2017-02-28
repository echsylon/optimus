package com.echsylon.optimus.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


@EViewGroup(R.layout.layout_infoview_default)
public class InfoView extends LinearLayout {
    public static final int LINE_TYPE_SINGLE = 1;
    public static final int LINE_TYPE_MULTI = 2;

    @ViewById(R.id.label)
    TextView labelView;

    @ViewById(R.id.text)
    TextView textView;

    @ViewById(R.id.icon)
    ImageView iconView;


    private CharSequence label;
    private CharSequence text;
    private Drawable icon;
    private int textType;

    public InfoView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public InfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public InfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }


    private void init(@NonNull final Context context,
                      @Nullable final AttributeSet attrs,
                      final int defStyleAttr,
                      final int defStyleRes) {

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.InfoView,
                    defStyleAttr,
                    defStyleRes);

            try {
                textType = typedArray.getInt(R.styleable.InfoView_textType, LINE_TYPE_SINGLE);
                label = typedArray.getText(R.styleable.InfoView_label);
                text = typedArray.getText(R.styleable.InfoView_text);
                icon = typedArray.getDrawable(R.styleable.InfoView_icon);
            } finally {
                typedArray.recycle();
            }
        }
    }

    @AfterViews
    void afterViews() {
        setTextType(textType);
        setLabel(label);
        setText(text);
        setIcon(icon);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        labelView.setOnClickListener(listener);
        textView.setOnClickListener(listener);
        iconView.setOnClickListener(listener);
        super.setOnClickListener(listener);
    }

    void setLabel(@Nullable final CharSequence text) {
        labelView.setText(text);
    }

    void setLabel(final int resId) {
        labelView.setText(resId);
    }

    void setText(@Nullable final CharSequence text) {
        textView.setText(text);
        textView.setVisibility(text == null ? GONE : VISIBLE);
    }

    void setText(@StringRes final int resId) {
        textView.setText(resId);
        textView.setVisibility(VISIBLE);
    }

    void setIcon(@Nullable final Drawable icon) {
        iconView.setImageDrawable(icon);
        iconView.setVisibility(icon == null ? GONE : VISIBLE);
    }

    void setIcon(@DrawableRes final int resId) {
        iconView.setImageResource(resId);
        iconView.setVisibility(VISIBLE);
    }

    void setTextType(final int textType) {
        switch (textType) {
            case LINE_TYPE_SINGLE:
                textView.setSingleLine(true);
                break;
            case LINE_TYPE_MULTI:
                textView.setSingleLine(false);
                break;
            default:
                // Unknown line type, don't ignore
                break;
        }
    }

}
