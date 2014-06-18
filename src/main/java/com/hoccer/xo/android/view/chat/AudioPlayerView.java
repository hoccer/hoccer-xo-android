package com.hoccer.xo.android.view.chat;

import android.content.*;
import android.view.View;
import android.widget.LinearLayout;
import com.hoccer.xo.release.R;

public class AudioPlayerView extends LinearLayout {

    public AudioPlayerView(Context context) {
        super(context);

        View v =  inflate(context, R.layout.content_audio, null);
        addView(v);
    }
}
