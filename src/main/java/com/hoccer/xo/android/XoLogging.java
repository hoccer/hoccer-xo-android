package com.hoccer.xo.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;

/**
 * Static logging routines
 *
 * This class initializes and terminates our logging system.
 */
public class XoLogging {

    private final static String TAG = XoConfiguration.LOG_LOGCAT_TAG;

    /** SharedPreferences to listen on */
    private static SharedPreferences sPreferences;
    /** Listener watching preferences */
    private static SharedPreferences.OnSharedPreferenceChangeListener sPreferencesListener;

    /** The root logger */
    private static Logger sRootLogger;

    /** Log file appender */
    private static RollingFileAppender sFileAppender;
    /** Logcat appender */
    private static LogcatAppender      sLogcatAppender;

    /** @return directory for log files */
    private static File getLogDirectory() {
        return XoApplication.getExternalStorage();
    }

    /**
     * Initialize the logging system
     * @param application for context
     */
    public static void initialize(XoApplication application) {
        Log.i(TAG, "[logging] initializing logging");

        // get the root logger for configuration
        sRootLogger = Logger.getRootLogger();

        // create logcat appender
        sLogcatAppender = new LogcatAppender(XoConfiguration.LOG_LOGCAT_LAYOUT);

        // create file appender
        try {
            File file = new File(getLogDirectory(), XoConfiguration.LOG_FILE_NAME);
            sFileAppender = new RollingFileAppender(XoConfiguration.LOG_FILE_LAYOUT, file.toString());
            sFileAppender.setMaximumFileSize(XoConfiguration.LOG_FILE_SIZE);
            sFileAppender.setMaxBackupIndex(XoConfiguration.LOG_FILE_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // attach preference listener
        sPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
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
        sPreferences.registerOnSharedPreferenceChangeListener(sPreferencesListener);

        // apply initial configuration
        configureLogLevel();
        configureLogLogcat();
        configureLogSd();
    }

    /**
     * Shut down the logging system
     */
    public static final void shutdown() {
        if(sPreferencesListener != null) {
            sPreferences.unregisterOnSharedPreferenceChangeListener(sPreferencesListener);
            sPreferencesListener = null;
        }
    }

    private static void configureLogLevel() {
        String levelString = sPreferences.getString("preference_log_level", "INFO");
        Log.i(TAG, "[logging] setting log level to " + levelString);
        Level level = Level.toLevel(levelString);
        sRootLogger.setLevel(level);
    }

    private static void configureLogSd() {
        boolean enabled = sPreferences.getBoolean("preference_log_sd", false);
        Log.i(TAG, "[logging] " + (enabled ? "enabling" : "disabling") + " logging to SD card");
        if(enabled) {
            XoApplication.ensureDirectory(getLogDirectory());
            sRootLogger.addAppender(sFileAppender);
        } else {
            sRootLogger.removeAppender(sFileAppender);
        }
    }

    private static void configureLogLogcat() {
        boolean enabled = sPreferences.getBoolean("preference_log_logcat", true);
        Log.i(TAG, "[logging] " + (enabled ? "enabling" : "disabling") + " logging to logcat");
        if(enabled) {
            sRootLogger.addAppender(sLogcatAppender);
        } else {
            sRootLogger.removeAppender(sLogcatAppender);
        }
    }

    private static class LogcatAppender extends AppenderSkeleton {

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
