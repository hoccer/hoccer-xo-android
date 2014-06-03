package com.hoccer.xo.android.view.chat.attachments;

import android.content.Context;
import android.view.View;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.android.view.chat.ChatMessageItem;


public class ChatLocationItem extends ChatMessageItem {

    public ChatLocationItem(Context context, TalkClientMessage message) {
        super(context, message);
    }

    @Override
    protected void configureViewForMessage(View view) {
        super.configureViewForMessage(view);

        // TODO: do additional configuration here
    }
}
