package com.hoccer.xo.android.view.chat.attachments;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.view.IViewListener;
import com.hoccer.xo.android.view.chat.AudioPlayerView;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;



public class ChatAudioItem extends ChatMessageItem implements IViewListener {

    private MediaPlayerService mMediaPlayerService;
    private ImageButton mPlayPauseButton;
    private BroadcastReceiver mReceiver;
    private ServiceConnection mConnection;
    private AudioAttachmentItem mAudioContentObject;
    private boolean mIsPlayable = false;

    public ChatAudioItem(Context context, TalkClientMessage message) {
        super(context, message);
    }

    @Override
    public ChatItemType getType() {
        return ChatItemType.ChatItemWithAudio;
    }

    @Override
    protected void configureViewForMessage(View view) {
        super.configureViewForMessage(view);
        configureAttachmentViewForMessage(view);
    }

    @Override
    protected void displayAttachment(final IContentObject contentObject) {
        super.displayAttachment(contentObject);

        // add view lazily
        if (mContentWrapper.getChildCount() == 0) {
            LinearLayout audioLayout = new AudioPlayerView(mContext, this);
            TextView captionTextView = (TextView) audioLayout.findViewById(R.id.tv_content_audio_caption);
            TextView fileNameTextView = (TextView) audioLayout.findViewById(R.id.tv_content_audio_name);

            if(mMessage.isIncoming()) {
                captionTextView.setTextColor(Color.BLACK);
                fileNameTextView.setTextColor(Color.BLACK);
            } else {
                captionTextView.setTextColor(Color.WHITE);
                fileNameTextView.setTextColor(Color.WHITE);
            }

            String extension = contentObject.getContentDataUrl();
            extension = extension.substring(extension.lastIndexOf("."), extension.length());
            fileNameTextView.setText(contentObject.getFileName() + extension);

            mPlayPauseButton = (ImageButton) audioLayout.findViewById(R.id.ib_content_audio_play);
            mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
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
            });
            mContentWrapper.addView(audioLayout);

            mAudioContentObject = AudioAttachmentItem.create(contentObject.getContentDataUrl(), contentObject);
            if (mAudioContentObject == null) {
                mIsPlayable = false;
            } else {
                mIsPlayable = true;
            }
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
    }

    private void pausePlaying() {
        if (isBound()) {
            mMediaPlayerService.pause();
        }
    }

    private void startPlaying() {
        if (isBound()) {
            if (mMediaPlayerService.isPaused() && mMediaPlayerService.getCurrentMediaItem() != null && mAudioContentObject.equals(mMediaPlayerService.getCurrentMediaItem())) {
                mMediaPlayerService.play();
            } else {
                mMediaPlayerService.setMedia(mAudioContentObject);
                mMediaPlayerService.play(0);
            }
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
    public void onAttachedToWindow() {
        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.startService(intent);
        bindService(intent);

        createBroadcastReceiver();
    }

    @Override
    public void onDetachedFromWindow() {
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
