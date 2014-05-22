package com.hoccer.xo.android.content.audio;

import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.MediaItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MediaPlaylist implements ListIterator<MediaItem>, IXoTransferListener {


    public static enum RepeatMode {
        REPEAT_TRACK, REPEAT_ALL, NO_REPEAT;
    }

    public static final int UNDEFINED_CONTACT_ID = -1;

    private List<MediaItem> mPlaylistItems = new ArrayList<MediaItem>();

    private RepeatMode mRepeatMode = RepeatMode.NO_REPEAT;
    private int mConversationContactId = UNDEFINED_CONTACT_ID;
    private int mCurrentIndex = 0;
    private boolean mIsUpdatable = true;

    private MediaPlaylist() {
    }

    public static MediaPlaylist create(String mediaFilePath) {

        MediaPlaylist pl = new MediaPlaylist();
        pl.mPlaylistItems.add(MediaItem.create(mediaFilePath));
        return pl;
    }

    public static MediaPlaylist create(List<TalkClientDownload> mAudioAttachmentList) {

        MediaPlaylist pl = new MediaPlaylist();
        for (TalkClientDownload tcd : mAudioAttachmentList) {
            pl.mPlaylistItems.add(MediaItem.create(tcd.getContentDataUrl()));
        }
        return pl;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public boolean isUpdatable() {
        return mIsUpdatable;
    }

    public int getConversationContactId() {
        return mConversationContactId;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public void setUpdatable(boolean isUpdatable) {
        mIsUpdatable = isUpdatable;
    }

    public void setConversationContactId(int pConversationContactId) {
        mConversationContactId = pConversationContactId;
    }

    public int size() {
        return mPlaylistItems.size();
    }

    public RepeatMode getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.mRepeatMode = repeatMode;
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
        if (mCurrentIndex == 0) {
            mCurrentIndex = mPlaylistItems.size() - 1;
        } else {
            --mCurrentIndex;
        }
        return mPlaylistItems.get(mCurrentIndex);
    }

    @Override
    public MediaItem next() {
        if (mCurrentIndex == mPlaylistItems.size() - 1) {
            mCurrentIndex = 0;
        } else {
            ++mCurrentIndex;
        }
        return mPlaylistItems.get(mCurrentIndex);
    }

    public MediaItem nextByRepeatMode() {
        switch (mRepeatMode) {
            case NO_REPEAT:
                if (hasNext()) {
                    return mPlaylistItems.get(++mCurrentIndex);
                }
            case REPEAT_ALL:
                return next();
            case REPEAT_TRACK:
                return current();
            default:
                return null;
        }
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
        return mPlaylistItems.get(mCurrentIndex);
    }

    @Override
    public void onDownloadRegistered(TalkClientDownload download) {

    }

    @Override
    public void onDownloadStarted(TalkClientDownload download) {

    }

    @Override
    public void onDownloadProgress(TalkClientDownload download) {

    }

    @Override
    public void onDownloadFinished(TalkClientDownload download) {
        int contactId = UNDEFINED_CONTACT_ID;

        try {
            TalkClientMessage message = XoApplication.getXoClient().getDatabase().findMessageByDownloadId(download.getClientDownloadId());
            contactId = message.getConversationContact().getClientContactId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (download.getContentMediaType().equals(ContentMediaType.AUDIO)) {
            if (mConversationContactId == contactId) {
                MediaItem newItem = MediaItem.create(download.getContentDataUrl());
                mPlaylistItems.add(newItem);

            }
        }
    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {

    }

    @Override
    public void onUploadStarted(TalkClientUpload upload) {

    }

    @Override
    public void onUploadProgress(TalkClientUpload upload) {

    }

    @Override
    public void onUploadFinished(TalkClientUpload upload) {

    }

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {

    }

}
