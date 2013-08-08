package com.hoccer.talk.android.content;

import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.android.service.TalkClientService;
import com.hoccer.talk.client.model.TalkClientDownload;
import org.apache.log4j.Logger;

import java.io.File;

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

    private static final Logger LOG = Logger.getLogger(ContentObject.class);

    public static ContentObject forDownload(TalkClientDownload download) {
        ContentObject co = new ContentObject();
        TalkClientDownload.State state = download.getState();
        TalkClientDownload.Type type = download.getType();
        co.setMimeType(download.getContentType());
        co.setMediaType(download.getMediaType());
        switch (type) {
        case AVATAR:
            File avatarLocation = TalkApplication.getAvatarLocation(download);
            if(avatarLocation != null) {
                LOG.info("co from avatar " + avatarLocation.toString());
                co.setContentUrl(avatarLocation.toString());
            }
            break;
        case ATTACHMENT:
            File attachmentLocation = TalkApplication.getAttachmentLocation(download);
            if(attachmentLocation != null) {
                LOG.info("co from avatar " + attachmentLocation.toString());
                co.setContentUrl(attachmentLocation.toString());
            }
            break;
        }
        LOG.info("content " + co.getContentUrl() + " in state " + state);
        switch (state) {
        case NEW:
            co.setState(State.DOWNLOAD_NEW);
            break;
        case COMPLETE:
            co.setState(State.DOWNLOAD_COMPLETE);
            break;
        case STARTED:
        case DECRYPTING:
            co.setState(State.DOWNLOAD_STARTED);
            break;
        case FAILED:
        default:
            co.setState(State.DOWNLOAD_FAILED);
            break;
        }
        int contentLength = download.getContentLength();
        if(contentLength != -1) {
            co.setTransferLength(contentLength);
            co.setTransferProgress((int)download.getDownloadProgress());
        }
        co.setAspectRatio(download.getAspectRatio());
        return co;
    }

    public enum State {
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

    double mAspectRatio;

    int mTransferProgress;

    int mTransferLength;

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

    public double getAspectRatio() {
        return mAspectRatio;
    }

    public void setAspectRatio(double mAspectRatio) {
        this.mAspectRatio = mAspectRatio;
    }

    public int getTransferProgress() {
        return mTransferProgress;
    }

    public void setTransferProgress(int mTransferProgress) {
        this.mTransferProgress = mTransferProgress;
    }

    public int getTransferLength() {
        return mTransferLength;
    }

    public void setTransferLength(int mTransferLength) {
        this.mTransferLength = mTransferLength;
    }
}
