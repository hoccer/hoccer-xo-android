package com.hoccer.xo.android.content;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import org.apache.log4j.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaMetaData {

    private static final Logger LOG = Logger.getLogger(MediaMetaData.class);

    private String mTitle = null;
    private String mArtist = null;
    private String mAlbumTitle = null;
    private String mMimeType = null;
    private boolean mHasAudio = false;
    private boolean mHasVideo = false;

    private MediaMetaData() {
    }

    public String getTitle(String filePath) {
        File file = new File(filePath);
        return (mTitle != null) ? mTitle : file.getName();
    }

    public String getArtist() {
        return (mArtist != null) ? mArtist : "Unknown Artist";
    }

    public String getAlbumTitle() {
        return mAlbumTitle;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public boolean hasAudio() {
        return mHasAudio;
    }

    public boolean hasVideo() {
        return mHasVideo;
    }

    private void setTitle(String pTitle) {
        this.mTitle = pTitle;
    }

    private void setArtist(String pArtist) {
        this.mArtist = pArtist;
    }

    private void setAlbumTitle(String pAlbumTitle) {
        this.mArtist = pAlbumTitle;
    }

    private void setMimeType(String pMimeType) {
        mMimeType = pMimeType;
    }

    private void setHasAudio(boolean pHasAudio) {
        mHasAudio = pHasAudio;
    }

    private void setHasVideo(boolean pHasVideo) {
        mHasVideo = pHasVideo;
    }

    public static MediaMetaData create(String pMediaFilePath) throws IllegalArgumentException {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        MediaMetaData metaData = new MediaMetaData();

        retriever.setDataSource(pMediaFilePath);
        metaData.setTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        metaData.setArtist(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        metaData.setAlbumTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        metaData.setMimeType(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));

        if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null) {
            metaData.setHasAudio(true);
        }

        if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != null) {
            metaData.setHasVideo(true);
        }

        return metaData;
    }

    public static List<MediaMetaData> create(List<String> pMediaFilePathList) throws IllegalArgumentException {
        ArrayList<MediaMetaData> metaDataList = new ArrayList<MediaMetaData>();

        for (String mediaFilePath : pMediaFilePathList) {
            metaDataList.add(create(mediaFilePath));
        }

        return metaDataList;
    }

    public static byte[] getArtwork(String filePath) {
        String path = Uri.parse(filePath).getPath();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }

        return retriever.getEmbeddedPicture();
    }
}
