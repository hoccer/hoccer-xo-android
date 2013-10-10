package com.hoccer.talk.android.content;

import android.app.Activity;
import android.view.View;

/**
 * Content viewers can create views for specific content objects
 */
public interface IContentViewer {

    /** Returns true if the viewer can handle the given object */
    public abstract boolean canViewObject(ContentObject object);

    /** Returns a readily initialized view for the given content */
    public abstract View getViewForObject(Activity activity, ContentObject object, ContentView view);

}
