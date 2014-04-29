package com.hoccer.xo.android.content.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import org.apache.log4j.Logger;

import com.hoccer.xo.android.view.AudioPlayerView;

/*
 * Created by alexw on 28.04.14.
 */
public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private final static Logger LOG = Logger.getLogger(AudioPlayer.class);

    private static AudioPlayer INSTANCE = null;

    private int mId = 1;
    private String mCurrentPath = "";

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private Notification.Builder mBuilder;

    private Context mParentContext;
    private boolean paused = false;

    public static synchronized AudioPlayer get(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new AudioPlayer(context);
        }
        return INSTANCE;
    }

    public AudioPlayer(Context context){

        mParentContext = context;
        mAudioManager = (AudioManager) mParentContext.getSystemService(Context.AUDIO_SERVICE);

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

//        final Intent emptyIntent = new Intent(mParentContext, AudioPlayer.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mParentContext, 0, emptyIntent, 0);
//
//        mBuilder = new Notification.Builder(mParentContext)
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
//        NotificationManager notificationManager = (NotificationManager) mParentContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(mId, mBuilder.build());
    }

    private void setUrl(String path){
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();

            mCurrentPath = path;
        } catch (Exception e) {
            LOG.error("setFile: exception setting data source", e);
        }
    }

    public String getCurrentPath(){
        return mCurrentPath;
    }

    public void start(String path) {

        if (isPaused() && isSameTrack(path)) {
            mMediaPlayer.start();
        } else {
            setUrl(path);
        }
    }

    private boolean isSameTrack(String path) {
        return mCurrentPath == path;
    }

    public void stop(){

        muteMusic();

        mCurrentPath = null;

        //TODO! calling these crashes the app

        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    public void pause(){

        muteMusic();
        mMediaPlayer.pause();
        setPaused(true);
    }

    private void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void muteMusic(){

        NotificationManager notificationManager = (NotificationManager) mParentContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mId);

        // Abandon audio focus when playback complete
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public boolean isPlaying(String fileName) {
        return fileName.equals(mCurrentPath);
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
        }
        else{
            LOG.debug("Audio focus request not granted");
        }
    }

    public boolean isPaused() {
        return paused;
    }
}
