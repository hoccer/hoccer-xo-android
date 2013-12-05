package com.hoccer.xo.android.content.audio;

import android.app.Activity;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.android.view.AudioPlayerView;

public class AudioViewer extends ContentViewer<AudioPlayerView> {

    @Override
    public boolean canViewObject(IContentObject object) {
        if(object.getContentMediaType().equals("audio")) {
            return true;
        }
        return false;
    }

    @Override
    protected AudioPlayerView makeView(Activity activity) {
        return new AudioPlayerView(activity);
    }

    @Override
    protected void updateViewInternal(AudioPlayerView view, ContentView contentView, IContentObject contentObject) {
        if(contentObject.getContentDataUrl() != null) {
            view.setFile(contentObject.getContentDataUrl());
        }
    }

    @Override
    protected void clearViewInternal(AudioPlayerView view) {
    }

}
