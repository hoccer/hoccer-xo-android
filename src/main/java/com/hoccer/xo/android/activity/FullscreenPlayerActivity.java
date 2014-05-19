package com.hoccer.xo.android.activity;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.android.fragment.FullscreenPlayerFragment;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends XoActivity {

    private BroadcastReceiver mBroadcastReceiver;

    private final static Logger LOG = Logger.getLogger(FullscreenPlayerActivity.class);
    private FullscreenPlayerFragment mFullscreenPlayerFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_fullscreen_player;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBroadcastReceiver();
        mFullscreenPlayerFragment = (FullscreenPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_fullscreen_player);
        enableUpNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    private void createBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    mFullscreenPlayerFragment.updatePlayState();
                }
                if (intent.getAction().equals(MediaPlayerService.TRACK_CHANGED_ACTION)) {
                    mFullscreenPlayerFragment.updateTrackData();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        intentFilter.addAction(MediaPlayerService.TRACK_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

}
