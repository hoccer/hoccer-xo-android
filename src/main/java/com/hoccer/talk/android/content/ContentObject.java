package com.hoccer.talk.android.content;

import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
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

    public static TalkClientUpload createAvatarUpload(ContentObject object) {
        TalkClientUpload upload = new TalkClientUpload();
        upload.initializeAsAvatar(object.getContentUrl(), object.getMimeType());
        return upload;
    }

    public static TalkClientUpload createAttachmentUpload(ContentObject object) {
        TalkClientUpload upload = new TalkClientUpload();
        upload.initializeAsAttachment(object.getContentUrl(), object.getMimeType(), object.getMediaType(), object.getAspectRatio());
        return upload;
    }

    public static ContentObject forUpload(TalkClientUpload upload) {
        LOG.info("content object for upload " + upload.getClientUploadId());

        ContentObject co = new ContentObject();
        TalkClientUpload.State state = upload.getState();
        TalkClientUpload.Type type = upload.getType();
        co.setMimeType(upload.getContentType());
        co.setMediaType(upload.getMediaType());
        switch (type) {
            case AVATAR:
                File avatarLocation = TalkApplication.getAvatarLocation(upload);
                if(avatarLocation != null) {
                    LOG.info("co from avatar " + avatarLocation.toString());
                    co.setContentUrl(avatarLocation.toString());
                }
                break;
            case ATTACHMENT:
                File attachmentLocation = TalkApplication.getAttachmentLocation(upload);
                if(attachmentLocation != null) {
                    LOG.info("co from attachment " + attachmentLocation.toString());
                    co.setContentUrl(attachmentLocation.toString());
                }
                break;
        }
        LOG.info("content " + co.getContentUrl() + " in state " + state);
        co.setAvailable(true);
        switch (state) {
            case NEW:
                co.setState(State.UPLOAD_NEW);
                break;
            case COMPLETE:
                co.setState(State.UPLOAD_COMPLETE);
                break;
            case REGISTERED:
            case ENCRYPTED:
            case STARTED:
                co.setState(State.UPLOAD_STARTED);
                break;
            case FAILED:
            default:
                co.setState(State.UPLOAD_FAILED);
                break;
        }
        int contentLength = upload.getEncryptedLength();
        if(contentLength != -1) {
            co.setTransferLength(contentLength);
            co.setTransferProgress((int)upload.getProgress());
        }
        co.setAspectRatio(upload.getAspectRatio());
        return co;
    }

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
            co.setAvailable(true);
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
        UPLOAD_NEW,
        UPLOAD_COMPLETE,
        UPLOAD_FAILED,
        UPLOAD_STARTED,
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

    boolean mAvailable;

    public ContentObject() {
        mAspectRatio = 1.0;
    }

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

    public boolean isAvailable() {
        return mAvailable;
    }

    public void setAvailable(boolean mAvailable) {
        this.mAvailable = mAvailable;
    }
}
