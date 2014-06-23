package com.hoccer.xo.android.view.chat.attachments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.util.ColorSchemeManager;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;



public class ChatAudioItem extends ChatMessageItem {

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
        if (mContentWrapper.getChildCount() == 0)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View v =  inflater.inflate(R.layout.content_audio, null);
            mContentWrapper.addView(v);
        }
        LinearLayout audioLayout = (LinearLayout) mContentWrapper.getChildAt(0);

        TextView captionTextView = (TextView) audioLayout.findViewById(R.id.tv_content_audio_caption);
        TextView fileNameTextView = (TextView) audioLayout.findViewById(R.id.tv_content_audio_name);
        mPlayPauseButton = (ImageButton) audioLayout.findViewById(R.id.ib_content_audio_play);
        setPlayButton();

        if(mMessage.isIncoming()) {
            captionTextView.setTextColor(mContext.getResources().getColor(R.color.xo_incoming_message_textColor));
            fileNameTextView.setTextColor(mContext.getResources().getColor(R.color.xo_incoming_message_textColor));
        } else {
            captionTextView.setTextColor(mContext.getResources().getColor(R.color.xo_compose_message_textColor));
            fileNameTextView.setTextColor(mContext.getResources().getColor(R.color.xo_compose_message_textColor));
        }

        String extension = contentObject.getContentDataUrl();
        extension = extension.substring(extension.lastIndexOf("."), extension.length());
        fileNameTextView.setText(contentObject.getFileName() + extension);

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

        mAudioContentObject = AudioAttachmentItem.create(contentObject.getContentDataUrl(), contentObject);

        mIsPlayable = mAudioContentObject != null;
        updatePlayPauseView();
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

    private void setPlayButton(){
        mPlayPauseButton.setBackgroundDrawable(null);
        mPlayPauseButton.setBackgroundDrawable(ColorSchemeManager.fillAttachmentForeground(mContext, R.drawable.ic_light_play, mMessage.isIncoming()));
    }

    private void setPauseButton(){
        mPlayPauseButton.setBackgroundDrawable(null);
        mPlayPauseButton.setBackgroundDrawable(ColorSchemeManager.fillAttachmentForeground(mContext, R.drawable.ic_light_pause, mMessage.isIncoming()));
    }

    public void updatePlayPauseView() {
        if(mPlayPauseButton != null && mMessage != null) {
            if (isActive()) {
                setPauseButton();
            } else {
                setPlayButton();
            }
        }
    }

    public boolean isActive() {
        boolean isActive = false;
        if (mAudioContentObject != null && isBound()) {
            isActive = !mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped() && mAudioContentObject.equals(mMediaPlayerService.getCurrentMediaItem());
        }

        return isActive;
    }

    private void initializeMediaPlayerService(){
        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.startService(intent);
        bindService(intent);

        createBroadcastReceiver();
    }

    private void destroyMediaPlayerService(){
        mContext.unbindService(mConnection);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void setVisibility(boolean visible) {
        if ( mVisible == visible){
            return;
        }

        super.setVisibility(visible);

        if( mVisible){
            initializeMediaPlayerService();
        }else{
            destroyMediaPlayerService();
        }
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
