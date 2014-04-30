package com.hoccer.xo.android.content.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.view.View;
import com.hoccer.xo.android.view.AudioPlayerView;
import org.apache.log4j.Logger;

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

    private String mAudioPlayerPath;
    private String mTempPlayerPath;

    private AudioPlayerView mTempActivePlayerView;
    private AudioPlayerView mActivePlayerView;

    public static synchronized AudioPlayer get(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new AudioPlayer(context);
        }
        return INSTANCE;
    }

    public AudioPlayer(Context context){

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
            } else if( focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                LOG.debug("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            }
        }
    };

    private void createNotification(){

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

    private void initMediaPlayer(String path){
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();

            mTempPlayerPath = path;
        } catch (Exception e) {
            LOG.error("setFile: exception setting data source", e);
        }
    }

    public void start(AudioPlayerView audioPlayerView) {

        if (isPaused() && isSameView(audioPlayerView)) {
            mMediaPlayer.start();
            mActivePlayerView.setPlayState();
        } else {
            mTempActivePlayerView = audioPlayerView;
            initMediaPlayer(audioPlayerView.getPlayerViewPath());
        }
    }

    private boolean isSameView(View audioPlayerView) {
        return mActivePlayerView == audioPlayerView;
    }

    public void pause(){

        muteMusic();
        mMediaPlayer.pause();
        setPaused(true);

        mActivePlayerView.setPauseState();
    }

    private void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void muteMusic(){

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

            mMediaPlayer.start();
            mMediaPlayer.setVolume(1.0f, 1.0f);
            mAudioPlayerPath = mTempPlayerPath;

            if (mActivePlayerView != null) {
                mActivePlayerView.setStopState();
            }
            mActivePlayerView = mTempActivePlayerView;
            mActivePlayerView.setPlayState();
        }
        else{
            LOG.debug("Audio focus request not granted");
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public String getAudioPlayerPath() {
        return mAudioPlayerPath;
    }
}
