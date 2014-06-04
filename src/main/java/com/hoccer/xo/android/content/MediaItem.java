package com.hoccer.xo.android.content;

import android.net.Uri;

public class MediaItem {

    private MediaMetaData mMetaData;
    private String mFilePath;


    public static MediaItem create(String mediaFilePath) {
        if (mediaFilePath == null || mediaFilePath.isEmpty()) {
            return null;
        }
        MediaItem mi = new MediaItem();
        mi.setFilePath(mediaFilePath);

        String path = Uri.parse(mediaFilePath).getPath();
        mi.setMetaData(MediaMetaData.create(path));
        return mi;
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
        if (obj != null && obj instanceof MediaItem) {
            return this.getFilePath().equals(((MediaItem) obj).getFilePath());
        }

        return false;
    }
}
