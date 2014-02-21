package com.hoccer.xo.android.content.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        return new CancelingAspectImageView(activity);
    }

    @Override
    protected void updateViewInternal(AspectImageView view, ContentView contentView, IContentObject contentObject) {
        int maxContentHeight = contentView.getMaxContentHeight();
        if(maxContentHeight != Integer.MAX_VALUE) {
            view.setMaxHeight(maxContentHeight);
        }
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.setAspectRatio(contentObject.getContentAspectRatio());
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        String contentUrl = contentObject.getContentDataUrl();
        if(contentObject.isContentAvailable() && contentUrl != null) {
            loadImage(view, contentUrl);
        } else {
            clearViewInternal(view);
        }
    }

    @Override
    protected void clearViewInternal(AspectImageView view) {
        LOG.trace("clearing");
        ImageLoader.getInstance().cancelDisplayTask(view);
        view.setImageDrawable(null);
    }

    private void loadImage(ImageView view, String contentUrl) {
        String oldUrl = mUpdateCache.get(view);
        if(oldUrl == null || !oldUrl.equals(contentUrl)) {
            LOG.trace("triggering load of " + contentUrl);
            mUpdateCache.put(view, contentUrl);
            view.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(
                    contentUrl, view, XoApplication.getContentImageOptions(), this);
        } else {
            LOG.trace("not triggering load of " + contentUrl);
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
