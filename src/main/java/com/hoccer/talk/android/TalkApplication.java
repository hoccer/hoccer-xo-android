package com.hoccer.talk.android;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import android.app.Application;
import com.j256.ormlite.logger.LoggerFactory;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TalkApplication extends Application {

    private final static String TAG = "HoccerTalk";

    private static Logger LOG = null;

    private static ScheduledExecutorService EXECUTOR = null;

    private SharedPreferences mPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesChangedListener;

    private Logger              mRootLogger;
    private RollingFileAppender mFileAppender;
    private LogcatAppender      mLogcatAppender;

    private Thread.UncaughtExceptionHandler mPreviousHandler;

    public static ScheduledExecutorService getExecutor() {
        if(EXECUTOR == null) {
            EXECUTOR = Executors.newScheduledThreadPool(2);
        }
        return EXECUTOR;
    }

	@Override
	public void onCreate() {
		super.onCreate();

        // initialize logging system
        initLogging();

        // configure ormlite to use log4j
        System.setProperty("com.j256.ormlite.logger.type", "LOG4J");

        // get logger for this class
        LOG = Logger.getLogger(TalkApplication.class);

        // install a default exception handler
        mPreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                LOG.error("uncaught exception", ex);
                if(mPreviousHandler != null) {
                    mPreviousHandler.uncaughtException(thread, ex);
                }
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if(mPreviousHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mPreviousHandler);
            mPreviousHandler = null;
        }

        if(mPreferencesChangedListener != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(mPreferencesChangedListener);
            mPreferencesChangedListener = null;
        }

        if(EXECUTOR != null) {
            EXECUTOR.shutdownNow();
            EXECUTOR = null;
        }
    }

    private void initLogging() {
        Log.i(TAG, "[logging] initializing logging");

        // get the root logger for configuration
        mRootLogger = Logger.getRootLogger();

        // create logcat appender
        mLogcatAppender = new LogcatAppender(TalkConfiguration.LOG_LOGCAT_LAYOUT);

        // create file appender
        try {
            String file = Environment.getExternalStorageDirectory() + File.separator + "hoccer-talk.log";
            mFileAppender = new RollingFileAppender(TalkConfiguration.LOG_FILE_LAYOUT, file);
            mFileAppender.setMaximumFileSize(TalkConfiguration.LOG_FILE_SIZE);
            mFileAppender.setMaxBackupIndex(TalkConfiguration.LOG_FILE_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // attach preference listener
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferencesChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("preference_log_logcat")) {
                    configureLogLogcat();
                }
                if(key.equals("preference_log_sd")) {
                    configureLogSd();
                }
                if(key.equals("preference_log_level")) {
                    configureLogLevel();
                }
            }
        };
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferencesChangedListener);

        // apply initial configuration
        configureLogLevel();
        configureLogLogcat();
        configureLogSd();
    }

    private void configureLogLevel() {
        String levelString = mPreferences.getString("preference_log_level", "INFO");
        Log.i(TAG, "[logging] setting log level to " + levelString);
        Level level = Level.toLevel(levelString);
        mRootLogger.setLevel(level);
    }

    private void configureLogSd() {
        boolean enabled = mPreferences.getBoolean("preference_log_sd", false);
        Log.i(TAG, "[logging] " + (enabled ? "enabling" : "disabling") + " logging to SD card");
        if(enabled) {
            mRootLogger.addAppender(mFileAppender);
        } else {
            mRootLogger.removeAppender(mFileAppender);
        }
    }

    private void configureLogLogcat() {
        boolean enabled = mPreferences.getBoolean("preference_log_logcat", true);
        Log.i(TAG, "[logging] " + (enabled ? "enabling" : "disabling") + " logging to logcat");
        if(enabled) {
            mRootLogger.addAppender(mLogcatAppender);
        } else {
            mRootLogger.removeAppender(mLogcatAppender);
        }
    }

    private class LogcatAppender extends AppenderSkeleton {

        public LogcatAppender(Layout layout) {
            super();
            setLayout(layout);
        }

        @Override
        protected void append(LoggingEvent event) {
            String message = getLayout().format(event);
            int level = event.getLevel().toInt();
            if(level == Level.WARN_INT) {
                if(event.getThrowableInformation() != null) {
                    Log.w(TAG, message, event.getThrowableInformation().getThrowable());
                } else {
                    Log.w(TAG, message);
                }
            } else if(level == Level.ERROR_INT) {
                if(event.getThrowableInformation() != null) {
                    Log.e(TAG, message, event.getThrowableInformation().getThrowable());
                } else {
                    Log.e(TAG, message);
                }
            } else if(level == Level.FATAL_INT) {
                if(event.getThrowableInformation() != null) {
                    Log.wtf(TAG, message, event.getThrowableInformation().getThrowable());
                } else {
                    Log.wtf(TAG, message);
                }
            } else {
                if(event.getThrowableInformation() != null) {
                    Log.i(TAG, message, event.getThrowableInformation().getThrowable());
                } else {
                    Log.i(TAG, message);
                }
            }
        }

        @Override
        public void close() {
            Log.v(TAG, "[logging] logcat appender closed");
        }

        @Override
        public boolean requiresLayout() {
            return true;
        }
    }

}
