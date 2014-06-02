package com.hoccer.xo.android.view.chat.attachments;

import android.content.Context;
import android.view.View;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.android.view.chat.ChatMessageItem;


public class ChatDataItem extends ChatMessageItem {

    public ChatDataItem(Context context) {
        super(context);
    }

    @Override
    protected void configureViewForMessage(View view, TalkClientMessage message) {
        super.configureViewForMessage(view, message);

        // TODO: do additional configuration here
    }
}
