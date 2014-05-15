package com.hoccer.xo.android.content.audio;

import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.xo.android.content.MediaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MediaPlaylist implements ListIterator<MediaItem> {

    private List<MediaItem> playlistItems = new ArrayList<MediaItem>();

    private int mCurrentIndex = 0;

    private MediaPlaylist() {
    }

    @Override
    public boolean hasPrevious() {
        if (!playlistItems.isEmpty()) {
            if (previousIndex() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        if (!playlistItems.isEmpty()) {
            if (nextIndex() < playlistItems.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MediaItem previous() {
        return playlistItems.get(--mCurrentIndex);
    }

    @Override
    public MediaItem next() {
        return playlistItems.get(++mCurrentIndex);
    }


    @Override
    public int previousIndex() {
        return mCurrentIndex - 1;
    }

    @Override
    public int nextIndex() {
        return mCurrentIndex + 1;
    }

    @Override
    public void remove() {
    }

    @Override
    public void set(MediaItem talkClientDownload) {

    }

    @Override
    public void add(MediaItem talkClientDownload) {

    }

    public MediaItem current() {
        return playlistItems.get(mCurrentIndex);
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public static MediaPlaylist create(String mediaFilePath) {

        MediaPlaylist pl = new MediaPlaylist();
        pl.playlistItems.add(MediaItem.create(mediaFilePath));
        return pl;
    }

    public static MediaPlaylist create(List<TalkClientDownload> mAudioAttachmentList) {

        MediaPlaylist pl = new MediaPlaylist();
        for (TalkClientDownload tcd : mAudioAttachmentList) {
            pl.playlistItems.add(MediaItem.create(tcd.getContentDataUrl()));
        }
        return pl;
    }
}
