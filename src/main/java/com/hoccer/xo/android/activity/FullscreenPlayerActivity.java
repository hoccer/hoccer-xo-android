package com.hoccer.xo.android.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import com.hoccer.xo.android.fragment.FullscreenPlayerFragment;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.test.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends FragmentActivity {

    private BroadcastReceiver mBroadcastReceiver;

    private final static Logger LOG = Logger.getLogger(FullscreenPlayerActivity.class);
    private ServiceConnection mServiceConnection;

    private MediaPlayerService mMediaPlayerService;
    private FullscreenPlayerFragment mPlayerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_player);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlayerFragment = (FullscreenPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_fullscreen_player);
        bindMediaPlayerService();
        createBroadcastReceiver();
    }

    private void bindMediaPlayerService() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                    mMediaPlayerService = binder.getService();
                    mPlayerFragment.initView();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mPlayerFragment.enableViewComponents(false);
                }
            };
        }
        Intent serviceIntent = new Intent(this, MediaPlayerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    private void createBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    mPlayerFragment.updatePlayState();
                    if (mMediaPlayerService.isStopped()) {
                        finish();
                    }
                }
                if (intent.getAction().equals(MediaPlayerService.TRACK_CHANGED_ACTION)) {
                    mPlayerFragment.updateTrackData();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        intentFilter.addAction(MediaPlayerService.TRACK_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public MediaPlayerService getMediaPlayerService() {
        return mMediaPlayerService;
    }
}
