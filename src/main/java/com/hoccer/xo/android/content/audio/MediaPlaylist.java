package com.hoccer.xo.android.content.audio;

import com.hoccer.xo.android.content.MediaItem;
import org.apache.log4j.Logger;

import java.util.*;

public class MediaPlaylist implements ListIterator<MediaItem> {

    public static enum RepeatMode {
        REPEAT_TITLE, REPEAT_ALL, NO_REPEAT;
    }

   private static final Logger LOG = Logger.getLogger(MediaPlaylist.class);

    private List<MediaItem> mMediaItems = new ArrayList<MediaItem>();
    private List<MediaItem> mMediaItemsOriginalOrder;

    private RepeatMode mRepeatMode = RepeatMode.NO_REPEAT;
   private int mCurrentIndex = 0;
    private boolean shuffleActive = false;

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public int size() {
        return mMediaItems.size();
    }

    @Override
    public boolean hasPrevious() {
        if (!mMediaItems.isEmpty()) {
            if (previousIndex() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        if (!mMediaItems.isEmpty()) {
            if (nextIndex() < mMediaItems.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MediaItem previous() {
        if ( mCurrentIndex == 0){
            mCurrentIndex = mMediaItems.size() - 1;
        }else{
            --mCurrentIndex;
        }
        return mMediaItems.get(mCurrentIndex);
    }

    @Override
    public MediaItem next() {
        if ( mCurrentIndex == mMediaItems.size() - 1){
            mCurrentIndex = 0;
        } else {
            ++mCurrentIndex;
        }
        return mMediaItems.get(mCurrentIndex);
    }

    public MediaItem nextByRepeatMode() {
        switch (mRepeatMode) {
            case NO_REPEAT:
                if (hasNext()) {
                    return mMediaItems.get(++mCurrentIndex);
                }
                break;
            case REPEAT_ALL:
                return next();
            case REPEAT_TITLE:
                return current();
        }
        return null;
    }

    public void clear() {
        mMediaItems.clear();
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
    public void set(MediaItem item) {
        int index = mMediaItems.indexOf(item);
        if(index >= 0) {
            mCurrentIndex = index;
        } else {
            LOG.error("Try to set playlist to unknown item.");
        }
    }

    @Override
    public void add(MediaItem item) {
        LOG.error("Adding items at current position is not supported.");
    }

    public void add( int index, MediaItem item) {
        mMediaItems.add(index, item);
    }

    public void addAll(List<MediaItem> items) {
        mMediaItems.addAll(items);
    }

    public MediaItem current() {
        if((mCurrentIndex >= 0) && (mCurrentIndex < mMediaItems.size())) {
            return mMediaItems.get(mCurrentIndex);
        } else {
            return null;
        }
    }

    public RepeatMode getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.mRepeatMode = repeatMode;
    }

    public boolean isShuffleActive() {
        return shuffleActive;
    }

    public void setShuffleActive(boolean shuffleActive) {
        this.shuffleActive = shuffleActive;
        if (this.shuffleActive) {
            shufflePlaylistItems();
        } else {
            resetOriginalOrderOfPlaylistItems();
        }
    }

    private void resetOriginalOrderOfPlaylistItems() {
        mMediaItems = mMediaItemsOriginalOrder;
    }

    private void shufflePlaylistItems() {
        mMediaItemsOriginalOrder = new ArrayList<MediaItem>(mMediaItems);
        Random rnd = new Random(System.nanoTime());
        Collections.shuffle(mMediaItems, rnd);
    }
}
