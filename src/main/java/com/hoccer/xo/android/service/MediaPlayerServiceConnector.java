package com.hoccer.xo.android.service;

import android.app.Activity;
import android.content.*;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Wraps the binding and callback mechanism.
 */
public class MediaPlayerServiceConnector {

    private Activity mActivity;
    private String mIntent;
    private Listener mListener;

    private boolean mIsConnected = false;

    private MediaPlayerService mMediaPlayerService;
    private ServiceConnection mMediaPlayerServiceConnection;
    private BroadcastReceiver mBroadcastReceiver;

    public interface Listener {
        void onConnected(MediaPlayerService service);
        void onDisconnected();
        void onAction(String action, MediaPlayerService service);
    }

    public void connect(Activity activity, String intent, Listener listener) {
        disconnect();

        mActivity = activity;
        mIntent = intent;
        mListener = listener;

        Intent serviceIntent = new Intent(activity, MediaPlayerService.class);
        activity.startService(serviceIntent);
        bindMediaPlayerService(serviceIntent);
        createMediaPlayerBroadcastReceiver();
    }

    public void disconnect() {
        if(mIsConnected)
        {
            mActivity.unbindService(mMediaPlayerServiceConnection);
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void createMediaPlayerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mListener.onAction(intent.getAction(), mMediaPlayerService);
            }
        };
        IntentFilter filter = new IntentFilter(mIntent);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, filter);
    }

    private void bindMediaPlayerService(Intent intent) {

        mMediaPlayerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
                mListener.onConnected(mMediaPlayerService);
                mIsConnected = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mListener.onDisconnected();
            }
        };

        mActivity.bindService(intent, mMediaPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
