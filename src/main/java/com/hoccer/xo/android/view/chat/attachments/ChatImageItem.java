package com.hoccer.xo.android.view.chat.attachments;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


public class ChatImageItem extends ChatMessageItem implements ImageLoadingListener {

    private ClickableImageView mImageView;

    public ChatImageItem(Context context) {
        super(context);
    }

    @Override
    protected void configureViewForMessage(View view, TalkClientMessage message) {
        super.configureViewForMessage(view, message);

        configureAttachmentViewForMessage(view, message);
    }

    @Override
    protected void displayAttachment(IContentObject contentObject) {

        mContentTransferProgress.setVisibility(View.GONE);

        // add view lazily
        if (mContentWrapper.getChildCount() == 0) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout imageLayout = (RelativeLayout) inflater.inflate(R.layout.content_image, null);
            mContentWrapper.addView(imageLayout);
        }

        mImageView = (ClickableImageView) mContentWrapper.findViewById(R.id.civ_image_view);
        mContentWrapper.setVisibility(View.GONE);

        loadImage(mImageView, contentObject.getContentDataUrl());
    }

    private void loadImage(ImageView view, String contentUrl) {
        int cornerRadiusInPixels = getCornerRadius(view);

        DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                .cloneFrom(XoApplication.getContentImageOptions())
                .displayer(new RoundedBitmapDisplayer(cornerRadiusInPixels)).build();

        ImageLoader.getInstance().displayImage(contentUrl, view, displayOptions, this);
    }

    private int getCornerRadius(ImageView view) {
        float cornerRadiusInDP = view.getResources()
                .getDimension(R.dimen.xo_message_corner_radius);
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadiusInDP,
                        view.getResources().getDisplayMetrics());
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {}

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        mContentWrapper.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {}
}
