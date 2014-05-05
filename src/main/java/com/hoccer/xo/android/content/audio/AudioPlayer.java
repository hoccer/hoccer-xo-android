package com.hoccer.xo.android.content.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by alexw on 28.04.14.
 */
public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayer.class);

    private static AudioPlayer INSTANCE = null;

    private int mId = 1;

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private Notification.Builder mBuilder;

    private Context mContext;
    private boolean paused = false;

    private String mCurrentMediaFilePath;
    private String mTempMediaFilePath;

    private List<PauseStateChangedListener> pauseStateChangedListeners = new ArrayList<PauseStateChangedListener>();

    public static synchronized AudioPlayer get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AudioPlayer(context);
        }
        return INSTANCE;
    }

    public AudioPlayer(Context context) {

        mContext = context;

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
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

    private void createNotification() {

//        final Intent emptyIntent = new Intent(mContext, AudioPlayer.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, emptyIntent, 0);
//
//        mBuilder = new Notification.Builder(mContext)
//                .setSmallIcon(R.drawable.logo)
//                .setContentTitle("Awesome Author")
//                .setContentText("Awesome Track")
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(false)
//                .addAction(R.drawable.btn01, "Click", pendingIntent)
//                .addAction(R.drawable.btn02, "Me", pendingIntent)
//                .addAction(R.drawable.btn03, "Please", pendingIntent);
//
//        mBuilder.setPriority(100);
//
//        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(mId, mBuilder.build());
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
            resetAndPrepareMediaPlayer(mediaFilePath);
        }
    }

    private boolean isResumable(String mediaFilePath) {
        return isPaused() && isSamePath(mediaFilePath);
    }

    public void play() {
        mMediaPlayer.start();
        setPaused(false);

        notifyPauseStateChangedListeners();
    }

    public void pause() {

        muteMusic();
        mMediaPlayer.pause();
        setPaused(true);

        notifyPauseStateChangedListeners();
    }

    private boolean isSamePath(String mediaFilePath) {
        return mCurrentMediaFilePath == mediaFilePath;
    }

    private void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void muteMusic() {

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mId);

        // Abandon audio focus when playback complete
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LOG.debug("onError(" + what + "," + extra + ")");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LOG.debug("onPrepared()");

        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            createNotification();
            setCurrentMediaFilePath(mTempMediaFilePath);
            play();
        } else {
            LOG.debug("Audio focus request not granted");
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public String getCurrentMediaFilePath() {
        return mCurrentMediaFilePath;
    }

//    public List<PauseStateChangedListener> getListeners() {
//        return pauseStateChangedListeners;
//    }

    public void removePauseStateChangedListeners() {
        pauseStateChangedListeners.clear();
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
