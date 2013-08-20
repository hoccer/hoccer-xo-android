package com.hoccer.talk.android.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.talk.android.R;
import org.apache.log4j.Logger;

import java.io.IOException;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener,
                   MediaPlayer.OnErrorListener,
                   MediaPlayer.OnPreparedListener,
                   MediaPlayer.OnCompletionListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    MediaPlayer mPlayer;

    ImageButton mPlayPause;

    public AudioPlayerView(Context context) {
        super(context);
        initialize(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        mPlayer = new MediaPlayer();
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        addView(inflate(context, R.layout.view_audio_player, null));
        mPlayPause = (ImageButton) findViewById(R.id.audio_play_pause);
        mPlayPause.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == mPlayPause) {
            LOG.info("onClick(PlayPause)");
            if(mPlayer.isPlaying()) {
                mPlayPause.setImageResource(R.drawable.ic_light_av_play);
                mPlayer.pause();
            } else {
                mPlayPause.setImageResource(R.drawable.ic_light_av_pause);
                mPlayer.start();
            }
        }
    }

    public void setFile(String path) {
        LOG.info("setFile(" + path + ")");
        mPlayPause.setEnabled(false);
        mPlayPause.setImageResource(R.drawable.ic_light_av_play);
        if(mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            LOG.error("exception setting data source", e);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LOG.info("onError(" + what + "," + extra + ")");
        mPlayPause.setEnabled(false);
        mPlayPause.setImageResource(R.drawable.ic_light_av_play);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LOG.info("onPrepared()");
        mPlayPause.setEnabled(true);
        mPlayPause.setImageResource(R.drawable.ic_light_av_play);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LOG.info("onCompletion()");
        mPlayPause.setImageResource(R.drawable.ic_light_av_play);
    }

}
