package com.hoccer.xo.android.content.audio;

import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewCache;
import com.hoccer.xo.android.view.AudioPlayerView;

import android.app.Activity;

public class AudioViewCache extends ContentViewCache<AudioPlayerView> {

    @Override
    public boolean canViewObject(IContentObject object) {
        if (object.getContentMediaType().equals("audio")) {
            return true;
        }
        return false;
    }

    @Override
    protected AudioPlayerView makeView(Activity activity) {

        return new AudioPlayerView(activity);
    }

    @Override
    protected void updateViewInternal(AudioPlayerView view, ContentView contentView,
            IContentObject contentObject, boolean isLightTheme) {

        if (contentObject.getContentDataUrl() != null) {
            view.setFile(contentObject.getContentDataUrl());
        }
    }

    @Override
    protected void clearViewInternal(AudioPlayerView view) {
    }

}
