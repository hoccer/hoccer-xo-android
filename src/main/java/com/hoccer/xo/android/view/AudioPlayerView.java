package com.hoccer.xo.android.view;

import android.content.*;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.MediaItem;
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    private MediaPlayerService mMediaPlayerService;
    private ImageButton mPlayPauseButton;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    private ServiceConnection mConnection;
    private IContentObject contentObject;
    private MediaPlaylist playlist;

    public void setContentObject(IContentObject contentObject) {
        this.contentObject = contentObject;
    }

    public AudioPlayerView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        addView(inflate(context, R.layout.content_audio, null));
        mContext = context;
    }

    private void bindService(Intent intent) {

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
                updatePlayPauseView();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

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
        if (isBound()) {
            mMediaPlayerService.pause();
        }
    }

    private void startPlaying() {
        if (isBound()) {
            if (playlist == null) {
                playlist = MediaPlaylist.create(contentObject.getContentDataUrl());
            }
            mMediaPlayerService.start(playlist);
        }
    }

    @Override
    public void onClick(View view) {
        if (isActive()) {
            pausePlaying();
        } else {
            startPlaying();
        }
    }

    public void updatePlayPauseView() {
        if (isActive()) {
            showPauseButton();
        } else {
            showPlayButton();
        }
    }

    public boolean isActive() {
        if (contentObject != null && isBound()) {
            return !mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped() && contentObject.getContentDataUrl().equals(mMediaPlayerService.getCurrentMediaFilePath());
        } else {
            return false;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.startService(intent);
        bindService(intent);

        createBroadcastReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unbindService(mConnection);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private void createBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    updatePlayPauseView();
                }
            }
        };
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    }

    public boolean isBound() {
        return mMediaPlayerService != null;
    }
}
