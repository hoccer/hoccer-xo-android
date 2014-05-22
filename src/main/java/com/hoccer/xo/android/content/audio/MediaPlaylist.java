package com.hoccer.xo.android.content.audio;

import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.xo.android.content.MediaItem;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MediaPlaylist implements ListIterator<MediaItem> {

    public static final int UNDEFINED_CONTACT_ID = -1;
    private static final Logger LOG = Logger.getLogger(MediaPlaylist.class);

    private List<MediaItem> mPlaylistItems = new ArrayList<MediaItem>();
    private int mConversationContactId = UNDEFINED_CONTACT_ID;
    private int mCurrentIndex = 0;

    public MediaPlaylist(String mediaFilePath) {
        mPlaylistItems.add(MediaItem.create(mediaFilePath));
    }

    public MediaPlaylist(List<TalkClientDownload> mAudioAttachmentList, int conversationContactId) {

        for (TalkClientDownload tcd : mAudioAttachmentList) {
            mPlaylistItems.add(MediaItem.create(tcd.getContentDataUrl()));
        }
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public int getConversationContactId() {
        return mConversationContactId;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public int size() {
        return mPlaylistItems.size();
    }

    @Override
    public boolean hasPrevious() {
        if (!mPlaylistItems.isEmpty()) {
            if (previousIndex() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        if (!mPlaylistItems.isEmpty()) {
            if (nextIndex() < mPlaylistItems.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MediaItem previous() {
        if ( mCurrentIndex == 0){
            mCurrentIndex = mPlaylistItems.size() - 1;
        }else{
            --mCurrentIndex;
        }
        return mPlaylistItems.get(mCurrentIndex);
    }

    @Override
    public MediaItem next() {
        if ( mCurrentIndex == mPlaylistItems.size() - 1){
            mCurrentIndex = 0;
        }else {
            ++mCurrentIndex;
        }
        return mPlaylistItems.get(mCurrentIndex);
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
        LOG.error("Removing items from playlist is not supported.");
    }

    @Override
    public void set(MediaItem talkClientDownload) {
        int index = mPlaylistItems.indexOf(talkClientDownload);
        if(index >= 0) {
            mCurrentIndex = index;
        }
        else {
            LOG.error("Try to set playlist to unknown item.");
        }
    }

    @Override
    public void add(MediaItem talkClientDownload) {
        LOG.error("Removing items from playlist is not supported.");
    }

    public MediaItem current() {
        return mPlaylistItems.get(mCurrentIndex);
    }
}
