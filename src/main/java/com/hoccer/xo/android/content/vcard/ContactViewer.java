package com.hoccer.xo.android.content.vcard;

import android.app.Activity;
import android.view.View;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.IContentViewer;
import com.hoccer.xo.android.view.ContactView;

public class ContactViewer implements IContentViewer {

    @Override
    public boolean canViewObject(IContentObject object) {
        if(object.getContentMediaType().equals("contact")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Activity activity, IContentObject object, ContentView view) {
        ContactView v = new ContactView(activity);

        v.showContent(object.getContentUrl());

        return v;
    }

}
