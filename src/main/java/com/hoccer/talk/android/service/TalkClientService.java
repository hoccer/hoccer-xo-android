package com.hoccer.talk.android.service;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.android.database.TalkDatabase;
import com.hoccer.talk.client.HoccerTalkClient;
import com.hoccer.talk.logging.HoccerLoggers;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;

public class TalkClientService extends OrmLiteBaseService<TalkDatabase> {

	private static final Logger LOG =
		HoccerLoggers.getLogger(TalkClientService.class);

    private static final AtomicInteger ID_COUNTER =
        new AtomicInteger();

    HoccerTalkClient mClient;

    ScheduledExecutorService mExecutor;

    ScheduledFuture<?> mShutdownFuture;

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        mClient = new HoccerTalkClient(mExecutor, getHelper());
	}

    @Override
    public void onDestroy() {
        LOG.info("onDestroy()");
        super.onDestroy();
        mExecutor.shutdownNow();
    }

    @Override
	public IBinder onBind(Intent intent) {
        LOG.info("onBind(" + intent.toString() + ")");
        try {
            getHelper().getMessageDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Connection();
	}

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.info("onUnbind(" + intent.toString() + ")");
        return super.onUnbind(intent);
    }

    private void scheduleShutdown() {
        shutdownShutdown();
        mShutdownFuture = mExecutor.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("shutting down due to keep-alive timeout");
                        stopSelf();
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

    public class Connection extends ITalkClientService.Stub {

        int mId;

        ITalkClientListener mListener;

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
        public void setListener(ITalkClientListener listener)
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
