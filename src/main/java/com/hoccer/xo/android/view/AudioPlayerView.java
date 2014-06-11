package com.hoccer.xo.android.view;

import android.app.AlertDialog;
import android.content.*;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class AudioPlayerView
        extends LinearLayout
        implements View.OnClickListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayerView.class);

    private MediaPlayerService mMediaPlayerService;
    private ImageButton mPlayPauseButton;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    private ServiceConnection mConnection;
    private AudioAttachmentItem mAudioContentObject;
    private boolean mIsPlayable = false;

    public AudioPlayerView(Context context) {
        super(context);
        mContext = context;
        addView(inflate(mContext, R.layout.content_audio, null));
    }

    public void setContentObject(IContentObject contentObject) {
        mAudioContentObject = AudioAttachmentItem.create(contentObject.getContentDataUrl(), contentObject);
        if (mAudioContentObject == null) {
            mIsPlayable = false;
        } else {
            mIsPlayable = true;
        }
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

    private void pausePlaying() {
        if (isBound()) {
            mMediaPlayerService.pause();
        }
    }

    private void startPlaying() {
        if (isBound()) {
            mMediaPlayerService.setMedia(mAudioContentObject);
            mMediaPlayerService.play(0);
        }
    }

    @Override
    public void onClick(View view) {
        if (mIsPlayable) {
            if (isActive()) {
                pausePlaying();
            } else {
                startPlaying();
            }
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setMessage(mContext.getResources().getString(R.string.content_not_supported_audio_msg));
            alertDialog.setTitle(mContext.getString(R.string.content_not_supported_audio_title));
            DialogInterface.OnClickListener nullListener = null;
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", nullListener);
            alertDialog.show();
        }
    }

    public void updatePlayPauseView() {
        mPlayPauseButton.setImageResource((isActive()) ? R.drawable.ic_dark_pause : R.drawable.ic_dark_play);
    }

    public boolean isActive() {
        boolean isActive = false;
        if (mAudioContentObject != null && isBound()) {
            isActive = !mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped() && mAudioContentObject.equals(mMediaPlayerService.getCurrentMediaItem());
        }

        return isActive;
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
