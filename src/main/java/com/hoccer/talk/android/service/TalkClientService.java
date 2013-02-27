package com.hoccer.talk.android.service;

import java.util.logging.Logger;

import com.hoccer.talk.logging.HoccerLoggers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class TalkClientService extends Service {

	private static final Logger LOG =
		HoccerLoggers.getLogger(TalkClientService.class);  

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
        LOG.info("onBind(" + intent.toString() + ")");
		return new Connection();
	}
	
	public class Connection extends ITalkClientService.Stub {

		@Override
		public void keepAlive() throws RemoteException {
		}
		
	}

}
