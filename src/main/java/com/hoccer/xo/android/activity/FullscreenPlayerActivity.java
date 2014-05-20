package com.hoccer.xo.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import com.hoccer.xo.android.fragment.FullscreenPlayerFragment;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends FragmentActivity {

    private BroadcastReceiver mBroadcastReceiver;

    private final static Logger LOG = Logger.getLogger(FullscreenPlayerActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_player);
    }

    @Override
    protected void onStart() {
        super.onStart();
        createBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    private void createBroadcastReceiver() {
        final FullscreenPlayerFragment playerFragment = (FullscreenPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_fullscreen_player);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    playerFragment.updatePlayState();
                }
                if (intent.getAction().equals(MediaPlayerService.TRACK_CHANGED_ACTION)) {
                    playerFragment.updateTrackData();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        intentFilter.addAction(MediaPlayerService.TRACK_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

}
