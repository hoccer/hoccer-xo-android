package com.hoccer.xo.android.content;

import android.app.Activity;
import android.view.View;
import com.hoccer.talk.content.IContentObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Content view caches can create and manage views for specific content objects
 */
public abstract class ContentViewCache<V extends View> {

    private static final int MAX_CACHED_VIEWS = 3;

    protected Logger LOG = null;

    WeakHashMap<Activity, List<V>> mViewCache = new WeakHashMap<Activity, List<V>>();

    protected ContentViewCache() {
        LOG = Logger.getLogger(this.getClass());
    }

    public abstract boolean canViewObject(IContentObject object);

    protected abstract V makeView(Activity activity);

    protected abstract void updateViewInternal(V view, ContentView contentView, IContentObject contentObject, boolean isLightTheme);
    protected abstract void clearViewInternal(V view);

    public V getViewForObject(Activity activity, ContentView contentView, IContentObject contentObject, boolean isLightTheme) {
        V view = getView(activity, contentView);
        updateViewInternal(view, contentView, contentObject, isLightTheme);
        return view;
    }

    public void returnView(Activity activity, View view) {
        returnViewInternal(activity, (V) view);
    }

    private void returnViewInternal(Activity activity, V view) {
        List<V> cache = getCache(activity);
        clearViewInternal(view);
        if(cache.size() < MAX_CACHED_VIEWS) {
            cache.add(view);
        }
    }

    public void updateView(View view, ContentView contentView, IContentObject contentObject, boolean isLightTheme) {
        updateViewInternal((V)view, contentView, contentObject, isLightTheme);
    }

    public void clearView(View view) {
        clearViewInternal((V)view);
    }

    private V getView(Activity activity, ContentView contentView) {
        List<V> cache = getCache(activity);
        if(cache.isEmpty()) {
            return makeView(activity);
        } else {
            return cache.remove(0);
        }
    }

    private List<V> getCache(Activity activity) {
        List<V> cache = mViewCache.get(activity);
        if(cache == null) {
            cache = new ArrayList<V>();
            mViewCache.put(activity, cache);
        }
        return cache;
    }
}
