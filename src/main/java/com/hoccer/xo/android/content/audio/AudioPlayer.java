package com.hoccer.xo.android.content.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import org.apache.log4j.Logger;

import com.hoccer.xo.android.view.AudioPlayerView;

/**
 * Created by alexw on 28.04.14.
 */
public class AudioPlayer {

    private final static Logger LOG = Logger.getLogger(AudioPlayer.class);

    private static AudioPlayer INSTANCE = null;

    private int mId = 1;
    private String mCurrentPath = "";

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private Notification.Builder mBuilder;

    private Context mParentContext;

    public static synchronized AudioPlayer get(Context context, AudioPlayerView playerView) {
        if(INSTANCE == null) {
            INSTANCE = new AudioPlayer(context, playerView);
        }
        return INSTANCE;
    }

    public AudioPlayer(Context context, AudioPlayerView playerView){

        mParentContext = context;
        mAudioManager = (AudioManager) mParentContext.getSystemService(Context.AUDIO_SERVICE);

        mMediaPlayer = new MediaPlayer();
//        mMediaPlayer.setOnErrorListener(playerView);
//        mMediaPlayer.setOnPreparedListener(playerView);
//        mMediaPlayer.setOnCompletionListener(playerView);
    }

    private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {

            LOG.debug("AUDIO FOCUS CHANGED: " + focusChange);

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                LOG.debug("AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                LOG.debug("AUDIOFOCUS_GAIN");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                LOG.debug("AUDIOFOCUS_LOSS");

            } else if( focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
                LOG.debug("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            }
        }
    };

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_musicplayer);
//
//        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//    }

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
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();

            mCurrentPath = path;
        } catch (Exception e) {
            //LOG.error("setFile: exception setting data source", e);
        }
    }

    public String getCurrentPath(){
        return mCurrentPath;
    }

    public void start(String audioPath){

        setUrl(audioPath);

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

    public void stop(){

        muteMusic();

        //TODO! calling these crashes the app

        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    public void pause(){

        muteMusic();

        //TODO! calling these crashes the app

        mMediaPlayer.pause();
//        mMediaPlayer.release();
    }

    private void muteMusic(){
        mCurrentPath = null;

        NotificationManager notificationManager = (NotificationManager) mParentContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mId);

        // Abandon audio focus when playback complete
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public boolean isPlaying(String fileName){
        return fileName.equals(mCurrentPath);
    }

//    @Override
//    public boolean onError(MediaPlayer mp, int what, int extra) {
//        LOG.debug("onError(" + what + "," + extra + ")");
//        mPlayPause.setEnabled(false);
//        mPlayPause.setImageResource(R.drawable.ic_dark_play);
//        return false;
//    }
//
//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        LOG.debug("onPrepared()");
//        mPlayPause.setEnabled(true);
//        mPlayPause.setImageResource(R.drawable.ic_dark_play);
//    }
//
//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        LOG.debug("onCompletion()");
//        mPlayPause.setImageResource(R.drawable.ic_dark_play);
//    }
}
