package com.hoccer.talk.android;

import android.os.Environment;
import android.util.Log;

import android.app.Application;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.io.File;
import java.io.IOException;

public class TalkApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

        Log.i("HoccerTalk", "Initializing logging");

        Logger rootLogger = Logger.getRootLogger();

        try {
            String file = Environment.getExternalStorageDirectory() + File.separator + "myapp.log";
            FileAppender fileAppender = new FileAppender(new SimpleLayout(), file);
            rootLogger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.i("HoccerTalk", "Done initializing logging");

        // enable log forwarding
        //AndroidLogHandler.engage();
	}

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
