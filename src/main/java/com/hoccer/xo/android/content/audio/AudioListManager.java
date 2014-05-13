package com.hoccer.xo.android.content.audio;

import android.content.*;
import android.os.IBinder;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.xo.android.database.AndroidTalkDatabase;
import com.hoccer.xo.android.service.MediaPlayerService;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AudioListManager implements Iterator<TalkClientDownload> {

    private static AudioListManager INSTANCE = null;

    private static final Logger LOG = Logger.getLogger(AudioListManager.class);

    private final Context mContext;
    private final XoClientDatabase mDatabase;

    private List<TalkClientDownload> mAudioAttachmentList = new ArrayList<TalkClientDownload>();

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
            mAudioAttachmentList = mDatabase.findClientDownloadByMediaType("audio");
        } catch (SQLException e) {
            LOG.error("SQL query failed: " + e);
        }
    }

    @Override
    public boolean hasNext() {
        if (!mAudioAttachmentList.isEmpty()) {
            if (currentIndex + 1 < mAudioAttachmentList.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TalkClientDownload next() {
        return mAudioAttachmentList.get(++currentIndex);
    }

    @Override
    public void remove() {
    }

    public List<TalkClientDownload> getAudioList() {
        return mAudioAttachmentList;
    }
}
