package com.hoccer.xo.android.content.audio;

import android.media.MediaMetadataRetriever;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nico on 13/05/2014.
 */
public class MediaMetaData {

    private static final Logger LOG = Logger.getLogger(MediaMetaData.class);

    private String mTitle = null;
    private String mArtist = null;
    private String mAlbumTitle = null;
    private String mMimeType = null;
    private boolean mHasAudio = false;
    private boolean mHasVideo = false;
    private byte[] mArtwork = null;

    public String getTitle() {
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

    public byte[] getArtwork() {
        return mArtwork;
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

    private void setArtwork(byte[] pArtwork) {
        mArtwork = pArtwork;
    }

    public static MediaMetaData factorMetaDataForFile(String pMediaFilePath) throws IllegalArgumentException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        return retrieveMetaDataFromFile(retriever, pMediaFilePath);
    }

    public static List<MediaMetaData> factorMetaDataForFileList(List<String> pMediaFilePathList) throws IllegalArgumentException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        ArrayList<MediaMetaData> metaDatas = new ArrayList<MediaMetaData>();

        for (String mediaFilePath : pMediaFilePathList) {
            metaDatas.add(retrieveMetaDataFromFile(retriever,mediaFilePath));
        }

        return metaDatas;
    }


    private static MediaMetaData retrieveMetaDataFromFile(MediaMetadataRetriever pRetriever, String pMediaFilePath) {
        MediaMetaData metaData = new MediaMetaData();

        pRetriever.setDataSource(pMediaFilePath);
        metaData.setTitle(pRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        metaData.setArtist(pRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        metaData.setAlbumTitle(pRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        metaData.setMimeType(pRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));

        if (pRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null) {
            metaData.setHasAudio(true);
        }

        if (pRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != null) {
            metaData.setHasVideo(true);
        }

        metaData.setArtwork(pRetriever.getEmbeddedPicture());

        return metaData;
    }


}
