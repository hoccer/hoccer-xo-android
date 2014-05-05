package com.hoccer.xo.android.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.xo.android.content.audio.AudioPlayer;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener, AudioPlayer.PauseStateChangedListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    private AudioPlayer mPlayer;

    private ImageButton mPlayPauseButton;

    private String mMediaFilePath;

    public AudioPlayerView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        addView(inflate(context, R.layout.content_audio, null));

        mPlayer = AudioPlayer.get(context);

        mPlayPauseButton = (ImageButton) findViewById(R.id.audio_play);
        mPlayPauseButton.setOnClickListener(this);
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
        mPlayer.start(mMediaFilePath);
    }

    @Override
    public void onClick(View view) {
        if (isActive()) {
            pausePlaying();
        } else {
            startPlaying();
        }
    }

    public void setFile(String path) {
        mMediaFilePath = path;
    }

    public void updatePlayPauseView() {
        if (isActive()) {
            showPauseButton();
        } else {
            showPlayButton();
        }
    }

    public boolean isActive() {
        if (mMediaFilePath != null) {
            return !mPlayer.isPaused() && !mPlayer.isStopped() && mMediaFilePath.equals(mPlayer.getCurrentMediaFilePath());
        } else {
            return false;
        }
    }

    @Override
    public void onPauseStateChanged() {
        updatePlayPauseView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mPlayer.addPauseStateChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayer.removePauseStateChangedListener(this);
    }
}
