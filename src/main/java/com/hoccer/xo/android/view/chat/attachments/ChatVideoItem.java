package com.hoccer.xo.android.view.chat.attachments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.util.ThumbnailManager;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;


public class ChatVideoItem extends ChatMessageItem {

    public ChatVideoItem(Context context, TalkClientMessage message) {
        super(context, message);
    }

    @Override
    public ChatItemType getType() {
        return ChatItemType.ChatItemWithVideo;
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout videoLayout = (RelativeLayout) inflater.inflate(R.layout.content_video, null);
            mContentWrapper.addView(videoLayout);
        }

        TextView videoTitle = (TextView) mContentWrapper.findViewById(R.id.tv_video_title);
        TextView videoDescription = (TextView) mContentWrapper.findViewById(R.id.tv_video_description);
        ImageButton playButton = (ImageButton) mContentWrapper.findViewById(R.id.ib_content_open);
        final ImageView thumbnailView = (ImageView) mContentWrapper.findViewById(R.id.iv_video_preview);

        int textColor = -1;
        int iconId = -1;
        if (mMessage.isIncoming()) {
            textColor = Color.BLACK;
            iconId = R.drawable.ic_dark_music;
            iconId = R.drawable.ic_dark_video;
        } else {
            textColor = Color.WHITE;
            iconId = R.drawable.ic_light_music;
            iconId = R.drawable.ic_light_video;
        }

        videoTitle.setTextColor(textColor);
        videoDescription.setTextColor(textColor);
        playButton.setImageResource(iconId);

        int mask;
        if (mMessage.isIncoming()) {
            //rootView.setGravity(Gravity.LEFT);
            mask = R.drawable.bubble_grey;
        } else {
            //rootView.setGravity(Gravity.RIGHT);
            mask = R.drawable.bubble_green;
        }

        String tag = (mMessage.getMessageId() != null) ? mMessage.getMessageId() : mMessage.getMessageTag();

        if (contentObject.getContentDataUrl() != null) {
            ThumbnailManager.getInstance(mContext).displayThumbnailForVideo(contentObject.getContentDataUrl(), thumbnailView, mask, tag);
            //initImageView(contentObject.getContentDataUrl(), thumbnailView, true);
        }


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contentObject.isContentAvailable()) {
                    String url = contentObject.getContentUrl();
                    if (url == null) {
                        url = contentObject.getContentDataUrl();
                    }
                    if (url != null) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(url), "video/*");
                            XoActivity activity = (XoActivity) mContext;
                            activity.startExternalActivity(intent);
                        } catch (ActivityNotFoundException exception) {
                            Toast.makeText(mContext, R.string.error_no_videoplayer, Toast.LENGTH_LONG).show();
                            LOG.error("Exception while starting external activity ", exception);
                        }
                    }
                }
            }
        });
    }
}
