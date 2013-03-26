package com.hoccer.talk.android.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.android.database.TalkDatabase;
import com.hoccer.talk.client.HoccerTalkClient;
import com.hoccer.talk.client.ITalkClientListener;
import com.hoccer.talk.logging.HoccerLoggers;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;

/**
 * Android service for Hoccer Talk
 *
 * This service wraps a Talk client instance for use by Android applications.
 *
 * It should be started with startService() and kept alive using keepAlive() RPC calls
 * for as long as it is needed. If not called regularly the service will stop itself.
 *
 *
 */
public class TalkClientService extends OrmLiteBaseService<TalkDatabase> implements ITalkClientListener {

	private static final Logger LOG =
		HoccerLoggers.getLogger(TalkClientService.class);

    private static final AtomicInteger ID_COUNTER =
        new AtomicInteger();

    /** Connectivity manager for monitoring */
    ConnectivityManager mConnectivityManager;

    /** Our connectivity change broadcast receiver */
    ConnectivityReceiver mConnectivityReceiver;

    /** Executor for ourselves and the client */
    ScheduledExecutorService mExecutor;

    /** Hoccer client that we serve */
    HoccerTalkClient mClient;

    /** Reference to latest auto-shutdown future */
    ScheduledFuture<?> mShutdownFuture;

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        mClient = new HoccerTalkClient(mExecutor, getHelper());
        mClient.registerListener(this);
	}

    @Override
    public void onDestroy() {
        LOG.info("onDestroy()");
        super.onDestroy();
        mExecutor.shutdownNow();
        unregisterConnectivityReceiver();
    }

    @Override
	public IBinder onBind(Intent intent) {
        LOG.info("onBind(" + intent.toString() + ")");
        if(!mClient.isActivated()) {
            mClient.activate();
        }

        return new Connection();
	}

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.info("onUnbind(" + intent.toString() + ")");
        return super.onUnbind(intent);
    }

    @Override
    public void onClientStateChange(HoccerTalkClient client, int state) {
        LOG.info("onClientStateChange(" + HoccerTalkClient.stateToString(state) + ")");
        if(state == HoccerTalkClient.STATE_IDLE) {
            registerConnectivityReceiver();
            handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());
        }
        if(state == HoccerTalkClient.STATE_INACTIVE) {
            unregisterConnectivityReceiver();
        }
    }

    private void doShutdown() {
        LOG.info("shutting down");
        // command the client to deactivate
        if(mClient.isActivated()) {
            mClient.deactivateNow();
        }
        // stop ourselves
        stopSelf();
    }

    private void scheduleShutdown() {
        shutdownShutdown();
        mShutdownFuture = mExecutor.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("keep-alive timeout");
                        doShutdown();
                    }
                },
                TalkConfiguration.SERVICE_KEEPALIVE_TIMEOUT, TimeUnit.SECONDS
        );
    }

    private void shutdownShutdown() {
        if(mShutdownFuture != null) {
            mShutdownFuture.cancel(false);
            mShutdownFuture = null;
        }
    }

    private void registerConnectivityReceiver() {
        LOG.info("registerConnectivityReceiver()");
        if(mConnectivityReceiver == null) {
            mConnectivityReceiver = new ConnectivityReceiver();
            registerReceiver(mConnectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void unregisterConnectivityReceiver() {
        LOG.info("unregisterConnectivityReceiver()");
        if(mConnectivityReceiver != null) {
            unregisterReceiver(mConnectivityReceiver);
            mConnectivityReceiver = null;
        }
    }

    private void handleConnectivityChange(NetworkInfo activeNetwork) {
        LOG.info("connectivity change:"
                + " type " + activeNetwork.getTypeName()
                + " state " + activeNetwork.getState().name());
    }

    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.info("onConnectivityChange()");
            handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());
        }
    }

    public class Connection extends ITalkClientService.Stub {

        int mId;

        ITalkClientServiceListener mListener;

        Connection() {
            mId = ID_COUNTER.incrementAndGet();
            mListener = null;
            LOG.info("[" + mId + "] connected");
        }

		@Override
		public void keepAlive()
                throws RemoteException {
            LOG.info("[" + mId + "] keepAlive()");
            scheduleShutdown();
		}

        @Override
        public void wake()
                throws RemoteException {
            LOG.info("[" + mId + "] wake()");
            mClient.wake();
        }

        @Override
        public void setListener(ITalkClientServiceListener listener)
                throws RemoteException {
            LOG.info("[" + mId + "] setListener()");
            mListener = listener;
        }

        @Override
        public void messageCreated(String messageTag) throws RemoteException {
            LOG.info("[" + mId + "] messageCreated(" + messageTag + ")");
            mClient.tryToDeliver(messageTag);
        }

    }

}
