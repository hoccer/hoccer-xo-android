package com.hoccer.talk.android;

import com.hoccer.talk.android.logging.AndroidLogHandler;

import android.app.Application;

public class TalkApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// enable log forwarding
		AndroidLogHandler.engage();
	}

}
