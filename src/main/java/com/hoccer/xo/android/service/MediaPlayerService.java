package com.hoccer.xo.android.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;
import com.hoccer.xo.android.activity.FullscreenPlayerActivity;
import com.hoccer.xo.android.content.MediaItem;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.List;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final int MUSIC_PLAYER_NOTIFICATION_ID = 1;
    public static final String PLAYSTATE_CHANGED_ACTION = "com.hoccer.xo.android.content.audio.PLAYSTATE_CHANGED_ACTION";
    public static final String TRACK_CHANGED_ACTION = "com.hoccer.xo.android.content.audio.TRACK_CHANGED_ACTION";


    private static final String UPDATE_PLAYSTATE_ACTION = "com.hoccer.xo.android.content.audio.UPDATE_PLAYSTATE_ACTION";
    private static final Logger LOG = Logger.getLogger(MediaPlayerService.class);

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
    private MediaMetaData mMediaMetaData;
    private CharSequence mFileName;
    private RemoteViews mNotificationViews;

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver;
    private MediaPlaylist mCurrentPlaylist;

    public class MediaPlayerBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        createBroadcastReceiver();
        createPlayStateTogglePendingIntent();
        registerPlayStateToggleIntentFilter();

        createAppFocusTracker();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    private void createAppFocusTracker() {

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (isApplicationKilled(getApplicationContext())) {
                        stopSelf();
                    } else {
                        if (isApplicationSentToBackground(getApplicationContext())) {
                            if (!isPaused() && !isStopped()) {
                                createNotification();
                                updateNotification();
                            }
                        } else {
                            removeNotification();
                        }
                    }
                }
            }
        }).start();
    }

    private boolean isApplicationKilled(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> runningTasks = am.getRecentTasks(1000, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        for (int i = 0; i < runningTasks.size(); ++i) {
            ActivityManager.RecentTaskInfo info = runningTasks.get(i);
            if (info.baseIntent.getComponent().getPackageName().equalsIgnoreCase(getApplication().getPackageName())) {
                return false;
            }
        }
        return true;
    }

    public boolean isApplicationSentToBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void createBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(UPDATE_PLAYSTATE_ACTION)) {
                    if (isPaused()) {
                        play(true);
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

    private void registerPlayStateToggleIntentFilter() {
        IntentFilter filter = new IntentFilter(UPDATE_PLAYSTATE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void createMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
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
        if (!isPaused()) {
            mNotificationViews.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_dark_content_pause);
        } else {
            mNotificationViews.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_dark_content_play);
        }
        mBuilder.setContent(mNotificationViews);
        startForeground(MUSIC_PLAYER_NOTIFICATION_ID, mBuilder.build());
    }

    private void createNotification() {
        Intent resultIntent = new Intent(this, FullscreenPlayerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
                .addParentStack(FullscreenPlayerActivity.class)
                .addNextIntent(resultIntent);

        mResultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationViews = createNotificationViews();

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_music)
                .setContent(mNotificationViews)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(mResultPendingIntent);

        mBuilder.setPriority(Notification.PRIORITY_MAX);
    }

    private RemoteViews createNotificationViews() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.view_audioplayer_notification);
        views.setOnClickPendingIntent(R.id.btn_play_pause, mPlayStateTogglePendingIntent);
        updateMetaDataView(views);
        return views;
    }

    private void updateMetaDataView(RemoteViews views) {
        String title = getString(R.string.media_meta_data_unknown_title);
        String artist = getString(R.string.media_meta_data_unknown_artist);
        String metaDataTitle = mMediaMetaData.getTitle();
        String metaDataArtist = mMediaMetaData.getArtist();
        boolean metaDataAvailable = false;
        if (metaDataTitle != null && !metaDataTitle.isEmpty()) {
            title = metaDataTitle;
            metaDataAvailable = true;
        }
        if (metaDataArtist != null && !metaDataArtist.isEmpty()) {
            artist = metaDataArtist;
            metaDataAvailable = true;
        }
        if (metaDataAvailable) {
            views.setViewVisibility(R.id.media_metadata_layout, View.VISIBLE);
            views.setViewVisibility(R.id.filename_text, View.GONE);
            views.setTextViewText(R.id.media_metadata_title_text, title);
            views.setTextViewText(R.id.media_metadata_artist_text, artist);
        } else {
            views.setViewVisibility(R.id.filename_text, View.VISIBLE);
            views.setViewVisibility(R.id.media_metadata_layout, View.GONE);
            views.setTextViewText(R.id.filename_text, mFileName);
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

    private void resetFileNameAndMetaData() {
        String path = Uri.parse(mCurrentMediaFilePath).getPath();
        mFileName = extractFileName(path);
        try {
            mMediaMetaData = MediaMetaData.create(path);
        } catch (IllegalArgumentException e) {
            LOG.error(e);
        }
    }

    private boolean isResumable(MediaPlaylist playlist) {
        return isPaused() && !playlistChanged(playlist) && !pathChanged(playlist.current().getFilePath());
    }

    public void start(MediaPlaylist playlist) {

        if (isResumable(playlist)) {
            resume();
        } else {
            if (mMediaPlayer == null) {
                createMediaPlayer();
            }
            if (playlistChanged(playlist)) {
                setCurrentPlaylist(playlist);
            }
            resetAndPrepareMediaPlayer(playlist.current().getFilePath());
        }

    }

    public void start() {
        if (mCurrentPlaylist != null) {
            start(mCurrentPlaylist);
        } else {
            LOG.error("No playlist available!");
        }
    }

    public void start(MediaPlaylist playlist, int position) {
        playlist.setCurrentIndex(position);
        start(playlist);
    }

    private boolean playlistChanged(MediaPlaylist playlist) {
        return mCurrentPlaylist != playlist;
    }

    private boolean pathChanged(String mediaFilePath) {
        return !mCurrentMediaFilePath.equals(mediaFilePath);
    }

    public void setCurrentPlaylist(MediaPlaylist playlist) {
        mCurrentPlaylist = playlist;
    }

    public MediaPlaylist getCurrentPlaylist() {
        return mCurrentPlaylist;
    }

    public void resume() {
        play(true);
    }

    public void play(boolean canResume) {
        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.start();
            setPaused(false);
            setStopped(false);
            if (!canResume) {
                setCurrentMediaFilePath(mTempMediaFilePath);
                resetFileNameAndMetaData();
                broadcastTrackChanged();
            }
            if (isNotificationActive()) {
                updateNotification();
            }
            broadcastPlayStateChanged();
        } else {
            LOG.debug("Audio focus request not granted");
        }
    }

    private void playNext() {
        MediaItem mediaItem = mCurrentPlaylist.nextByRepeatMode();
        if (mediaItem != null) {
            resetAndPrepareMediaPlayer(mediaItem.getFilePath());
        } else {
            stop();
        }
    }

    public void skipForward() {
        if (mCurrentPlaylist.size() > 0) {
            String path = mCurrentPlaylist.next().getFilePath();
            resetAndPrepareMediaPlayer(path);
        }
    }

    public void skipBackwards() {
        if (mCurrentPlaylist.size() > 0) {
            String path = mCurrentPlaylist.previous().getFilePath();
            resetAndPrepareMediaPlayer(path);
        }
    }

    public void pause() {
        mMediaPlayer.pause();
        setPaused(true);
        setStopped(false);
        if (isNotificationActive()) {
            updateNotification();
        }
        broadcastPlayStateChanged();
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            mMediaPlayer.release();
            mMediaPlayer = null;
            setPaused(false);
            setStopped(true);
            if (isNotificationActive()) {
                removeNotification();
            }
            broadcastPlayStateChanged();
        }
    }

    private boolean isNotificationActive() {
        return isApplicationSentToBackground(this);
    }

    public void setSeekPosition(int position) {
        mMediaPlayer.seekTo(position);
    }

    private void removeNotification() {
        stopForeground(true);
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
        play(false);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stopped;
    }

    public int getTotalDuration() {
        return (isStopped()) ? 0 : mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return (isStopped()) ? 0 : mMediaPlayer.getCurrentPosition();
    }

    public String getCurrentMediaFilePath() {
        return mCurrentMediaFilePath;
    }

    private void setCurrentMediaFilePath(String currentMediaFilePath) {
        mCurrentMediaFilePath = currentMediaFilePath;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void broadcastPlayStateChanged() {
        Intent intent = new Intent(PLAYSTATE_CHANGED_ACTION);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastTrackChanged() {
        Intent intent = new Intent(TRACK_CHANGED_ACTION);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private String extractFileName(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return fileName;
    }

    public MediaMetaData getMediaMetaData() {
        return mMediaMetaData;
    }
}
