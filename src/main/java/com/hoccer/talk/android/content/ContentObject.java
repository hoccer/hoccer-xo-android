package com.hoccer.talk.android.content;

public class ContentObject {

    enum State {
        UPLOAD_SELECTED,
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
