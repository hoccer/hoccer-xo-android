package com.hoccer.xo.android.activity;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.MediaMetaData;
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
    private TextView mSongArtistLabel;
    private TextView mSongCurrentDurationLabel;
    private TextView mSongTotalDurationLabel;
    private TextView mTitleNumberLabel;
    private long mTotalDuration = 0;
    private ImageView mArtworkView;

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
        mSongTitleLabel = (TextView) findViewById(R.id.attachmentlist_item_title_name);
        mSongArtistLabel = (TextView) findViewById(R.id.attachmentlist_item_artist_name);
        mSongCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        mSongTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        mTitleNumberLabel = (TextView) findViewById(R.id.attachmentlist_item_number);
        mArtworkView = (ImageView) findViewById(R.id.artwork_view);

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
                    mButtonPlay.setImageResource(R.drawable.ic_player_play);
                } else {
                    mMediaPlayerService.start();
                    mButtonPlay.setImageResource(R.drawable.ic_player_pause);
                }
            }
        });

        mButtonPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playPrevTrack();
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextTrack();
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

                updateView();
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

    private void updateView() {

        mTempFilePath = mMediaPlayerService.getCurrentMediaFilePath();

        String artistName = mMediaPlayerService.getMediaMetaData().getArtist();
        String trackName = mMediaPlayerService.getMediaMetaData().getTitle(getResources().getString(R.string.app_name));

        mSongTitleLabel.setText(trackName);
        mSongArtistLabel.setText(artistName);
        mTotalDuration = mMediaPlayerService.getTotalDuration();
        mSongTotalDurationLabel.setText("" + milliSecondsToTimer(mTotalDuration));
        // TODO: track no. from current playlist
        mTitleNumberLabel.setText("");

        byte[] cover = MediaMetaData.getArtwork(mTempFilePath);

        if( cover != null ) {
            Bitmap coverBitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            mArtworkView.setImageBitmap(coverBitmap);
        }

        updatePlayPauseView();
    }

    private void updatePlayPauseView() {
        if (mMediaPlayerService != null) {
            mButtonPlay.setImageResource((!mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped()) ? R.drawable.ic_player_pause : R.drawable.ic_player_play);
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
                if (intent.getAction().equals(MediaPlayerService.TRACK_CHANGED_ACTION)) {
                    updateView();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(MediaPlayerService.TRACK_CHANGED_ACTION));
    }

    private void playPrevTrack() {
        mMediaPlayerService.playPrevious();
    }

    private void playNextTrack() {
        mMediaPlayerService.playNext();
    }

}
