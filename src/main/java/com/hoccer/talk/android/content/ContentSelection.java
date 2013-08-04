package com.hoccer.talk.android.content;

import android.app.Activity;

public class ContentSelection {

    Activity mActivity;

    ContentSelector mSelector;

    public ContentSelection(Activity activity) {
        mActivity = activity;
    }

    public ContentSelection(Activity activity, ContentSelector selector) {
        mActivity = activity;
        mSelector = selector;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public ContentSelector getSelector() {
        return mSelector;
    }

    public void setSelector(ContentSelector selector) {
        this.mSelector = selector;
    }

}
