package com.hoccer.xo.android.view;

import android.content.*;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.xo.android.content.audio.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener{

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    private MediaPlayerService mMediaPlayerService;
    private ImageButton mPlayPauseButton;
    private String mMediaFilePath;
    private BroadcastReceiver mReceiver;
    private Context mContext;

    public AudioPlayerView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        addView(inflate(context, R.layout.content_audio, null));

        mContext = context;

        Intent intent = new Intent(context, MediaPlayerService.class);
        context.startService(intent);
        bindService(intent);
    }

    private void bindService(Intent intent){

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
                updatePlayPauseView();
                invalidate();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);

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
            mMediaPlayerService.start(mMediaFilePath);
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
        if (mMediaFilePath != null && isBound()) {
            return !mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped() && mMediaFilePath.equals(mMediaPlayerService.getCurrentMediaFilePath());
        } else {
            return false;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        createBroadcastReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mReceiver);
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
        mContext.registerReceiver(mReceiver, filter);
    }

    public boolean isBound() {
        return mMediaPlayerService != null;
    }
}
