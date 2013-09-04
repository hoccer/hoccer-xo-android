package com.hoccer.talk.android.content.audio;

import android.app.Activity;
import android.view.View;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentView;
import com.hoccer.talk.android.content.IContentViewer;
import com.hoccer.talk.android.views.AudioPlayerView;

public class AudioViewer implements IContentViewer {

    @Override
    public boolean canViewObject(ContentObject object) {
        if(object.getMediaType().equals("audio")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Activity context, ContentObject object, ContentView view) {
        if(!canViewObject(object)) {
            return null;
        }
        AudioPlayerView player = new AudioPlayerView(context);
        if(object.getContentUrl() != null) {
            player.setFile(object.getContentUrl());
        }
        return player;
    }

}
