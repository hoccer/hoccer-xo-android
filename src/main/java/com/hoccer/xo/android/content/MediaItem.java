package com.hoccer.xo.android.content;

import android.net.Uri;

public class MediaItem {

    private MediaMetaData metaData;
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public MediaMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MediaMetaData metaData) {
        this.metaData = metaData;
    }

    public static MediaItem create(String mediaFilePath) {
        MediaItem mi = new MediaItem();
        mi.setFilePath(mediaFilePath);

        String path = Uri.parse(mediaFilePath).getPath();
        mi.setMetaData(MediaMetaData.create(path));
        return mi;
    }
}
