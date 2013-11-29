package com.hoccer.xo.android.content.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.android.view.AspectImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import org.apache.log4j.Logger;

import java.util.WeakHashMap;

public class ImageViewer extends ContentViewer<AspectImageView> implements ImageLoadingListener {

    private static final Logger LOG = Logger.getLogger(ImageViewer.class);

    WeakHashMap<ImageView, String> mUpdateCache = new WeakHashMap<ImageView, String>();

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("image");
    }

    @Override
    protected AspectImageView makeView(Activity activity) {
        return new AspectImageView(activity);
    }

    @Override
    protected void updateView(AspectImageView view, ContentView contentView, IContentObject contentObject) {
        int maxContentHeight = contentView.getMaxContentHeight();
        if(maxContentHeight != Integer.MAX_VALUE) {
            view.setMaxHeight(maxContentHeight);
        }
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setAspectRatio(contentObject.getContentAspectRatio());
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        String contentUrl = contentObject.getContentDataUrl();
        if(contentObject.isContentAvailable() && contentUrl != null) {
            loadImage(view, contentUrl);
        } else {
            view.setImageDrawable(null);
        }
    }

    private void loadImage(ImageView view, String contentUrl) {
        String oldUrl = mUpdateCache.get(view);
        if(oldUrl == null || !oldUrl.equals(contentUrl)) {
            mUpdateCache.put(view, contentUrl);
            view.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(
                    contentUrl, view, XoApplication.getContentImageOptions(), this);
        }
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        LOG.debug("load of " + imageUri + " started");
    }
    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        LOG.error("load of " + imageUri + " failed: " + failReason.getType().toString(), failReason.getCause());
    }
    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        LOG.debug("load of " + imageUri + " complete");
    }
    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        LOG.debug("load of " + imageUri + " cancelled");
    }

}

