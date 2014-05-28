package com.hoccer.xo.android.view.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.hoccer.talk.client.model.TalkClientMessage;


public class ChatImageView extends ChatAttachmentView {

    private View mImageView;

    public ChatImageView(Context context) {
        super(context);
    }

    public ChatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void configureWithMessage(TalkClientMessage message) {
        super.configureWithMessage(message);

        // TODO: add imageview and hook up to the method signaling attachment transfer is finished. Display image then.
    }
}
