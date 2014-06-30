package com.hoccer.xo.android.service;

import android.content.*;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Wraps the binding and callback mechanism.
 */
public class MediaPlayerServiceConnector {

    private Context mContext;
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

    public void connect(Context context, String intent, Listener listener) {
        disconnect();

        mContext = context;
        mIntent = intent;
        mListener = listener;

        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        context.startService(serviceIntent);
        bindMediaPlayerService(serviceIntent);
        createMediaPlayerBroadcastReceiver();
    }

    public void disconnect() {
        if(mIsConnected)
        {
            mContext.unbindService(mMediaPlayerServiceConnection);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
            mMediaPlayerService = null;
            mIsConnected = false;
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
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, filter);
    }

    private void bindMediaPlayerService(Intent intent) {

        mMediaPlayerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
                mIsConnected = true;
                mListener.onConnected(mMediaPlayerService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mListener.onDisconnected();
            }
        };

        mContext.bindService(intent, mMediaPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public MediaPlayerService getService() {
        return mMediaPlayerService;
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
