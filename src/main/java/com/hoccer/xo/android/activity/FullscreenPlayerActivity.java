package com.hoccer.xo.android.activity;

import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.audio.AudioListManager;
import com.hoccer.xo.android.content.audio.MusicLoader;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends XoActivity implements SeekBar.OnSeekBarChangeListener {

    private ImageButton mButtonPlay;
    private ImageButton mButtonNext;
    private ImageButton mButtonPrevious;
    private ImageButton mButtonPlaylist;
    private ImageButton mButtonRepeat;
    private ImageButton mButtonShuffle;
    private SeekBar mSongProgressBar;
    private TextView mSongTitleLabel;
    private TextView mSongCurrentDurationLabel;
    private TextView mSongTotalDurationLabel;
    private long mTotalDuration = 0;

    private MediaPlayerService mMediaPlayerService;
    private BroadcastReceiver mBroadcastReceiver;

    private Handler mHandler = new Handler();

    private String mTempFilePath;

    private ServiceConnection mServiceConnection;

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
        mButtonNext = (ImageButton) findViewById(R.id.btnNext);
        mButtonPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        mButtonRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        mButtonShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        mSongProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        mSongTitleLabel = (TextView) findViewById(R.id.songTitle);
        mSongCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        mSongTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        mSongProgressBar.setOnSeekBarChangeListener(this);

        mSongProgressBar.setProgress(0);
        mSongProgressBar.setMax(100);

        createBroadcastReceiver();
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

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextTrack();
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            try {
                long currentDuration = mMediaPlayerService.getCurrentPosition();

                mSongCurrentDurationLabel.setText("" + milliSecondsToTimer(currentDuration));

                int progress = getProgressPercentage(currentDuration, mTotalDuration);

                mSongProgressBar.setProgress(progress);

                mHandler.postDelayed(this, 100);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        // abstract method implemented
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);

        mMediaPlayerService.setSeekPosition(seekBar.getProgress());

        updateProgressBar();
    }

    private void bindService(Intent intent) {

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();

                updatePlayPauseView();

                mTempFilePath = mMediaPlayerService.getCurrentMediaFilePath();

                String artistName = (mMediaPlayerService.getMediaMetaData().getArtist() == null) ? "" : mMediaPlayerService.getMediaMetaData().getArtist();
                String trackName = (mMediaPlayerService.getMediaMetaData().getTitle() == null) ? "" : mMediaPlayerService.getMediaMetaData().getTitle();

                String labelText = (artistName.equals("") && trackName.equals("")) ? mTempFilePath : (artistName + "\n" + trackName);

                mSongTitleLabel.setText(labelText);
                mTotalDuration = mMediaPlayerService.getTotalDuration();
                mSongTotalDurationLabel.setText("" + milliSecondsToTimer(mTotalDuration));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = 0.0d;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        return percentage.intValue();
    }

    private void updatePlayPauseView() {
        if (mMediaPlayerService != null) {
            if (!mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped()) {
                mButtonPlay.setImageResource(R.drawable.ic_dark_pause);
            } else {
                mButtonPlay.setImageResource(R.drawable.ic_dark_play);
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        mHandler.removeCallbacks(mUpdateTimeTask);
        mUpdateTimeTask = null;
    }

    private void createBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    updatePlayPauseView();
                }
            }
        };
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void playNextTrack() {
        AudioListManager.get(getApplicationContext()).playNext();
    }
}