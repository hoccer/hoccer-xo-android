package com.hoccer.xo.android.content.audio;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewCache;
import com.hoccer.xo.android.view.AudioPlayerView;

import android.app.Activity;
import com.hoccer.xo.release.R;

import java.io.File;

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
            updateFilenameText(view, contentObject, isLightTheme);
            view.setFile(contentObject.getContentDataUrl());
        }
    }

    private void updateFilenameText(View view, IContentObject contentObject, boolean isLightTheme) {

        TextView filenameText = (TextView) view.findViewById(R.id.tv_content_audio_name);
        String filename = contentObject.getFileName();

        if (filename == null) {
            String dataUrl = contentObject.getContentDataUrl();
            filename = dataUrl.substring(dataUrl.lastIndexOf(File.separator) + 1);
        }

        filenameText.setText(filename);
        if (isLightTheme) {
            filenameText.setTextColor(Color.BLACK);
        } else {
            filenameText.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void clearViewInternal(AudioPlayerView view) {
    }
}
