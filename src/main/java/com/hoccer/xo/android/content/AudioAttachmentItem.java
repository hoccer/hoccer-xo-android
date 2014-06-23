package com.hoccer.xo.android.content;

import android.net.Uri;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import org.apache.log4j.Logger;

public class AudioAttachmentItem {

    static final Logger LOG = Logger.getLogger(AudioAttachmentItem.class);

    private IContentObject contentObject;

    private MediaMetaData mMetaData;

    private String mFilePath;

    public static AudioAttachmentItem create(String mediaFilePath, IContentObject contentObject) {
        if (mediaFilePath == null || mediaFilePath.isEmpty()) {
            return null;
        }
        AudioAttachmentItem mi = new AudioAttachmentItem();
        mi.setFilePath(mediaFilePath);

        String path = Uri.parse(mediaFilePath).getPath();
        try {
            mi.setMetaData(MediaMetaData.create(path));
        } catch (Exception e) {
            LOG.warn("Cannot load meta-data for file.");
            return null;
        }

        mi.setContentObject(contentObject);

        return mi;
    }

    public IContentObject getContentObject() {
        return contentObject;
    }

    public void setContentObject(IContentObject contentObject) {
        this.contentObject = contentObject;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    public String getFileName() {
        return mFilePath.substring(mFilePath.lastIndexOf("/") + 1);
    }

    public MediaMetaData getMetaData() {
        return mMetaData;
    }

    public void setMetaData(MediaMetaData metaData) {
        this.mMetaData = metaData;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null && obj instanceof AudioAttachmentItem) {

            IContentObject contentObject = ((AudioAttachmentItem) obj).getContentObject();

            if (contentObject instanceof TalkClientDownload && this.getContentObject() instanceof TalkClientDownload) {
                isEqual = ((TalkClientDownload) this.getContentObject()).getClientDownloadId() ==
                        ((TalkClientDownload) contentObject).getClientDownloadId();
            } else if (contentObject instanceof TalkClientUpload && this.getContentObject() instanceof TalkClientUpload) {
                isEqual = ((TalkClientUpload) this.getContentObject()).getClientUploadId() ==
                        ((TalkClientUpload) contentObject).getClientUploadId();
            }
        }

        return isEqual;
    }
}
