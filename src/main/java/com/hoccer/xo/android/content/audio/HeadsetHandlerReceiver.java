package com.hoccer.xo.android.content.audio;

import android.content.*;
import android.os.IBinder;
import com.hoccer.xo.android.service.MediaPlayerService;

public class HeadsetHandlerReceiver extends BroadcastReceiver{

    private ServiceConnection mServiceConnection;
    private MediaPlayerService mMediaPlayerService;

    @Override
    public void onReceive(Context context, Intent intent) {

        if( intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){

            int headSetState = intent.getIntExtra("state", 0);
            if (headSetState == 0){
                bindMediaPlayerService(context);
            }
        }
    }

    private void bindMediaPlayerService(Context context) {

        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                    mMediaPlayerService = binder.getService();
                    mMediaPlayerService.pause();
                    mMediaPlayerService.unbindService(mServiceConnection);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
        }
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
}
