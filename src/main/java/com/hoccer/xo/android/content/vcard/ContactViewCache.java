package com.hoccer.xo.android.content.vcard;

import android.app.Activity;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewCache;

public class ContactViewCache extends ContentViewCache<ContactView> {

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
