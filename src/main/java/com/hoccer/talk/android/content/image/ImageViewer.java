package com.hoccer.talk.android.content.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentViewer;
import org.apache.log4j.Logger;

import java.util.zip.Inflater;

public class ImageViewer extends ContentViewer {

    private static final Logger LOG = Logger.getLogger(ImageViewer.class);

    @Override
    public boolean canViewObject(ContentObject object) {
        if(object.getMediaType().equals("image")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Context context, ContentObject object) {
        if(!canViewObject(object)) {
            return null;
        }
        LOG.info("constructing image drawable");
        Drawable drawable = Drawable.createFromPath(object.getContentUrl());
        LOG.info("constructing image view");
        ImageView view = new ImageView(context);
        LOG.info("applying drawable");
        view.setImageDrawable(drawable);
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

}
