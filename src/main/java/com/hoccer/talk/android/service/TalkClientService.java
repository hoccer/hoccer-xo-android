package com.hoccer.talk.android.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        mClient = new HoccerTalkClient(getHelper());
	}

	@Override
	public IBinder onBind(Intent intent) {
        LOG.info("onBind(" + intent.toString() + ")");
        getHelper();
		return new Connection();
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
		}

        @Override
        public void setListener(ITalkClientListener listener)
                throws RemoteException {
            LOG.info("[" + mId + "] setListener()");
            mListener = listener;
        }
		
	}

}
