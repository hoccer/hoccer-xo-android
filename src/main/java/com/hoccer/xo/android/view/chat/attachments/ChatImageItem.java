package com.hoccer.xo.android.view.chat.attachments;

import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.util.ImageLoader;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class ChatImageItem extends ChatMessageItem {

    private Context mContext;

    public ChatImageItem(Context context, TalkClientMessage message) {
        super(context, message);
        mContext = context;
    }

    public ChatItemType getType() {
        return ChatItemType.ChatItemWithImage;
    }

    @Override
    protected void configureViewForMessage(View view) {
        super.configureViewForMessage(view);
        configureAttachmentViewForMessage(view);
    }

    @Override
    protected void displayAttachment(final IContentObject contentObject) {
        super.displayAttachment(contentObject);
        mAttachmentView.setPadding(0, 0, 0, 0);
        mAttachmentView.setBackgroundDrawable(null);
        // add view lazily
        if (mContentWrapper.getChildCount() == 0) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout imageLayout = (RelativeLayout) inflater
                    .inflate(R.layout.content_image, null);
            mContentWrapper.addView(imageLayout);
        }

        mContentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImage(contentObject);
            }
        });

        ImageView imageView = (ImageView) mContentWrapper.findViewById(R.id.iv_image_view);
        RelativeLayout rootView = (RelativeLayout) mContentWrapper.findViewById(R.id.rl_root);
        imageView.setVisibility(View.INVISIBLE);
        if (mMessage.isIncoming()) {
            rootView.setGravity(Gravity.LEFT);
        } else {
            rootView.setGravity(Gravity.RIGHT);
        }
        imageView.setVisibility(View.INVISIBLE);
        ImageLoader.getInstance(mContext).displayImage(contentObject.getContentDataUrl(), imageView, mMessage.isIncoming());
    }

    private void displayImage(IContentObject contentObject) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(contentObject.getContentDataUrl()), "image/*");
        try {
            XoActivity activity = (XoActivity) mContext;
            activity.startExternalActivity(intent);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

}
