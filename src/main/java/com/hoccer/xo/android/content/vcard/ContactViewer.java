package com.hoccer.xo.android.content.vcard;

import android.app.Activity;
import android.view.View;
import com.hoccer.xo.android.content.ContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.IContentViewer;
import com.hoccer.xo.android.view.ContactView;

public class ContactViewer implements IContentViewer {

    @Override
    public boolean canViewObject(ContentObject object) {
        if(object.getMediaType().equals("contact")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Activity activity, ContentObject object, ContentView view) {
        ContactView v = new ContactView(activity);

        v.showContent(object.getContentUrl());

        return v;
    }

}
