package com.hoccer.xo.android.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.audio.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends XoActivity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageButton mButtonPlay;
    private ImageButton mButtonForward;
    private ImageButton mButtonBackward;
    private ImageButton mButtonNext;
    private ImageButton mButtonPrevious;
    private ImageButton mButtonPlaylist;
    private ImageButton mButtonRepeat;
    private ImageButton mButtonShuffle;
    private SeekBar mSongProgressBar;
    private TextView mSongTitleLabel;
    private TextView mSongCurrentDurationLabel;
    private TextView mSongTotalDurationLabel;

    private MediaPlayerService mMediaPlayerService;

    private Handler mHandler = new Handler();

    private String mTempFilePath;

    private final static Logger LOG = Logger.getLogger(FullscreenPlayerActivity.class);


    @Override
    protected int getLayoutResource() {
        return R.layout.music_fullscreen_player;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent);

        enableUpNavigation();

        mButtonPlay = (ImageButton) findViewById(R.id.btnPlay);
        mButtonForward = (ImageButton) findViewById(R.id.btnForward);
        mButtonBackward = (ImageButton) findViewById(R.id.btnBackward);
        mButtonNext = (ImageButton) findViewById(R.id.btnNext);
        mButtonPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        mButtonRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        mButtonShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        mSongProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        mSongTitleLabel = (TextView) findViewById(R.id.songTitle);
        mSongCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        mSongTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        mSongProgressBar.setOnSeekBarChangeListener(this);

//        mMediaPlayerService.registerOnCompletitionListener(this);

        mSongProgressBar.setProgress(0);
        mSongProgressBar.setMax(100);

        updateProgressBar();

        mButtonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped()) {
                    mMediaPlayerService.pause();
                    mButtonPlay.setImageResource(R.drawable.ic_dark_play);
                } else {
                    mMediaPlayerService.start(mTempFilePath);
                    mButtonPlay.setImageResource(R.drawable.ic_dark_pause);
                }
            }
        });

        mButtonForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                LOG.error("------------------------- Forward onClick");
            }
        });

        mButtonBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                LOG.error("------------------------- Back onClick");
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                LOG.error("------------------------- Next onClick");
            }
        });

        mButtonPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMediaPlayerService.setSeekPosition(0);
            }
        });

        mButtonRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                LOG.error("------------------------- Repeat onClick");
            }
        });

        mButtonShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                LOG.error("------------------------- Shuffle onClick");
            }
        });
    }

    private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long totalDuration = mMediaPlayerService.getTotalDuration();
            long currentDuration = mMediaPlayerService.getCurrentPosition();

            LOG.error("------------------------- run: " + totalDuration + ", " + currentDuration);

            mSongTotalDurationLabel.setText("" + milliSecondsToTimer(totalDuration));

            mSongCurrentDurationLabel.setText("" + milliSecondsToTimer(currentDuration));

            int progress = getProgressPercentage(currentDuration, totalDuration);

            mSongProgressBar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        LOG.error("------------------------- onProgressChanged");
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
//        mUpdateTimeTask = null;
        LOG.error("------------------------- onStartTrackingTouch");
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        LOG.error("------------------------- onStopTrackingTouch");

        mMediaPlayerService.setSeekPosition(seekBar.getProgress());

        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // used for shuffling or repeat later on
    }

    private void bindService(Intent intent){

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();

                mTempFilePath = mMediaPlayerService.getCurrentMediaFilePath();

                String artistName = (mMediaPlayerService.getArtistName() == null) ? "" : mMediaPlayerService.getArtistName();
                String trackName = (mMediaPlayerService.getTrackName() == null) ? "" : mMediaPlayerService.getTrackName();

                mSongTitleLabel.setText(artistName + "\n" + trackName);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int)( milliseconds / (1000 * 60 * 60));
        int minutes = (int)(milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if(hours > 0){
            finalTimerString = hours + ":";
        }

        if( seconds < 10 ){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = 0.0d;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage =(((double)currentSeconds)/totalSeconds)*100;

        return percentage.intValue();
    }
}