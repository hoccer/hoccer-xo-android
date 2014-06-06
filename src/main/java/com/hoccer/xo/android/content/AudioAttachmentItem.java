package com.hoccer.xo.android.content;

import android.net.Uri;
import com.hoccer.talk.content.IContentObject;

public class AudioAttachmentItem {

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
        mi.setMetaData(MediaMetaData.create(path));

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
        if (obj != null && obj instanceof AudioAttachmentItem) {
            return this.getFilePath().equals(((AudioAttachmentItem) obj).getFilePath());
        }

        return false;
    }
}
