package com.hoccer.talk.android.content.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentView;
import com.hoccer.talk.android.content.IContentViewer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import org.apache.log4j.Logger;

public class ImageViewer implements IContentViewer {

    private static final Logger LOG = Logger.getLogger(ImageViewer.class);

    @Override
    public boolean canViewObject(ContentObject object) {
        if(object.getMediaType().equals("image")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Context context, ContentObject object, ContentView contentView) {
        if(!canViewObject(object)) {
            return null;
        }
        LOG.info("constructing image view");
        ImageView view = new ImageView(context);
        if(contentView.getMaxHeight() >= 0) {
            view.setMaxHeight(contentView.getMaxHeight());
        }
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ImageLoader.getInstance().displayImage("file://" + object.getContentUrl(), view, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                LOG.info("load of " + imageUri + " started");
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                LOG.error("load of " + imageUri + " failed: " + failReason.getType().toString(), failReason.getCause());
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                LOG.info("load of " + imageUri + " complete");
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                LOG.info("load of " + imageUri + " cancelled");
            }
        });
        return view;
    }

}
