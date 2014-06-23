package com.hoccer.xo.android.view.chat.attachments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;


public class ChatAudioItem extends ChatMessageItem {

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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout audioLayout = (LinearLayout) inflater.inflate(R.layout.content_audio, null);
            TextView captionTextView = (TextView) audioLayout.findViewById(R.id.tv_content_audio_caption);
            TextView fileNameTextView = (TextView) audioLayout.findViewById(R.id.tv_content_audio_name);
            ImageButton playButton = (ImageButton) audioLayout.findViewById(R.id.ib_content_audio_play);

            int textColor = -1;
            int iconId = -1;
            if(mMessage.isIncoming()) {
                textColor = R.color.xo_incoming_message_textColor;
                iconId = R.drawable.ic_dark_music;
            } else {
                textColor = R.color.xo_compose_message_textColor;
                iconId = R.drawable.ic_light_music;
            }

            captionTextView.setTextColor(textColor);
            fileNameTextView.setTextColor(textColor);
            playButton.setImageResource(iconId);

            String extension = contentObject.getContentDataUrl();
            extension = extension.substring(extension.lastIndexOf("."), extension.length());
            fileNameTextView.setText(contentObject.getFileName() + extension);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (contentObject.isContentAvailable()) {
                        String url = contentObject.getContentUrl();
                        if (url == null) {
                            url = contentObject.getContentDataUrl();
                        }
                        if (url != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            XoActivity activity = (XoActivity) view.getContext();
                            activity.startExternalActivity(intent);
                        }
                    }
                }
            });
            mContentWrapper.addView(audioLayout);
        }

    }
}
