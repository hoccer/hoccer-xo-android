package com.hoccer.xo.android.content;

import android.app.Activity;
import android.view.View;
import com.hoccer.talk.content.IContentObject;

/**
 * Content viewers can create views for specific content objects
 */
public interface IContentViewer {

    /** Returns true if the viewer can handle the given object */
    public abstract boolean canViewObject(IContentObject object);

    /** Returns a readily initialized view for the given content */
    public abstract View getViewForObject(Activity activity, IContentObject object, ContentView view);

}
