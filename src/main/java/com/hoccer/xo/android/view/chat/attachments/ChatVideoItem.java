package com.hoccer.xo.android.view.chat.attachments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.view.Gravity;
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
        ImageView thumbnailView = (ImageView) mContentWrapper.findViewById(R.id.iv_video_preview);
        RelativeLayout rootView = (RelativeLayout) mContentWrapper.findViewById(R.id.rl_root);

        int textColor;
        int mask;

        if (mMessage.isIncoming()) {
            textColor = Color.BLACK;
            rootView.setGravity(Gravity.LEFT);
            mask = R.drawable.bubble_grey;
        } else {
            textColor = Color.WHITE;
            rootView.setGravity(Gravity.RIGHT);
            mask = R.drawable.bubble_green;
        }

        videoTitle.setTextColor(textColor);
        videoDescription.setTextColor(textColor);

        String tag = (mMessage.getMessageId() != null) ? mMessage.getMessageId() : mMessage.getMessageTag();
        thumbnailView.setVisibility(View.INVISIBLE);

        if (contentObject.getContentDataUrl() != null) {
            mAttachmentView.setBackgroundDrawable(null);
            ThumbnailManager.getInstance(mContext).displayThumbnailForVideo(contentObject.getContentDataUrl(), rootView, mask, tag);
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
