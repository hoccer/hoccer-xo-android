package com.hoccer.xo.android.content.image;

import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.android.content.IContentViewerListener;
import com.hoccer.xo.android.view.AspectImageView;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import java.util.WeakHashMap;

public class ImageViewer extends ContentViewer<View> implements ImageLoadingListener, IClickableImageViewListener {

    private static final Logger LOG = Logger.getLogger(ImageViewer.class);

    WeakHashMap<ImageView, String> mUpdateCache = new WeakHashMap<ImageView, String>();

    private ClickableImageView mImageView;

    private IContentObject mContentObject;

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("image");
    }

    @Override
    protected View makeView(Activity activity) {
        View view = View.inflate(activity, R.layout.content_image, null);
        mImageView = (ClickableImageView) view.findViewById(R.id.image_show_button);
        return view;
    }

    @Override
    protected void updateViewInternal(final View view, ContentView contentView,
            final IContentObject contentObject, boolean isLightTheme) {

        mContentObject = contentObject;
        String contentUrl = contentObject.getContentDataUrl();

        if (contentObject.isContentAvailable() && contentUrl != null) {

            mImageView.setClickableImageViewListener(this);
            loadImage(mImageView, contentUrl);

        } else {
            clearViewInternal(view);
        }
    }

    @Override
    public void onImageViewClick(ClickableImageView view) {
        LOG.debug("handling click");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mContentObject.getContentDataUrl()), "image/*");
        try {
            Activity activity = (Activity) view.getContext();
            activity.startActivity(intent);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageViewLongClick(ClickableImageView view) {
        LOG.debug("handling long click");

        mContentViewerListener.onContentViewerLongClick(this);
    }

    @Override
    protected void clearViewInternal(View view) {
        LOG.trace("clearing");
        ImageLoader.getInstance().cancelDisplayTask(mImageView);
        mImageView.setImageDrawable(null);
    }

    private void loadImage(ImageView view, String contentUrl) {
        String oldUrl = mUpdateCache.get(view);
        if (oldUrl == null || !oldUrl.equals(contentUrl)) {
            LOG.trace("triggering load of " + contentUrl);
            mUpdateCache.put(view, contentUrl);
            view.setImageDrawable(null);

            int cornerRadiusInPixels = getCornerRadius(view);

            DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                    .cloneFrom(XoApplication.getContentImageOptions())
                    .displayer(new RoundedBitmapDisplayer(cornerRadiusInPixels)).build();

            ImageLoader.getInstance().displayImage(
                    contentUrl, view, displayOptions, this);
        } else {
            LOG.trace("not triggering load of " + contentUrl);
        }
    }

    private int getCornerRadius(ImageView view) {
        float cornerRadiusInDP = view.getResources()
                .getDimension(R.dimen.xo_message_corner_radius);
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadiusInDP,
                        view.getResources().getDisplayMetrics());
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        LOG.debug("load of " + imageUri + " started");
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        LOG.error("load of " + imageUri + " failed: " + failReason.getType().toString(),
                failReason.getCause());
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        LOG.debug("load of " + imageUri + " complete");
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        LOG.debug("load of " + imageUri + " cancelled");
    }

    /**
     * This is specific to using AspectImageView with UIL
     * and most badly needed here, so we do it here.
     */
    private class CancelingAspectImageView extends AspectImageView {

        public CancelingAspectImageView(Context context) {
            super(context);
        }

        @Override
        protected void onDetachedFromWindow() {
            LOG.trace("clear on detach");
            super.onDetachedFromWindow();
            ImageLoader.getInstance().cancelDisplayTask(this);
        }
    }

}
