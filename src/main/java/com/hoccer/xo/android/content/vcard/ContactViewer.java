package com.hoccer.xo.android.content.vcard;

import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;

import android.app.Activity;

public class ContactViewer extends ContentViewer<ContactView> {

    @Override
    public boolean canViewObject(IContentObject object) {
        if (object.getContentMediaType().equals("vcard")) {
            return true;
        }
        return false;
    }

    @Override
    protected ContactView makeView(Activity activity) {
        return new ContactView(activity);
    }

    @Override
    protected void updateViewInternal(ContactView view, ContentView contentView,
            IContentObject contentObject, boolean isLightTheme) {
        view.showContent(contentObject, isLightTheme);
    }

    @Override
    protected void clearViewInternal(ContactView view) {
    }

}
