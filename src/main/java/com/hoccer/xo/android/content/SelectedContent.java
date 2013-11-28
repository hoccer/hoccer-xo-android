package com.hoccer.xo.android.content;

import android.content.Intent;
import android.net.Uri;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentDisposition;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import org.apache.log4j.Logger;

/**
 * Content objects
 *
 * This is a grabbag object for all the properties
 * of content objects that we need in the UI.
 *
 * They do not cary an identity and can be created
 * from uploads, downloads as well as by selecting
 * content from external sources.
 *
 */
public class SelectedContent implements IContentObject {

    private static final Logger LOG = Logger.getLogger(SelectedContent.class);

    String mContentUrl;
    
    String mContentDataUrl;

    String mContentType = null;

    String mContentMediaType = null;

    int    mContentLength = -1;

    double mContentAspectRatio = 1.0;

    public SelectedContent(Intent resultIntent, String contentDataUrl) {
        Uri contentUrl = resultIntent.getData();
        LOG.info("new selected content: " + contentUrl);
        mContentUrl = contentUrl.toString();
        mContentType = resultIntent.getType();
        mContentDataUrl = contentDataUrl;
    }

    public SelectedContent(String contentUrl, String contentDataUrl) {
        LOG.info("new selected content: " + contentUrl);
        mContentUrl = contentUrl;
        mContentDataUrl = contentDataUrl;
    }

    public void setContentType(String mContentType) {
        this.mContentType = mContentType;
    }

    public void setContentMediaType(String mContentMediaType) {
        this.mContentMediaType = mContentMediaType;
    }

    public void setContentLength(int mContentLength) {
        this.mContentLength = mContentLength;
    }

    public void setContentAspectRatio(double mContentAspectRatio) {
        this.mContentAspectRatio = mContentAspectRatio;
    }

    @Override
    public boolean isContentAvailable() {
        return true;
    }

    @Override
    public ContentState getContentState() {
        return ContentState.SELECTED;
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return ContentDisposition.SELECTED;
    }

    @Override
    public String getContentType() {
        return mContentType;
    }

    @Override
    public String getContentUrl() {
        return mContentUrl;
    }

    @Override
    public String getContentDataUrl() {
        return mContentDataUrl;
    }

    @Override
    public int getContentLength() {
        return mContentLength;
    }

    @Override
    public String getContentMediaType() {
        return mContentMediaType;
    }

    @Override
    public double getContentAspectRatio() {
        return mContentAspectRatio;
    }

    @Override
    public int getTransferLength() {
        return 0;
    }

    @Override
    public int getTransferProgress() {
        return 0;
    }

    public static TalkClientUpload createAvatarUpload(IContentObject object) {
        TalkClientUpload upload = new TalkClientUpload();
        upload.initializeAsAvatar(
                object.getContentDataUrl(),
                object.getContentType(),
                object.getContentLength());
        return upload;
    }

    public static TalkClientUpload createAttachmentUpload(IContentObject object) {
        TalkClientUpload upload = new TalkClientUpload();
        upload.initializeAsAttachment(
                object.getContentDataUrl(),
                object.getContentType(),
                object.getContentMediaType(),
                object.getContentAspectRatio(),
                object.getContentLength());
        return upload;
    }

}
