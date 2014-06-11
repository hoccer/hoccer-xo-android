package com.hoccer.xo.android.view;

import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioAttachmentView extends LinearLayout implements View.OnClickListener {

    private Context mContext;
    private AudioAttachmentItem mAudioAttachmentItem;
    private ServiceConnection mConnection;
    private BroadcastReceiver mReceiver;
    private MediaPlayerService mMediaPlayerService;
    private DownloadArtworkTask mCurrentTask = null;

    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private ImageView mArtworkImageView;

    private static final Logger LOG = Logger.getLogger(AudioAttachmentView.class);

    public AudioAttachmentView(Context context) {
        super(context);
        mContext = context;
        addView(inflate(mContext, R.layout.item_audio_attachment, null));

        mTitleTextView = ((TextView) findViewById(R.id.tv_title_name));
        mArtistTextView = ((TextView) findViewById(R.id.tv_artist_name));
        mArtworkImageView = ((ImageView) findViewById(R.id.iv_artcover));
    }

    public void setMediaItem(AudioAttachmentItem audioAttachmentItem) {
        if (mAudioAttachmentItem == null || !mAudioAttachmentItem.getFilePath().equals(audioAttachmentItem.getFilePath())) {
            mAudioAttachmentItem = audioAttachmentItem;
            updateAudioView();
        }
    }

    private void updateAudioView() {
        mTitleTextView.setText(mAudioAttachmentItem.getMetaData().getTitleOrFilename(mAudioAttachmentItem.getFilePath()));

        String artist = mAudioAttachmentItem.getMetaData().getArtist();
        if (artist == null || artist.isEmpty()){
            artist = getResources().getString(R.string.media_meta_data_unknown_artist);
        }

        mArtistTextView.setText(artist);
        if (mCurrentTask != null && mCurrentTask.getStatus() != AsyncTask.Status.FINISHED) {
            mCurrentTask.cancel(true);
        }
        if (mAudioAttachmentItem.getMetaData().getArtwork() == null) {
            mCurrentTask = new DownloadArtworkTask();
            mCurrentTask.execute();
        } else {
            AudioAttachmentView.this.mArtworkImageView.setImageDrawable(mAudioAttachmentItem.getMetaData().getArtwork());
        }
    }

    public boolean isActive() {
        if (isBound()) {
            AudioAttachmentItem currentItem = mMediaPlayerService.getCurrentMediaItem();
            return !mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped() && ((mAudioAttachmentItem.getFilePath()).equals(currentItem.getFilePath()));
        } else {
            return false;
        }
    }

    public void updatePlayPauseView() {
        View view = findViewById(R.id.iv_playing_status);
        if (isActive()) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.startService(intent);
        bindService(intent);

        createBroadcastReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unbindService(mConnection);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void onClick(View v) {
    }

    private void bindService(Intent intent) {

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();

                updatePlayPauseView();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    private void createBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    updatePlayPauseView();
                }
            }
        };
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    }

    public boolean isBound() {
        return mMediaPlayerService != null;
    }

    private class DownloadArtworkTask extends AsyncTask<Void, Void, Drawable> {

        protected Drawable doInBackground(Void... params) {
            byte[] artworkRaw = MediaMetaData.getArtwork(mAudioAttachmentItem.getFilePath());
            if (artworkRaw != null) {
                return new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(artworkRaw, 0, artworkRaw.length));
            } else {
                return null;
            }
        }

        protected void onPostExecute(Drawable artwork) {
            super.onPostExecute(artwork);
            if (!this.isCancelled()) {
                mAudioAttachmentItem.getMetaData().setArtwork(artwork);
                AudioAttachmentView.this.mArtworkImageView.setImageDrawable(artwork);
            }
        }
    }
}
