package com.hoccer.xo.android.content.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.hoccer.xo.android.activity.ContactsActivity;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by alexw on 28.04.14.
 */
public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayer.class);

    private static AudioPlayer INSTANCE = null;

    private int mId = 1;

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer = null;
    private NotificationCompat.Builder mBuilder;

    private Context mContext;
    private boolean paused = false;
    private boolean stopped = true;

    private String mCurrentMediaFilePath;
    private String mTempMediaFilePath;

    private List<PauseStateChangedListener> pauseStateChangedListeners = new ArrayList<PauseStateChangedListener>();
    private PendingIntent mResultPendingIntent;
    private String mTitle;
    private String mSubtitle;

    public static synchronized AudioPlayer get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AudioPlayer(context);
        }
        return INSTANCE;
    }

    public AudioPlayer(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    private void createMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {

            LOG.debug("AUDIO FOCUS CHANGED: " + focusChange);

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                LOG.debug("AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                LOG.debug("AUDIOFOCUS_GAIN");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                LOG.debug("AUDIOFOCUS_LOSS");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                LOG.debug("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            }
        }
    };

//    private BroadcastReceiver xxx;

    private void updateNotification() {

        mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(mTitle)
                .setContentText(mSubtitle)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(mResultPendingIntent);

        mBuilder.setPriority(Notification.PRIORITY_MAX);

        if(!isPaused()) {
            mBuilder.addAction(R.drawable.ic_dark_pause, "", mResultPendingIntent);
        } else {
            mBuilder.addAction(R.drawable.ic_dark_play, "", mResultPendingIntent);
        }
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mId, mBuilder.build());
    }

    private void addNotification() {

        Intent resultIntent = new Intent(mContext, ContactsActivity.class);
        mResultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mTitle = mCurrentMediaFilePath.substring(mCurrentMediaFilePath.lastIndexOf("/") + 1);
        mSubtitle = "Artist";

        updateNotification();

        Intent nextIntent = new Intent(mContext, ContactsActivity.class);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(mContext, 0, nextIntent, 0);



//        IntentFilter filter = new IntentFilter();
//        filter.addAction("1");
//
//        xxx = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals("1")) {
//                    //play
//                    Log.d("bla", "play");
//                } else {
//                    //pause
//                    Log.d( "bla", "pause");
//                }
//            }
//        };
//
//        mContext.registerReceiver(xxx, filter);

    }

    private void resetAndPrepareMediaPlayer(String mediaFilePath) {
        mTempMediaFilePath = mediaFilePath;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mediaFilePath);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            LOG.error("setFile: exception setting data source", e);
        }
    }

    public void start(String mediaFilePath) {
        if (isResumable(mediaFilePath)) {
            play();
        } else {
            if (mMediaPlayer == null) {
                createMediaPlayer();
            }
            resetAndPrepareMediaPlayer(mediaFilePath);
        }
    }

    private boolean isResumable(String mediaFilePath) {
        return isPaused() && isSamePath(mediaFilePath);
    }

    public void play() {

        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.start();
            setPaused(false);
            setStopped(false);
            setCurrentMediaFilePath(mTempMediaFilePath);
            addNotification();
            notifyPauseStateChangedListeners();
        } else {
            LOG.debug("Audio focus request not granted");
        }
    }

    public void pause() {
        mMediaPlayer.pause();
        setPaused(true);
        setStopped(false);
        updateNotification();
        notifyPauseStateChangedListeners();
    }

    public void stop() {
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        mMediaPlayer.release();
        mMediaPlayer = null;
        setPaused(false);
        setStopped(true);
        removeNotification();
        notifyPauseStateChangedListeners();
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mId);
    }

    private boolean isSamePath(String mediaFilePath) {
        return mCurrentMediaFilePath.equals(mediaFilePath);
    }

    private void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LOG.debug("onError(" + what + "," + extra + ")");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getCurrentMediaFilePath() {
        return mCurrentMediaFilePath;
    }

    private void setCurrentMediaFilePath(String currentMediaFilePath) {
        this.mCurrentMediaFilePath = currentMediaFilePath;
    }

    public interface PauseStateChangedListener {
        void onPauseStateChanged();
    }

    public void addPauseStateChangedListener(PauseStateChangedListener l) {
        pauseStateChangedListeners.add(l);
    }

    public void removePauseStateChangedListener(PauseStateChangedListener l) {
        pauseStateChangedListeners.remove(l);
    }

    private void notifyPauseStateChangedListeners() {
        for (PauseStateChangedListener l : pauseStateChangedListeners) {
            l.onPauseStateChanged();
        }
    }

}
