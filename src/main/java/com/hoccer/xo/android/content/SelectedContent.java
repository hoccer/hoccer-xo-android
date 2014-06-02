package com.hoccer.xo.android.content;

import android.content.Intent;
import android.net.Uri;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentDisposition;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.crypto.CryptoUtils;
import com.hoccer.xo.android.XoApplication;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

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

    String mFileName;

    String mContentUrl;
    
    String mContentDataUrl;

    String mContentType = null;

    String mContentMediaType = null;

    String mContentHmac = null;

    int    mContentLength = -1;

    double mContentAspectRatio = 1.0;

    /**
     * Literal data.
     *
     * Converted to a file when selected content becomes an upload.
     */
    byte[] mData = null;

    public SelectedContent(Intent resultIntent, String contentDataUrl) {
        Uri contentUrl = resultIntent.getData();
        LOG.debug("new selected content: " + contentUrl);
        mContentUrl = contentUrl.toString();
        mContentType = resultIntent.getType();
        mContentDataUrl = contentDataUrl;
    }

    public SelectedContent(String contentUrl, String contentDataUrl) {
        LOG.debug("new selected content: " + contentUrl);
        mContentUrl = contentUrl;
        mContentDataUrl = contentDataUrl;
    }

    public SelectedContent(byte[] data) {
        LOG.debug("new selected content with raw data");
        mData = data;
        mContentLength = data.length;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
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
    public String getFileName() {
        return mFileName;
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
    public String getContentHmac() {
        if (mContentHmac == null) {
            byte[] hmac = new byte[0];
            try {
                hmac = CryptoUtils.computeHmac(mContentDataUrl);
                mContentHmac = new String(Base64.encodeBase64(hmac));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LOG.debug("mContentHmac="+mContentHmac);
        return mContentHmac;
    }

    @Override
    public int getTransferLength() {
        return 0;
    }

    @Override
    public int getTransferProgress() {
        return 0;
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        mData = data;
    }

    private void writeDataToFile() {
        if(mData != null) {
            File dir = XoApplication.getGeneratedDirectory();
            File file = new File(dir, UUID.randomUUID().toString());
            try {
                file.createNewFile();
                OutputStream os = null;
                MessageDigest digest = null;
                if (mContentHmac == null) {
                    digest = MessageDigest.getInstance("SHA256");
                    os = new DigestOutputStream(new FileOutputStream(file), digest);
                }  else {
                    os = new FileOutputStream(file);
                }
                os.write(mData);
                os.flush();
                os.close();
                mContentDataUrl = "file://" + file.toString();
                mData = null;
                if (digest != null) {
                    mContentHmac = new String(Base64.encodeBase64(digest.digest()));
                }
            } catch (IOException e) {
                LOG.error("error writing content to file", e);
            } catch (NoSuchAlgorithmException e) {
                LOG.error("error writing content to file", e);
            }
        }
    }

    public static TalkClientUpload createAvatarUpload(IContentObject object) {
        if(object instanceof SelectedContent) {
            ((SelectedContent)object).writeDataToFile();
        }
        TalkClientUpload upload = new TalkClientUpload();
        upload.initializeAsAvatar(
                object.getContentUrl(),
                object.getContentDataUrl(),
                object.getContentType(),
                object.getContentLength());
        return upload;
    }

    public static TalkClientUpload createAttachmentUpload(IContentObject object) {
        if(object instanceof SelectedContent) {
            ((SelectedContent)object).writeDataToFile();
        }
        TalkClientUpload upload = new TalkClientUpload();
        upload.initializeAsAttachment(
                object.getFileName(),
                object.getContentUrl(),
                object.getContentDataUrl(),
                object.getContentType(),
                object.getContentMediaType(),
                object.getContentAspectRatio(),
                object.getContentLength(),
                object.getContentHmac());
        return upload;
    }

}
