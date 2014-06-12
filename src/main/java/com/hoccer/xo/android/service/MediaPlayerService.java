package com.hoccer.xo.android.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.activity.FullscreenPlayerActivity;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class MediaPlayerService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public enum PlaylistType {
        ALL_MEDIA,
        CONVERSATION_MEDIA,
        SINGLE_MEDIA;
    }

    private PlaylistType mPlaylistType = PlaylistType.ALL_MEDIA;

    public static final int UNDEFINED_CONTACT_ID = -1;

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

    private AudioAttachmentItem mCurrentAudioAttachmentItem;
    private AudioAttachmentItem mTempAudioAttachmentItem;

    private PendingIntent mResultPendingIntent;

    private PendingIntent mPlayStateTogglePendingIntent;
    private final IBinder mBinder = new MediaPlayerBinder();
    private RemoteViews mNotificationViews;

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mHeadsetStateBroadcastReceiver;
    private MediaPlaylist mPlaylist = new MediaPlaylist();

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
        createHeadsetHandlerReceiver();
    }

    private void createHeadsetHandlerReceiver() {

        mHeadsetStateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {

                    int headSetState = intent.getIntExtra("state", 0);

                    if (headSetState == 0) {
                        pause();
                    }
                }
            }
        };

        IntentFilter receiverFilter = new IntentFilter(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mHeadsetStateBroadcastReceiver, receiverFilter);
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

    private boolean isApplicationSentToBackground(Context context) {
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
                        play(mCurrentAudioAttachmentItem);
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
        AudioAttachmentItem item = mCurrentAudioAttachmentItem;
        String metaDataTitle = item.getMetaData().getTitle();
        String metaDataArtist = item.getMetaData().getArtist();
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
            views.setTextViewText(R.id.filename_text, item.getFileName());
        }
    }

    private void resetAndPrepareMediaPlayer(AudioAttachmentItem audioAttachmentItem) {
        mTempAudioAttachmentItem = audioAttachmentItem;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(audioAttachmentItem.getFilePath().replace("file:///", "/"));
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            LOG.error("setFile: exception setting data source", e);
        }
    }

    public int getMediaListSize() {
        return mPlaylist.size();
    }

    public void play(int position) {
        mPlaylist.setCurrentTrackNumber(position);
        playNewTrack(mPlaylist.current());
    }

    public void play() {
        if (isStopped()) {
            mPlaylist.setCurrentTrackNumber(0);
        }
        play(mPlaylist.current());
    }

    public void play(final AudioAttachmentItem audioAttachmentItem) {
        if (mMediaPlayer == null) {
            createMediaPlayerAndPlay(audioAttachmentItem);
        } else {
            startPlaying();
        }
    }

    private void createMediaPlayerAndPlay(final AudioAttachmentItem audioAttachmentItem) {
        createMediaPlayer();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                play(audioAttachmentItem);
            }
        });
        resetAndPrepareMediaPlayer(audioAttachmentItem);
    }

    private void startPlaying() {
        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.start();
            setPaused(false);
            setStopped(false);
            if (!canResume()) {
                mCurrentAudioAttachmentItem = mTempAudioAttachmentItem;
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

    private boolean canResume() {
        return isPaused();
    }

    private void playNewTrack(AudioAttachmentItem audioAttachmentItem) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        play(audioAttachmentItem);
    }

    public void playNextByRepeatMode() {
        AudioAttachmentItem audioAttachmentItem = mPlaylist.nextByRepeatMode();
        if (audioAttachmentItem != null) {
            playNewTrack(audioAttachmentItem);
        } else {
            stop();
        }
    }

    public void playNext() {
        if (mPlaylist.size() > 0) {
            playNewTrack(mPlaylist.next());
        }
    }

    public void playPrevious() {
        if (mPlaylist.size() > 0) {
            playNewTrack(mPlaylist.previous());
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

    public void setSeekPosition(final int position) {
        if (mMediaPlayer == null) {
            createMediaPlayer();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (isStopped()) {
                        setStopped(false);
                        setPaused(true);
                    }
                    mMediaPlayer.seekTo(position);
                }
            });
            resetAndPrepareMediaPlayer(mCurrentAudioAttachmentItem);
        } else {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LOG.debug("onError(" + what + "," + extra + ")");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextByRepeatMode();
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
        return (isStopped()) ? 0 : mPlaylist.getCurrentTrackNumber();
    }

    public int getCurrentTrackNumber() {
        return mPlaylist.getCurrentTrackNumber();
    }

    public int getCurrentProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    public AudioAttachmentItem getCurrentMediaItem() {
        return mCurrentAudioAttachmentItem;
    }

    public int getCurrentConversationContactId() {
        int conversationContactId = UNDEFINED_CONTACT_ID;

        try {
            TalkClientMessage message;
            if (getCurrentMediaItem().getContentObject() instanceof  TalkClientDownload) {
                int attachmentId = ((TalkClientDownload) getCurrentMediaItem().getContentObject()).getClientDownloadId();
                message = XoApplication.getXoClient().getDatabase().findClientMessageByTalkClientDownloadId(attachmentId);
                if (message != null) {
                    conversationContactId = message.getSenderContact().getClientContactId();
                }
            } else if (getCurrentMediaItem().getContentObject() instanceof TalkClientUpload) {
                int attachmentId = ((TalkClientUpload) getCurrentMediaItem().getContentObject()).getClientUploadId();
                message = XoApplication.getXoClient().getDatabase().findClientMessageByTalkClientUploadId(attachmentId);
                if (message != null) {
                    conversationContactId = message.getSenderContact().getClientContactId();
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        return conversationContactId;
    }

    public PlaylistType getPlaylistType() {
        return mPlaylistType;
    }

    public void setMedia(AudioAttachmentItem item) {
        mPlaylist.clear();
        mPlaylist.add(0, item);
        mPlaylistType = PlaylistType.SINGLE_MEDIA;
    }

    public void setMediaList(List<AudioAttachmentItem> itemList, int conversationContactId) {
        mPlaylist.clear();
        mPlaylist.addAll(itemList);

        if (conversationContactId == UNDEFINED_CONTACT_ID) {
            mPlaylistType = PlaylistType.ALL_MEDIA;
        } else {
            mPlaylistType = PlaylistType.CONVERSATION_MEDIA;
        }
    }

    public void addMedia(AudioAttachmentItem item) {
        mPlaylist.add(0, item);
    }

    public void removeMedia(int pos) {
        if (mPlaylist.size() > 0) {
            mPlaylist.remove(pos);
        }
    }

    public void updatePosition(int pos) {
        mPlaylist.setCurrentTrackNumber(pos);
    }

    public MediaPlaylist.RepeatMode getRepeatMode() {
        return mPlaylist.getRepeatMode();
    }

    public void setRepeatMode(MediaPlaylist.RepeatMode mode) {
        mPlaylist.setRepeatMode(mode);
    }

    public boolean isShuffleActive() {
        return mPlaylist.isShuffleActive();
    }

    public void setShuffleActive(boolean isActive) {
        mPlaylist.setShuffleActive(isActive);
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


    private void removeNotification() {
        stopForeground(true);
    }

    private void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

}
