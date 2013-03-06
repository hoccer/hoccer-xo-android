package com.hoccer.talk.android;

import com.hoccer.talk.android.logging.AndroidLogHandler;

import android.app.Application;
import com.hoccer.talk.logging.HoccerLoggers;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.logging.Logger;

public class TalkApplication extends Application {

    private static final Logger LOG =
            HoccerLoggers.getLogger(TalkApplication.class);

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        // enable log forwarding
        AndroidLogHandler.engage();
	}

    @Override
    public void onTerminate() {
        LOG.info("onTerminate()");
        super.onTerminate();
    }

}
