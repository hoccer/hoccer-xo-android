package com.hoccer.talk.android.content.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentViewer;

import java.util.zip.Inflater;

public class ImageViewer extends ContentViewer {

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
        ImageView view = new ImageView(context);
        view.setImageDrawable(Drawable.createFromPath(object.getContentUrl()));
        return view;
    }
}
