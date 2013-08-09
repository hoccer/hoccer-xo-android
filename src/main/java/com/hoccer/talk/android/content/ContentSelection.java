package com.hoccer.talk.android.content;

import android.app.Activity;

public class ContentSelection {

    Activity mActivity;

    IContentSelector mSelector;

    public ContentSelection(Activity activity) {
        mActivity = activity;
    }

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
