package com.hoccer.xo.android.content.audio;

import com.hoccer.xo.android.content.AudioAttachmentItem;
import org.apache.log4j.Logger;

import java.util.*;

public class MediaPlaylist implements ListIterator<AudioAttachmentItem> {

    public static enum RepeatMode {
        REPEAT_TITLE, REPEAT_ALL, NO_REPEAT;
    }

    private static final Logger LOG = Logger.getLogger(MediaPlaylist.class);

    private List<AudioAttachmentItem> mAudioAttachmentItems = new ArrayList<AudioAttachmentItem>();
    private List<Integer> mPlaylistIndexes = new ArrayList<Integer>();

    private RepeatMode mRepeatMode = RepeatMode.NO_REPEAT;
    private int mCurrentIndex = 0;
    private boolean shuffleActive = false;

    public void setCurrentTrackNumber(int trackNumber) {
        mCurrentIndex = mPlaylistIndexes.indexOf(trackNumber);
        resetPlaylistIndexes();
    }

    public int getCurrentTrackNumber() {
        if (mPlaylistIndexes != null && mPlaylistIndexes.size() > 0) {
            return mPlaylistIndexes.get(mCurrentIndex);
        } else {
            return 0;
        }
    }

    public int size() {
        return mAudioAttachmentItems.size();
    }

    @Override
    public boolean hasPrevious() {
        if (!mAudioAttachmentItems.isEmpty()) {
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
    public AudioAttachmentItem previous() {
        --mCurrentIndex;
        if (mCurrentIndex < 0) {
            mCurrentIndex = mPlaylistIndexes.size() - 1;
        }
        return mAudioAttachmentItems.get(mPlaylistIndexes.get(mCurrentIndex));
    }

    @Override
    public AudioAttachmentItem next() {
        ++mCurrentIndex;
        if (mCurrentIndex >= mPlaylistIndexes.size()) {
            mCurrentIndex = 0;
        }
        return mAudioAttachmentItems.get(mPlaylistIndexes.get(mCurrentIndex));
    }

    public AudioAttachmentItem nextByRepeatMode() {
        switch (mRepeatMode) {
            case NO_REPEAT:
                if (hasNext()) {
                    return mAudioAttachmentItems.get(mPlaylistIndexes.get(++mCurrentIndex));
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
        mAudioAttachmentItems.clear();
        mPlaylistIndexes.clear();
        mCurrentIndex = 0;
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
    public void set(AudioAttachmentItem item) {
        LOG.error("Setting items at current position is not supported.");
    }

    @Override
    public void add(AudioAttachmentItem item) {
        LOG.error("Adding items at current position is not supported.");
    }

    public void remove(int attachmentIndex) {
        if (attachmentIndex < mAudioAttachmentItems.size()) {
            mAudioAttachmentItems.remove(attachmentIndex);

            int playlistIndex = mPlaylistIndexes.indexOf(attachmentIndex);
            mPlaylistIndexes.remove(playlistIndex);

            if (mCurrentIndex > playlistIndex) {
                mCurrentIndex--;
            }
        }
    }

    public void setTrack(AudioAttachmentItem item) {
        clear();
        mAudioAttachmentItems.add(item);
        resetPlaylistIndexes();
    }

    public void setTrackList(List<AudioAttachmentItem> items) {
        clear();
        mAudioAttachmentItems.addAll(items);
        resetPlaylistIndexes();
    }

    public AudioAttachmentItem current() {
        if ((mCurrentIndex >= 0) && (mCurrentIndex < mAudioAttachmentItems.size())) {
            return mAudioAttachmentItems.get(mPlaylistIndexes.get(mCurrentIndex));
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
        int currentTrackNumber = getCurrentTrackNumber();
        mPlaylistIndexes = new ArrayList<Integer>();
        for (int i = 0; i < mAudioAttachmentItems.size(); i++) {
            mPlaylistIndexes.add(i);
        }

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
