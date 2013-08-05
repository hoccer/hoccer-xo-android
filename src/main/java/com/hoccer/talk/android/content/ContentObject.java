package com.hoccer.talk.android.content;

import com.hoccer.talk.android.service.TalkClientService;
import com.hoccer.talk.client.model.TalkClientDownload;

/**
 * Represents the content behind an attachment
 *
 * This is a grabbag object for all the properties
 * of content objects that we need in the UI.
 *
 * They do not cary an identity and can be created
 * from uploads, downloads as well as by selecting
 * content from external sources.
 *
 */
public class ContentObject {

    public static ContentObject forDownload(TalkClientDownload download) {
        ContentObject co = new ContentObject();
        TalkClientDownload.State state = download.getState();
        TalkClientDownload.Type type = download.getType();
        co.setMimeType(download.getContentType());
        switch (type) {
        case AVATAR:
            co.setContentUrl(TalkClientService.HACK_AVATAR_DIRECTORY + download.getFile());
            break;
        case ATTACHMENT:
            co.setContentUrl(TalkClientService.HACK_ATTACHMENT_DIRECTORY + download.getFile());
            break;
        }
        switch (state) {
        case NEW:
            co.setState(State.DOWNLOAD_NEW);
            break;
        case COMPLETE:
            co.setState(State.DOWNLOAD_COMPLETE);
            break;
        case STARTED:
            co.setState(State.DOWNLOAD_STARTED);
            break;
        case FAILED:
            co.setState(State.DOWNLOAD_FAILED);
            break;
        }
        return co;
    }

    enum State {
        UPLOAD_SELECTED,
        DOWNLOAD_NEW,
        DOWNLOAD_STARTED,
        DOWNLOAD_FAILED,
        DOWNLOAD_COMPLETE,
    }

    State mState;

    String mContentUrl;

    String mMimeType;

    String mMediaType;

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        this.mState = state;
    }

    public String getContentUrl() {
        return mContentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.mContentUrl = contentUrl;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mimeType) {
        this.mMimeType = mimeType;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public void setMediaType(String mediaType) {
        this.mMediaType = mediaType;
    }

}
