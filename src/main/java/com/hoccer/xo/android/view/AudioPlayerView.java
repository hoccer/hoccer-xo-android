package com.hoccer.xo.android.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.xo.android.content.audio.AudioViewCache;
import com.hoccer.xo.android.content.audio.AudioPlayer;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    private AudioPlayer mPlayer;

    private ImageButton mPlayPauseButton;

//    private AudioViewCache mAudioViewCache;

    private boolean mActive = false;

    String mAudioPlayerPath;

    public AudioPlayerView(Context context/*, AudioViewCache parentCache*/) {
        super(context);
        initialize(context/*, parentCache*/);
    }

    private void initialize(Context context/*, AudioViewCache parentCache*/) {
        addView(inflate(context, R.layout.content_audio, null));
        mPlayer = AudioPlayer.get(context);
        mPlayPauseButton = (ImageButton) findViewById(R.id.audio_play);
        mPlayPauseButton.setOnClickListener(this);
//        mAudioViewCache = parentCache;
    }

    private void showPauseButton() {
        mPlayPauseButton.setImageResource(R.drawable.ic_dark_pause);
    }

    private void showPlayButton() {
        mPlayPauseButton.setImageResource(R.drawable.ic_dark_play);
    }

    private void pausePlaying() {
        mPlayer.pause();
    }

    private void startPlaying() {
        mPlayer.start(this);
    }

    public void setStopState() {
        setActive(false);
        showPlayButton();
    }

    public void setPauseState() {
        setActive(false);
        showPlayButton();
    }

    public void setPlayState() {
        setActive(true);
        showPauseButton();
    }

    public String getPlayerViewPath() {
        return mAudioPlayerPath;
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayPauseButton) {

            if (isActive()) {
                pausePlaying();
            } else
                startPlaying();
            }

//            if (isActive() && mPlayer.isPlaying()) {
//                pausePlaying();
//            } else if (!isActive() && mPlayer.isPlaying()) {
//                togglePlaying();
//            } else if (isActive() && mPlayer.isPaused()) {
//                togglePlaying();
//            } else {
//                startPlaying(mAudioPlayerPath);
//            }
    }

    public void setFile(String path) {
        mAudioPlayerPath = path;
    }

    public boolean isActive() {
        return mActive;
    }

    private void setActive(boolean mActive) {
        this.mActive = mActive;
    }
}
