package com.hoccer.talk.android.content;

import android.app.Activity;

public class ContentSelection {

    Activity mActivity;

    ContentSelector mSelector;

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

}
