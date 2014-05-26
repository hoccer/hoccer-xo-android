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
    private List<Integer> mPlaylistIndexes;

    private RepeatMode mRepeatMode = RepeatMode.NO_REPEAT;
    private int mCurrentIndex = 0;
    private boolean shuffleActive = false;


    public void setCurrentTrackNumber(int trackNumber) {
        mCurrentIndex = mPlaylistIndexes.indexOf(trackNumber);
        resetPlaylistIndexes();
    }

    public int getCurrentTrackNumber() {
        return mPlaylistIndexes.get(mCurrentIndex);
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
        if (!mPlaylistIndexes.isEmpty()) {
            if (nextIndex() < mPlaylistIndexes.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MediaItem previous() {
        --mCurrentIndex;
        if (mCurrentIndex < 0) {
            mCurrentIndex = mPlaylistIndexes.size() - 1;
        }
        return mMediaItems.get(mPlaylistIndexes.get(mCurrentIndex));
    }

    @Override
    public MediaItem next() {
        ++mCurrentIndex;
        if (mCurrentIndex >= mPlaylistIndexes.size()) {
            mCurrentIndex = 0;
        }
        return mMediaItems.get(mPlaylistIndexes.get(mCurrentIndex));
    }

    public MediaItem nextByRepeatMode() {
        switch (mRepeatMode) {
            case NO_REPEAT:
                if (hasNext()) {
                    return mMediaItems.get(mPlaylistIndexes.get(++mCurrentIndex));
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
        LOG.error("Setting items at current position is not supported.");
    }

    @Override
    public void add(MediaItem item) {
        LOG.error("Adding items at current position is not supported.");
    }

    public void add(int index, MediaItem item) {
        mMediaItems.add(index, item);
        resetPlaylistIndexes();
    }

    public void addAll(List<MediaItem> items) {
        mMediaItems.addAll(items);
        resetPlaylistIndexes();
    }

    public MediaItem current() {
        if ((mCurrentIndex >= 0) && (mCurrentIndex < mMediaItems.size())) {
            return mMediaItems.get(mPlaylistIndexes.get(mCurrentIndex));
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
        resetPlaylistIndexes();
    }

    private void resetPlaylistIndexes() {
        mPlaylistIndexes = new ArrayList<Integer>();
        for (int i = 0; i < mMediaItems.size(); i++) {
            mPlaylistIndexes.add(i);
        }

        int currentTrackNumber = getCurrentTrackNumber();
        if (shuffleActive) {
            Random rnd = new Random(System.nanoTime());
            Collections.shuffle(mPlaylistIndexes, rnd);

            // move current index to first shuffle position
            int shuffledIndex = mPlaylistIndexes.indexOf(currentTrackNumber);
            int firstShuffledTrack = mPlaylistIndexes.get(0);
            mPlaylistIndexes.set(0, currentTrackNumber);
            mPlaylistIndexes.set(shuffledIndex, firstShuffledTrack);
            mCurrentIndex = 0;
        } else {
            mCurrentIndex = currentTrackNumber;
        }

    }
}
