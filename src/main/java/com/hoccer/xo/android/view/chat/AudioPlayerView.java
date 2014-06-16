package com.hoccer.xo.android.view.chat;

import android.content.*;
import android.view.View;
import android.widget.LinearLayout;
import com.hoccer.xo.android.view.IViewListener;
import com.hoccer.xo.release.R;

public class AudioPlayerView
        extends LinearLayout {

    private IViewListener mListener;

    public AudioPlayerView(Context context, IViewListener listener) {
        super(context);

        mListener = listener;
        View v =  inflate(context, R.layout.content_audio, null);
        addView(v);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mListener.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mListener.onDetachedFromWindow();
    }
}
