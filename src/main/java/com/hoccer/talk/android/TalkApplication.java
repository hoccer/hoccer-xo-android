package com.hoccer.talk.android;

import android.os.Environment;
import android.util.Log;

import android.app.Application;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;

public class TalkApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

        Log.i("HoccerTalk", "Initializing logging");

        Logger rootLogger = Logger.getRootLogger();

        try {
            Layout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
            String file = Environment.getExternalStorageDirectory() + File.separator + "hoccer-talk.log";
            FileAppender fileAppender = new FileAppender(layout, file);
            rootLogger.setLevel(Level.INFO);
            rootLogger.addAppender(fileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("HoccerTalk", "Done initializing logging");
	}

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
