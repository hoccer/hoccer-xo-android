package com.hoccer.xo.android.content.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.hoccer.xo.android.activity.ContactsActivity;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String PLAYSTATE_CHANGED_ACTION = "com.hoccer.xo.android.content.audio.playStateChangedAction";

    private static final String UPDATE_PLAYSTATE_ACTION = "com.hoccer.xo.android.content.audio.updatePlayStateAction";
    private final static Logger LOG = Logger.getLogger(MediaPlayerService.class);

    private int mId = 1;

    private String mArtist = "";
    private String mTitle = "";

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer = null;
    private NotificationCompat.Builder mBuilder;

    private boolean paused = false;
    private boolean stopped = true;

    private String mCurrentMediaFilePath;
    private String mTempMediaFilePath;

    private PendingIntent mResultPendingIntent;

    private PendingIntent mPlayStateTogglePendingIntent;
    private final IBinder mBinder = new MediaPlayerBinder();

    public class MediaPlayerBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCreate(){
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        createBroadcastReceiver();
        createPlayStateTogglePendingIntent();
        registerPlayStateToggleIntentFilter();
    }

    private void registerPlayStateToggleIntentFilter() {
        IntentFilter filter = new IntentFilter(UPDATE_PLAYSTATE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void createBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(UPDATE_PLAYSTATE_ACTION)) {
                    if (isPaused()) {
                        play();
                    } else {
                        pause();
                    }
                }
            }
        };
    }

    private void createPlayStateTogglePendingIntent() {
        Intent nextIntent = new Intent(UPDATE_PLAYSTATE_ACTION);
        mPlayStateTogglePendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
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

    private void updateNotification() {

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(mTitle)
                .setContentText(mArtist)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(mResultPendingIntent);

        mBuilder.setPriority(Notification.PRIORITY_MAX);

        if (!isPaused()) {
            mBuilder.addAction(R.drawable.ic_dark_pause, "", mPlayStateTogglePendingIntent);
        } else {
            mBuilder.addAction(R.drawable.ic_dark_play, "", mPlayStateTogglePendingIntent);
        }

        startForeground(mId, mBuilder.build());
    }

    private BroadcastReceiver mReceiver;

    private void addNotification() {

        Intent resultIntent = new Intent(this, ContactsActivity.class);
        mResultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String path = Uri.parse(mCurrentMediaFilePath).getPath();

        updateMetaData(path);
        updateNotification();
    }

    private void updateMetaData(String path) {
        MediaMetadataRetriever mediaDataBla = new MediaMetadataRetriever();
        try {
            mediaDataBla.setDataSource(path);
            mArtist = mediaDataBla.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            mTitle = mediaDataBla.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }catch(IllegalArgumentException e){
            LOG.error("Failed to set media data! " + e.getMessage());
        }

        if (mTitle == null || mTitle.isEmpty()) {
            mTitle = path.substring(path.lastIndexOf("/") + 1);
        }
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
            broadcastPlayState();
        } else {
            LOG.debug("Audio focus request not granted");
        }
    }

    public void pause() {
        mMediaPlayer.pause();
        setPaused(true);
        setStopped(false);
        updateNotification();
        broadcastPlayState();
    }

    public void stop() {
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        mMediaPlayer.release();
        mMediaPlayer = null;
        setPaused(false);
        setStopped(true);
        removeNotification();
        broadcastPlayState();
    }

    public void setSeekPosition( int position ){
        long totalDuration = getTotalDuration();
        int currentPosition = progressToTimer(position, (int) totalDuration);

        mMediaPlayer.seekTo(currentPosition);
    }

    public void registerOnCompletitionListener(MediaPlayer.OnCompletionListener listener){
        mMediaPlayer.setOnCompletionListener(listener);
    }

    private void removeNotification() {
        stopForeground(true);
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


    public long getTotalDuration() {
        return mMediaPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public String getCurrentMediaFilePath() {
        return mCurrentMediaFilePath;
    }

    private void setCurrentMediaFilePath(String currentMediaFilePath) {
        this.mCurrentMediaFilePath = currentMediaFilePath;
    }

    private int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void broadcastPlayState() {
        Intent intent = new Intent(PLAYSTATE_CHANGED_ACTION);
        this.sendBroadcast(intent);
    }
}
