package com.hoccer.xo.android.base;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.*;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import com.hoccer.xo.android.activity.FullscreenPlayerActivity;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;

/**
 * Activity keeps track of synchronizing the mediaplay icon
 * according to the mediaplayer state.
 * It can also be used to pause the current media.
 */
public abstract class XoActionbarActivity extends XoActivity {

    private MediaPlayerService mMediaPlayerService;
    private ServiceConnection mMediaPlayerServiceConnection;
    private Menu mMenu;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindMediaPlayerService(intent);
        createMediaPlayerBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mMediaPlayerServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean result = super.onCreateOptionsMenu(menu);

        mMenu = menu;
        updateActionBarIcons(menu);

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_media_player:
                openFullScreenPlayer();
                updateActionBarIcons(mMenu);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFullScreenPlayer(){
        Intent resultIntent = new Intent(this, FullscreenPlayerActivity.class);
        startActivity(resultIntent);
    }

    private void createMediaPlayerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    updateActionBarIcons(mMenu);
                }
            }
        };
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void updateActionBarIcons( Menu menu){
        if ( mMediaPlayerService != null && menu != null) {
            MenuItem mediaPlayerItem = menu.findItem(R.id.menu_media_player);

            if ( mMediaPlayerService.isStopped() || mMediaPlayerService.isPaused()) {
                mediaPlayerItem.setVisible(false);
            }else {
                mediaPlayerItem.setVisible(true);
            }
        }
    }

    private void bindMediaPlayerService(Intent intent) {

        mMediaPlayerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
                updateActionBarIcons( mMenu);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        bindService(intent, mMediaPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }
}
