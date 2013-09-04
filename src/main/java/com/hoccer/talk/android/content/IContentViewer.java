package com.hoccer.talk.android.content;

import android.app.Activity;
import android.view.View;

public interface IContentViewer {

    public abstract boolean canViewObject(ContentObject object);

    public abstract View getViewForObject(Activity activity, ContentObject object, ContentView view);

}
