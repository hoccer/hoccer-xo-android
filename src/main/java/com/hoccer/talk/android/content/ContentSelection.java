package com.hoccer.talk.android.content;

import android.app.Activity;

/**
 * Handle object for content selection
 *
 * Contains all state required for a content selection.
 *
 */
public class ContentSelection {

    /** Initiating activity */
    Activity mActivity;

    /** Chosen content selector */
    IContentSelector mSelector;

    /**
     * Constructor for cases where the selector is not known
     * @param activity
     */
    public ContentSelection(Activity activity) {
        mActivity = activity;
    }

    /**
     * Constructor for cases where the selector is known
     * @param activity
     * @param selector
     */
    public ContentSelection(Activity activity, IContentSelector selector) {
        mActivity = activity;
        mSelector = selector;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public IContentSelector getSelector() {
        return mSelector;
    }

    public void setSelector(IContentSelector selector) {
        this.mSelector = selector;
    }

}
