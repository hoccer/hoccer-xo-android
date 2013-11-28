package com.hoccer.xo.android.content;

import android.app.Activity;
import android.view.View;
import com.hoccer.talk.content.IContentObject;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Content viewers can create views for specific content objects
 */
public abstract class ContentViewer<V extends View> {

    WeakHashMap<ContentView, WeakReference<V>> mViewCache =
            new WeakHashMap<ContentView, WeakReference<V>>();

    public abstract boolean canViewObject(IContentObject object);

    protected abstract V makeView(Activity activity);

    protected abstract void updateView(V view, ContentView contentView, IContentObject contentObject);

    public V getViewForObject(Activity activity, ContentView contentView, IContentObject contentObject) {
        V view = getView(activity, contentView);
        updateView(view, contentView, contentObject);
        return view;
    }

    protected V getView(Activity activity, ContentView contentView) {
        V view = null;

        WeakReference<V> viewReference = mViewCache.get(contentView);

        if(viewReference != null) {
            view = viewReference.get();
        }

        if(view == null) {
            view = makeView(activity);
            mViewCache.put(contentView, new WeakReference<V>(view));
        }

        return view;
    }

}
