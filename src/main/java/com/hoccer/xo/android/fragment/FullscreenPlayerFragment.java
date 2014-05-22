package com.hoccer.xo.android.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.*;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class FullscreenPlayerFragment extends Fragment {

    static final Logger LOG = Logger.getLogger(FullscreenPlayerFragment.class);
    static final String LOG_TAG = "BAZINGA";

    private ToggleButton mPlayButton;
    private ImageButton mSkipForwardButton;
    private ImageButton mSkipBackButton;
    private ImageButton mRepeatButton;
    private ToggleButton mShuffleButton;
    private SeekBar mTrackProgressBar;
    private TextView mTrackTitleLabel;
    private TextView mTrackArtistLabel;
    private TextView mCurrentTimeLabel;
    private TextView mTotalDurationLabel;
    private TextView mPlaylistIndexLabel;
    private TextView mPlaylistSizeLabel;
    private ImageView mArtworkView;

    private MediaPlayerService mMediaPlayerService;
    private ServiceConnection mServiceConnection;
    private Handler mTimeProgressHandler = new Handler();
    private Runnable mUpdateTimeTask;
    private ValueAnimator mBlinkAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBlinkAnimation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_fullscreen_player, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlayButton = (ToggleButton) getView().findViewById(R.id.bt_player_play);
        mSkipForwardButton = (ImageButton) getView().findViewById(R.id.bt_player_skip_forward);
        mSkipBackButton = (ImageButton) getView().findViewById(R.id.bt_player_skip_back);
        mRepeatButton = (ImageButton) getView().findViewById(R.id.bt_player_repeat);
        mShuffleButton = (ToggleButton) getView().findViewById(R.id.bt_player_shuffle);
        mTrackProgressBar = (SeekBar) getView().findViewById(R.id.pb_player_seek_bar);
        mTrackTitleLabel = (TextView) getView().findViewById(R.id.tv_player_track_title);
        mTrackArtistLabel = (TextView) getView().findViewById(R.id.tv_player_track_artist);
        mCurrentTimeLabel = (TextView) getView().findViewById(R.id.tv_player_current_time);
        mTotalDurationLabel = (TextView) getView().findViewById(R.id.tv_player_total_duration);
        mPlaylistIndexLabel = (TextView) getView().findViewById(R.id.tv_player_current_track_no);
        mPlaylistSizeLabel = (TextView) getView().findViewById(R.id.tv_player_playlist_size);
        mArtworkView = (ImageView) getView().findViewById(R.id.iv_player_artwork);
        mTrackProgressBar.setProgress(0);
        mTrackProgressBar.setMax(100);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resizeCoverArtView();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                    mMediaPlayerService = binder.getService();
                    setupViewListeners();
                    enableViewComponents(true);
                    updateTrackData();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    enableViewComponents(false);
                }
            };
        }
        Intent serviceIntent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        //resizeCoverArtView();
        if (mMediaPlayerService != null) {
            updateTrackData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mServiceConnection);
        mTimeProgressHandler.removeCallbacks(mUpdateTimeTask);
        mUpdateTimeTask = null;
    }

    public void updatePlayState() {
        if (mMediaPlayerService != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBlinkAnimation.cancel();
                    if (mMediaPlayerService.isPaused() || mMediaPlayerService.isStopped()) {
                        mPlayButton.setActivated(true);
                        mBlinkAnimation.start();
                    } else {
                        mPlayButton.setActivated(false);
                        mCurrentTimeLabel.setTextColor(getResources().getColor(R.color.xo_media_player_secondary_text));
                    }
                }
            });
        }
    }

    public void updateTrackData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String trackArtist = mMediaPlayerService.getMediaMetaData().getArtist();
                String trackTitle = mMediaPlayerService.getMediaMetaData().getTitle();
                int totalDuration = mMediaPlayerService.getTotalDuration();
                byte[] cover = MediaMetaData.getArtwork(mMediaPlayerService.getCurrentMediaFilePath());


                if (trackTitle == null || trackTitle.isEmpty()) {
                    File file = new File(mMediaPlayerService.getCurrentMediaFilePath());
                    trackTitle = file.getName();
                }

                mTrackTitleLabel.setText(trackTitle);
                if (trackArtist == null || trackArtist.isEmpty()) {
                    trackArtist = getActivity().getResources().getString(R.string.media_meta_data_unknown_artist);
                }
                mTrackArtistLabel.setText(trackArtist);
                mTrackProgressBar.setMax(totalDuration);
                mTrackProgressBar.setProgress(mMediaPlayerService.getCurrentPosition());

                mTotalDurationLabel.setText(stringFromTimeStamp(totalDuration));
                mPlaylistIndexLabel.setText(Integer.toString(mMediaPlayerService.getCurrentPlaylist().getCurrentIndex() + 1));
                mPlaylistSizeLabel.setText(Integer.toString(mMediaPlayerService.getCurrentPlaylist().size()));

                if (cover != null) {
                    Bitmap coverBitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
                    mArtworkView.setImageBitmap(coverBitmap);
                } else {
                    mArtworkView.setImageResource(R.drawable.media_cover_art_default);
                }

                resizeCoverArtView();
                updatePlayState();
            }
        });

        if (mUpdateTimeTask == null) {
            mUpdateTimeTask = new UpdateTimeTask();
        }

        mTimeProgressHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void setupViewListeners() {
        OnPlayerInteractionListener listener = new OnPlayerInteractionListener();
        mPlayButton.setOnClickListener(listener);
        mTrackProgressBar.setOnSeekBarChangeListener(listener);
        mSkipBackButton.setOnClickListener(listener);
        mSkipForwardButton.setOnClickListener(listener);
        mRepeatButton.setOnClickListener(listener);
        mShuffleButton.setOnClickListener(listener);
    }

    private void enableViewComponents(final boolean enable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayButton.setEnabled(enable);
                mSkipForwardButton.setEnabled(enable);
                mSkipBackButton.setEnabled(enable);
                mRepeatButton.setEnabled(enable);
                mShuffleButton.setEnabled(enable);
            }
        });

    }

    private void setupBlinkAnimation() {
        int colorFrom = getResources().getColor(R.color.xo_media_player_secondary_text);
        int colorTo = getResources().getColor(R.color.xo_app_main_color);
        mBlinkAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        mBlinkAnimation.setDuration(500);
        mBlinkAnimation.setRepeatMode(Animation.REVERSE);
        mBlinkAnimation.setRepeatCount(Animation.INFINITE);
        mBlinkAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentTimeLabel.setTextColor((Integer) animation.getAnimatedValue() );
            }
        });
    }

    private String stringFromTimeStamp(int timeInMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(minutes);
        if (hours > 0) {
            return String.format("%d:%02d:%02d", minutes, seconds);
        }

        return String.format("%2d:%02d", minutes, seconds);
    }

    private void resizeCoverArtView() {
        int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.media_player_layout_margin);
        int measuredViewHeight = getView().getMeasuredWidth() - (margin * 4);
        if (mArtworkView.getHeight() != measuredViewHeight) {
            Log.d(LOG_TAG, "Current width: " + mArtworkView.getWidth() + " height: " + mArtworkView.getHeight());
            Log.d(LOG_TAG, "ParentView width: " + getView().getMeasuredWidth());
            RelativeLayout.LayoutParams coverArtLayoutParams = (RelativeLayout.LayoutParams) mArtworkView.getLayoutParams();

            coverArtLayoutParams.height = measuredViewHeight;
            coverArtLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mArtworkView.setLayoutParams(coverArtLayoutParams);
            Log.d(LOG_TAG, "New width: " + mArtworkView.getWidth() + " height: " + mArtworkView.getHeight());
            Log.d(LOG_TAG, "getView() = " + getView());
        }
    }

    private class UpdateTimeTask implements Runnable {

        @Override
        public void run() {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int currentDuration = mMediaPlayerService.getCurrentPosition();
                        mCurrentTimeLabel.setText(stringFromTimeStamp(currentDuration));
                        mTrackProgressBar.setProgress(currentDuration);
                    }
                });

                mTimeProgressHandler.postDelayed(this, 100);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    }

    private class OnPlayerInteractionListener implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_player_play:
                    if (!mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped()) {
                        mMediaPlayerService.pause();
                    } else {
                        mMediaPlayerService.start();
                    }
                    break;
                case R.id.bt_player_skip_back:
                    mMediaPlayerService.playPrevious(true);
                    break;
                case R.id.bt_player_skip_forward:
                    mMediaPlayerService.playNext(true);
                    break;
                case R.id.bt_player_repeat:
                    // TODO set playlist to repeat (all & single)
                    break;
                case R.id.bt_player_shuffle:
                    // TODO set playlist to shuffle
                    break;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mMediaPlayerService.setSeekPosition(seekBar.getProgress());
            mTimeProgressHandler.postDelayed(mUpdateTimeTask, 100);
        }

    }
}