package com.hoccer.xo.android.view.chat.attachments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.view.chat.ChatMessageItem;


public class ChatDataItem extends ChatMessageItem {

    public ChatDataItem(Context context, TalkClientMessage message) {
        super(context, message);
    }

    @Override
    public ChatItemType getType() {
        return ChatItemType.ChatItemWithData;
    }

    @Override
    protected void configureViewForMessage(View view) {
        super.configureViewForMessage(view);
        configureAttachmentViewForMessage(view);
    }

    @Override
    protected void displayAttachment(IContentObject contentObject, boolean isIncoming) {
        super.displayAttachment(contentObject, isIncoming);

        // add view lazily
        if (mContentWrapper.getChildCount() == 0) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //RelativeLayout dataLayout = (RelativeLayout) inflater.inflate(R.layout.content_data, null);
            //mContentWrapper.addView(dataLayout);
        }


    }

}
