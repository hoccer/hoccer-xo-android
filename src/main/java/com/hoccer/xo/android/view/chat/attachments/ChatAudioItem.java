package com.hoccer.xo.android.view.chat.attachments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.service.MediaPlayerServiceConnector;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;


public class ChatAudioItem extends ChatMessageItem {

    private ImageButton mPlayPauseButton;
    private MediaPlayerServiceConnector mMediaPlayerServiceConnector;
    private AudioAttachmentItem mAudioContentObject;
    private boolean mIsPlayable = false;

    public ChatAudioItem(Context context, TalkClientMessage message) {
        super(context, message);
        mMediaPlayerServiceConnector = new MediaPlayerServiceConnector();
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

        mAudioContentObject = AudioAttachmentItem.create(contentObject.getContentDataUrl(), contentObject, true);
        mIsPlayable = mAudioContentObject != null;
        updatePlayPauseView();
    }

    private void pausePlaying() {
        if (mMediaPlayerServiceConnector.isConnected()) {
            mMediaPlayerServiceConnector.getService().pause();
        }
    }

    private void startPlaying() {
        if (mMediaPlayerServiceConnector.isConnected()) {
            MediaPlayerService service = mMediaPlayerServiceConnector.getService();
            if (service.isPaused() && service.getCurrentMediaItem() != null && mAudioContentObject.equals(service.getCurrentMediaItem())) {
                service.play();
            } else {
                service.setMedia(mAudioContentObject);
                service.play(0);
            }
        }
    }

    public void updatePlayPauseView() {
        if ( mPlayPauseButton != null) {
            mPlayPauseButton.setImageResource((isActive()) ? R.drawable.ic_dark_pause : R.drawable.ic_dark_play);
        }
    }

    public boolean isActive() {
        boolean isActive = false;
        if (mAudioContentObject != null && mMediaPlayerServiceConnector.isConnected()) {
            MediaPlayerService service = mMediaPlayerServiceConnector.getService();
            isActive = !service.isPaused() && !service.isStopped() && mAudioContentObject.equals(service.getCurrentMediaItem());
        }

        return isActive;
    }

    private void initializeMediaPlayerService(){
        mMediaPlayerServiceConnector.connect(mContext,
                MediaPlayerService.PLAYSTATE_CHANGED_ACTION,
                new MediaPlayerServiceConnector.Listener() {
                    @Override
                    public void onConnected(MediaPlayerService service) {
                        updatePlayPauseView();
                    }
                    @Override
                    public void onDisconnected() {
                    }
                    @Override
                    public void onAction(String action, MediaPlayerService service) {
                        updatePlayPauseView();
                    }
                });
    }

    private void destroyMediaPlayerService(){
        mMediaPlayerServiceConnector.disconnect();
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
}
