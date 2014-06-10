package com.hoccer.xo.android.content;

import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
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
    private Drawable mArtwork = null;

    private MediaMetaData() {
    }

    public String getTitle() {
        return mTitle;
    }

    public String getTitleOrFilename(String pFilePath) {
        if (mTitle == null || mTitle.isEmpty()) {
            File file = new File(pFilePath);
            return file.getName();
        }
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
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

    public Drawable getArtwork() {
        return mArtwork;
    }

    private void setTitle(String pTitle) {
        this.mTitle = pTitle;
    }

    private void setArtist(String pArtist) {
        this.mArtist = pArtist;
    }

    private void setAlbumTitle(String pAlbumTitle) {
        this.mAlbumTitle = pAlbumTitle;
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

    public void setArtwork(Drawable artwork) {
        mArtwork = artwork;
    }

    public static MediaMetaData create(String pMediaFilePath) throws IllegalArgumentException {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        MediaMetaData metaData = new MediaMetaData();

        retriever.setDataSource(pMediaFilePath);

        String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        if(album == null) {
            album = retriever.extractMetadata(25); // workaround bug on Galaxy S3 and S4
        }
        metaData.setAlbumTitle(album);

        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if(artist == null) {
            artist = retriever.extractMetadata(26); // workaround bug on Galaxy S3 and S4
        }
        metaData.setArtist(artist);

        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if(title == null) {
            title = retriever.extractMetadata(31); // workaround bug on Galaxy S3 and S4
        }
        metaData.setTitle(title);

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
