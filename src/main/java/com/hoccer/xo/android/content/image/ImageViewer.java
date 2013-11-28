package com.hoccer.xo.android.content.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.IContentViewer;
import com.hoccer.xo.android.view.AspectImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import org.apache.log4j.Logger;

import java.util.WeakHashMap;

public class ImageViewer implements IContentViewer, ImageLoadingListener {

    private static final Logger LOG = Logger.getLogger(ImageViewer.class);

    WeakHashMap<ImageView, String> mUpdateCache = new WeakHashMap<ImageView, String>();
    WeakHashMap<ContentView, AspectImageView> mViewCache = new WeakHashMap<ContentView, AspectImageView>();

    @Override
    public boolean canViewObject(IContentObject object) {
        if(object.getContentMediaType().equals("image")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Activity context, IContentObject object, ContentView contentView) {
        if(!canViewObject(object)) {
            return null;
        }

        AspectImageView view = mViewCache.get(contentView);

        if(view == null) {
            view = new AspectImageView(context);
            mViewCache.put(contentView, view);
        }

        int maxContentHeight = contentView.getMaxContentHeight();
        if(maxContentHeight != Integer.MAX_VALUE) {
            view.setMaxHeight(maxContentHeight);
        }
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setAspectRatio(object.getContentAspectRatio());
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        String contentUrl = object.getContentUrl();
        if(object.isContentAvailable() && contentUrl != null) {
            updateView(view, contentUrl);
        }

        return view;
    }

    private void updateView(ImageView view, String contentUrl) {
        String oldUrl = mUpdateCache.get(view);
        if(oldUrl == null || !oldUrl.equals(contentUrl)) {
            mUpdateCache.put(view, contentUrl);
            view.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(contentUrl, view, this);
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
