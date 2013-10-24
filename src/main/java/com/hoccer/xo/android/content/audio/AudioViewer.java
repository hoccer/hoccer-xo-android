package com.hoccer.xo.android.content.audio;

import android.app.Activity;
import android.view.View;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.IContentViewer;
import com.hoccer.xo.android.view.AudioPlayerView;

public class AudioViewer implements IContentViewer {

    @Override
    public boolean canViewObject(IContentObject object) {
        if(object.getContentMediaType().equals("audio")) {
            return true;
        }
        return false;
    }

    @Override
    public View getViewForObject(Activity context, IContentObject object, ContentView view) {
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
