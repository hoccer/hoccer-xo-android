package com.hoccer.xo.android.content.audio;

import android.content.*;
import android.os.IBinder;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.xo.android.service.MediaPlayerService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AudioListManager implements Iterator<TalkClientDownload> {

    private static AudioListManager INSTANCE = null;

    private static final Logger LOG = Logger.getLogger(AudioListManager.class);

    private final Context mContext;

    private List<TalkClientDownload> audioAttachmentList = new ArrayList<TalkClientDownload>();

    private int currentIndex = 0;

    private MediaPlayerService mMediaPlayerService;
    private ServiceConnection mConnection;

    public static synchronized AudioListManager get(Context applicationContext) {
        if (INSTANCE == null) {
            INSTANCE = new AudioListManager(applicationContext);
        }
        return INSTANCE;
    }

    private AudioListManager(Context applicationContext) {
        mContext = applicationContext;

        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.startService(intent);
        bindService(intent);
        createBroadcastReceiver();
    }

    private void bindService(Intent intent) {

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void createBroadcastReceiver() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    if (mMediaPlayerService.isStopped() && hasNext()) {
                        String path = next().getContentDataUrl();
                        mMediaPlayerService.start(path);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        mContext.registerReceiver(receiver, filter);
    }

    @Override
    public boolean hasNext() {
        if (!audioAttachmentList.isEmpty()) {
            if (currentIndex + 1 < audioAttachmentList.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TalkClientDownload next() {
        return audioAttachmentList.get(++currentIndex);
    }

    @Override
    public void remove() {
    }

    public void setAudioAttachmentList(List<TalkClientDownload> audioAttachmentList) {
        this.audioAttachmentList = audioAttachmentList;
    }

    public void playNext() {
        if (mMediaPlayerService != null) {
            if (hasNext()) {
                mMediaPlayerService.start(next().getContentDataUrl());
            }
        }
    }
}
