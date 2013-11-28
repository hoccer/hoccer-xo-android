package com.hoccer.xo.android.content.vcard;

import android.app.Activity;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.android.view.ContactView;

public class ContactViewer extends ContentViewer<ContactView> {

    @Override
    public boolean canViewObject(IContentObject object) {
        if(object.getContentMediaType().equals("contact")) {
            return true;
        }
        return false;
    }

    @Override
    protected ContactView makeView(Activity activity) {
        return new ContactView(activity);
    }

    @Override
    protected void updateView(ContactView view, ContentView contentView, IContentObject contentObject) {
        view.showContent(contentObject);
    }

}
