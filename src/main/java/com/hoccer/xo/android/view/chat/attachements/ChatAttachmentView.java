package com.hoccer.xo.android.view.chat.attachements;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.release.R;


/**
 * This class represents a chat attachment. To configure the view only the TalkClientMessage object is needed.
 */
public class ChatAttachmentView extends LinearLayout {

    protected Context mContext;
    protected View mRootLayout;
    protected View mProgressIndicator;

    public ChatAttachmentView(Context context) {
        super(context);
        mContext = context;
        initializeView();
    }

    public ChatAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initializeView();
    }

    public ChatAttachmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initializeView();
    }

    protected View getRootLayout() {
        return mRootLayout;
    }

    protected void initializeView() {
        mRootLayout = LayoutInflater.from(mContext).inflate(R.layout.item_chat_content, null);
        addView(mRootLayout);

        mProgressIndicator = null; // TODO: add indicator to layout
    }

    public void configureWithMessage(TalkClientMessage message) {
        // TODO: configure view, attachment progress indicator
    }

}
