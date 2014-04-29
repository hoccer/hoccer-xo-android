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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class AudioViewCache extends ContentViewCache<AudioPlayerView> {

    private List<AudioPlayerView> mActivePlayerList = new ArrayList<AudioPlayerView>();

    AudioPlayerView mCurrentPlayerView;

    @Override
    public boolean canViewObject(IContentObject object) {
        if (object.getContentMediaType().equals("audio")) {
            return true;
        }
        return false;
    }

    @Override
    protected AudioPlayerView makeView(Activity activity) {

        AudioPlayerView newPlayer = new AudioPlayerView(activity, this);

        //mActivePlayerList.add(newPlayer);

        LOG.error("-----------------------------------------------------------: COUNT OF AUDIO PLAYERS: " + mActivePlayerList.size());

        return newPlayer;
    }

    @Override
    protected void updateViewInternal(AudioPlayerView view, ContentView contentView,
            IContentObject contentObject, boolean isLightTheme) {

        if (contentObject.getContentDataUrl() != null) {
            updateFilenameText(view, contentObject, isLightTheme);
            LOG.error("update: -----------------------------------------------------------:" + contentObject.getContentDataUrl());
            view.setFile(contentObject.getContentDataUrl());
        }
    }

    public void togglePlayback(boolean toggle, String currentPath, AudioPlayerView activeAudioPlayerView){
        // toggle = true ? playback started : stopped

        mCurrentPlayerView = activeAudioPlayerView;

        if ((toggle == true) && (currentPath != null)) {
            // check if any player is already active and stop it

//            activeAudioPlayerView

            Iterator<AudioPlayerView> iterator = mActivePlayerList.iterator();
            while (iterator.hasNext()) {

                AudioPlayerView currPlayer = iterator.next();

                if (currPlayer.isPlaying() && (!currentPath.equals(currPlayer.getCurrentPath()))){
                    currPlayer.pausePlaying();
                }
            }
        }
    }

    private void updateFilenameText(View view, IContentObject contentObject, boolean isLightTheme) {

        TextView filenameText = (TextView) view.findViewById(R.id.tv_content_audio_name);
        String filename = contentObject.getFileName();
        LOG.error("-----------------------------------------------------------: " + filename);

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
