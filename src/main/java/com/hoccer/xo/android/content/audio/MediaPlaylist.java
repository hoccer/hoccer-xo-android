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

    private List<Integer> mPlaylistOrder = new ArrayList<Integer>();

    private RepeatMode mRepeatMode = RepeatMode.NO_REPEAT;

    private int mCurrentIndex = -1;
    private boolean shuffleActive = false;

    public void setCurrentIndex(int mCurrentIndex) {
        this.mCurrentIndex = mCurrentIndex;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentTrackNumber(int trackNumber) {
        mCurrentIndex = mPlaylistOrder.indexOf(trackNumber);
        resetPlaylistIndexes();
    }

    public int getCurrentTrackNumber() {
        if (mPlaylistOrder != null && mPlaylistOrder.size() > 0) {
            return mPlaylistOrder.get(mCurrentIndex);
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
        if (!mPlaylistOrder.isEmpty()) {
            if (nextIndex() < mPlaylistOrder.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AudioAttachmentItem previous() {
        --mCurrentIndex;
        if (mCurrentIndex < 0) {
            mCurrentIndex = mPlaylistOrder.size() - 1;
        }
        return mAudioAttachmentItems.get(mPlaylistOrder.get(mCurrentIndex));
    }

    @Override
    public AudioAttachmentItem next() {
        ++mCurrentIndex;
        if (mCurrentIndex >= mPlaylistOrder.size()) {
            mCurrentIndex = 0;
        }
        return mAudioAttachmentItems.get(mPlaylistOrder.get(mCurrentIndex));
    }

    public AudioAttachmentItem nextByRepeatMode() {
        switch (mRepeatMode) {
            case NO_REPEAT:
                if (hasNext()) {
                    return mAudioAttachmentItems.get(mPlaylistOrder.get(++mCurrentIndex));
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
        mPlaylistOrder.clear();
        mCurrentIndex = -1;
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

        if (attachmentIndex < 0) {
            throw new IllegalArgumentException("Removing entry with index < 0 is not possible.");
        }

        LOG.error("#foo attachmentIndex: " + attachmentIndex);
        LOG.error("#foo getCurrentIndex: " + getCurrentIndex());
        LOG.error("#foo getCurrentPlaylistPosition: " + getCurrentPlaylistPosition());
        LOG.error("#foo getIndexOfPlaylistPosition: " + getIndexOfPlaylistPosition(attachmentIndex));

        if (attachmentIndex == getCurrentPlaylistPosition()) {
            throw new IllegalStateException("Removing the current entry (" + getCurrentPlaylistPosition() + ") from playlist not possible.");
        }

        if (attachmentIndex < mAudioAttachmentItems.size()) {
            int indexOfPlaylistPosition = getIndexOfPlaylistPosition(attachmentIndex);
            mAudioAttachmentItems.remove(attachmentIndex);
            mPlaylistOrder.remove(indexOfPlaylistPosition);
            correctPlaylistIndexes(indexOfPlaylistPosition);
        }
    }

    private int getIndexOfPlaylistPosition(int attachmentIndex) {
        return mPlaylistOrder.indexOf(attachmentIndex);
    }

    private int getCurrentPlaylistPosition() {
        return mPlaylistOrder.get(mCurrentIndex);
    }

    private void correctPlaylistIndexes(int indexOfPlaylistPosition) {
        if (mCurrentIndex > indexOfPlaylistPosition) {
            mCurrentIndex--;
        }
        if (shuffleActive) {
            int missing = -1;
            int max = Collections.max(mPlaylistOrder);
            for (int i = 0; i <= max; i++) {
                if (missing != -1) {
                    mPlaylistOrder.set(mPlaylistOrder.indexOf(i), missing);
                }
                if (mPlaylistOrder.indexOf(i) == -1){
                    missing = i;
                }
            }
        } else {
            createPlaylistIndexes();
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
            return mAudioAttachmentItems.get(mPlaylistOrder.get(mCurrentIndex));
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

    public List<Integer> getPlaylistOrder() {
        return mPlaylistOrder;
    }

    public void setPlaylistOrder(ArrayList<Integer> playlistOrder) {
        this.mPlaylistOrder = playlistOrder;
    }

    private void resetPlaylistIndexes() {
        int currentTrackNumber = getCurrentTrackNumber();
        createPlaylistIndexes();

        if (shuffleActive) {
            shufflePlaylistIndexes(currentTrackNumber);
        } else {
            mCurrentIndex = currentTrackNumber;
        }
    }

    private void shufflePlaylistIndexes(int currentTrackNumber) {
        Random rnd = new Random(System.nanoTime());
        Collections.shuffle(mPlaylistOrder, rnd);

        // move current index to first shuffle position
        int shuffledIndex = mPlaylistOrder.indexOf(currentTrackNumber);
        int firstShuffledTrack = mPlaylistOrder.get(0);
        mPlaylistOrder.set(0, currentTrackNumber);
        mPlaylistOrder.set(shuffledIndex, firstShuffledTrack);
        mCurrentIndex = 0;
    }

    private void createPlaylistIndexes() {
        mPlaylistOrder = new ArrayList<Integer>();
        for (int i = 0; i < mAudioAttachmentItems.size(); i++) {
            mPlaylistOrder.add(i);
        }
    }
}
