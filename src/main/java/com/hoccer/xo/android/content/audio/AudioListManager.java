package com.hoccer.xo.android.content.audio;

import android.content.*;
import android.database.DataSetObserver;
import android.database.Observable;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.database.AndroidTalkDatabase;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AudioListManager extends Observable<DataSetObserver> implements ListIterator<TalkClientDownload>, IXoTransferListener {

    private static AudioListManager INSTANCE = null;

    private static final Logger LOG = Logger.getLogger(AudioListManager.class);

    private final Context mContext;
    private final XoClientDatabase mDatabase;

    private List<TalkClientDownload> mAudioList = new ArrayList<TalkClientDownload>();

    private int currentIndex = 0;

    public static synchronized AudioListManager get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AudioListManager(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private AudioListManager(Context applicationContext) {
        mContext = applicationContext;

        mDatabase = new XoClientDatabase(
                AndroidTalkDatabase.getInstance(applicationContext));
        try {
            mDatabase.initialize();
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }

        try {
            mAudioList = mDatabase.findClientDownloadByMediaType(ContentMediaType.AUDIO);
        } catch (SQLException e) {
            LOG.error("SQL query failed: " + e);
        }

        XoApplication.getXoClient().registerTransferListener(this);
    }

    @Override
    public boolean hasPrevious() {
        if (!mAudioList.isEmpty()){
            if (previousIndex() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        if (!mAudioList.isEmpty()) {
            if (nextIndex() < mAudioList.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TalkClientDownload previous() {
        return mAudioList.get(--currentIndex);
    }

    @Override
    public TalkClientDownload next() {
        return mAudioList.get(++currentIndex);
    }


    @Override
    public int previousIndex() {
        return currentIndex - 1;
    }

    @Override
    public int nextIndex() {
        return currentIndex + 1;
    }

    @Override
    public void remove() {
    }

    @Override
    public void set(TalkClientDownload talkClientDownload) {

    }

    @Override
    public void add(TalkClientDownload talkClientDownload) {

    }

    public List<TalkClientDownload> getAudioList() {
        return mAudioList;
    }

    private void notifyAudioListChanged() {
        for (DataSetObserver observer: mObservers){
            observer.onChanged();
        }
    }

    public void onDownloadRegistered(TalkClientDownload download) {
    }

    public void onDownloadStarted(TalkClientDownload download) {
    }

    public void onDownloadProgress(TalkClientDownload download) {
    }

    public void onDownloadFinished(TalkClientDownload download) {
        if(download.getContentMediaType().equals(ContentMediaType.AUDIO)){
            mAudioList.add(download);
            notifyAudioListChanged();
        }
    }

    public void onDownloadStateChanged(TalkClientDownload download) {
    }

    public void onUploadStarted(TalkClientUpload upload) {
    }

    public void onUploadProgress(TalkClientUpload upload) {
    }

    public void onUploadFinished(TalkClientUpload upload) {
    }

    public void onUploadStateChanged(TalkClientUpload upload) {
    }
}
