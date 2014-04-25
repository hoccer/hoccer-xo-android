package com.hoccer.xo.android.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.xo.android.content.audio.AudioViewCache;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener,
                   MediaPlayer.OnErrorListener,
                   MediaPlayer.OnPreparedListener,
                   MediaPlayer.OnCompletionListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    private MediaPlayer mPlayer;

    private ImageButton mPlayPause;

    private AudioViewCache mParentCache;

    private String mCurrentPath;

    public AudioPlayerView(Context context, AudioViewCache parentCache) {
        super(context);
        initialize(context, parentCache);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, AudioViewCache parentCache) {
        super(context, attrs);
        initialize(context, parentCache);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyle, AudioViewCache parentCache) {
        super(context, attrs, defStyle);
        initialize(context, parentCache);
    }

    private void initialize(Context context, AudioViewCache parentCache) {
        mPlayer = new MediaPlayer();
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        addView(inflate(context, R.layout.content_audio, null));
        mPlayPause = (ImageButton) findViewById(R.id.audio_play);
        mPlayPause.setOnClickListener(this);
        mParentCache = parentCache;
    }

    public void pausePlaying(){
        LOG.error("********************************* pause: " + mCurrentPath);
        mPlayPause.setImageResource(R.drawable.ic_dark_play);
        mPlayer.pause();
    }

    public void stopPlaying(){
        LOG.error("********************************* stop: " + mCurrentPath);
        mPlayPause.setImageResource(R.drawable.ic_dark_play);
        mPlayer.stop();
    }

    public void startPlaying(){
        LOG.error("********************************* start: " + mCurrentPath);
        mPlayPause.setImageResource(R.drawable.ic_dark_pause);
        mPlayer.start();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public String getCurrentPath(){
        return mCurrentPath;
    }

    @Override
    public void onClick(View v) {
        if(v == mPlayPause) {
            LOG.debug("onClick(PlayPause)");
            if(isPlaying()) {
                pausePlaying();
            } else {
                startPlaying();
            }
            mParentCache.togglePlayback(mPlayer.isPlaying(), mCurrentPath);
        }
    }

    public void setFile(String path) {
        LOG.debug("setFile(" + path + ")");
        mPlayPause.setEnabled(false);
        mPlayPause.setImageResource(R.drawable.ic_dark_play);
        if(isPlaying()) {
            stopPlaying();
        }
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepareAsync();

            mCurrentPath = path;
        } catch (Exception e) {
            LOG.error("setFile: exception setting data source", e);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LOG.debug("onError(" + what + "," + extra + ")");
        mPlayPause.setEnabled(false);
        mPlayPause.setImageResource(R.drawable.ic_dark_play);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LOG.debug("onPrepared()");
        mPlayPause.setEnabled(true);
        mPlayPause.setImageResource(R.drawable.ic_dark_play);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LOG.debug("onCompletion()");
        mPlayPause.setImageResource(R.drawable.ic_dark_play);
    }
}
