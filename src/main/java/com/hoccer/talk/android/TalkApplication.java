package com.hoccer.talk.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.hoccer.xo.release.R;
import com.hoccer.talk.client.HttpClientWithKeystore;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TalkApplication extends Application {

    private final static String TAG = "HoccerXO";

    private static Logger LOG = null;

    /** root of user-visible storage root */
    private static File EXTERNAL_STORAGE = null;

    /** root of app-private storage root */
    private static File INTERNAL_STORAGE = null;

    /** global executor for client background activity */
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

    private static File getLogDirectory() {
        return EXTERNAL_STORAGE;
    }

    public static File getAttachmentDirectory() {
        return new File(EXTERNAL_STORAGE, TalkConfiguration.EXTERNAL_ATTACHMENTS);
    }

    public static File getEncryptedUploadDirectory() {
        return new File(INTERNAL_STORAGE, TalkConfiguration.INTERNAL_UPLOADS);
    }

    public static File getEncryptedDownloadDirectory() {
        return new File(INTERNAL_STORAGE, TalkConfiguration.INTERNAL_DOWNLOADS);
    }

    public static File getAvatarDirectory() {
        return new File(INTERNAL_STORAGE, TalkConfiguration.INTERNAL_AVATARS);
    }

    public static File getAvatarLocation(TalkClientDownload download) {
        if(download.getState() == TalkClientDownload.State.COMPLETE) {
            String dataFile = download.getDataFile();
            if(dataFile != null) {
                return new File(getAvatarDirectory(), dataFile);
            }
        }
        return null;
    }

    public static File getAvatarLocation(TalkClientUpload upload) {
        String dataFile = upload.getDataFile();
        if(dataFile != null) {
            return new File(dataFile);
        }
        return null;
    }

    public static File getAttachmentLocation(TalkClientDownload download) {
        if(download.getState() == TalkClientDownload.State.COMPLETE) {
            File attachmentDir = getAttachmentDirectory();
            ensureDirectory(attachmentDir);
            String dataFile = download.getDataFile();
            if(dataFile != null) {
                return new File(attachmentDir, dataFile);
            }
        }
        return null;
    }

    public static File getAttachmentLocation(TalkClientUpload upload) {
        String dataFile = upload.getDataFile();
        if(dataFile != null) {
            return new File(dataFile);
        }
        return null;
    }


	@Override
	public void onCreate() {
		super.onCreate();

        // initialize storage roots (do so early for log files)
        EXTERNAL_STORAGE = Environment.getExternalStorageDirectory();
        INTERNAL_STORAGE = this.getFilesDir();

        // initialize logging system
        initLogging();

        // configure ormlite to use log4j
        System.setProperty("com.j256.ormlite.logger.type", "LOG4J");

        // get logger for this class
        LOG = Logger.getLogger(TalkApplication.class);

        // announce sdk version
        LOG.info("running on sdk version " + Build.VERSION.SDK_INT);

        // install a default exception handler
        LOG.info("setting up default exception handler");
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

        // log storage roots
        LOG.info("internal storage at " + INTERNAL_STORAGE.toString());
        LOG.info("external storage at " + EXTERNAL_STORAGE.toString());

        // set up SSL keystore
        LOG.info("initializing ssl keystore");
        try {
            KeyStore ks = KeyStore.getInstance("BKS");
            InputStream in = this.getResources().openRawResource(R.raw.ssl_bks);
			try {
				ks.load(in, "password".toCharArray());
			} finally {
				in.close();
			}
            HttpClientWithKeystore.initializeSsl(ks);
        } catch (Exception e) {
            LOG.error("error initializing SSL keystore", e);
        }

        // configure image loader
        LOG.info("configuring image loader");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);

        // set up directories
        LOG.info("setting up directory structure");
        ensureDirectory(getAttachmentDirectory());
        ensureDirectory(getAvatarDirectory());
        ensureNomedia(getAvatarDirectory());
        ensureDirectory(getEncryptedUploadDirectory());
        ensureNomedia(getEncryptedUploadDirectory());
        ensureDirectory(getEncryptedDownloadDirectory());
        ensureNomedia(getEncryptedDownloadDirectory());
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
            File file = new File(getLogDirectory(), TalkConfiguration.LOG_FILE_NAME);
            mFileAppender = new RollingFileAppender(TalkConfiguration.LOG_FILE_LAYOUT, file.toString());
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

    private static void ensureDirectory(File directory) {
        if(!directory.exists()) {
            LOG.info("creating directory " + directory.toString());
            directory.mkdirs();
        }
    }

    private static void ensureNomedia(File directory) {
        if(directory.exists()) {
            File nomedia = new File(directory, ".nomedia");
            if(!nomedia.exists()) {
                LOG.info("creating nomedia marker " + nomedia.toString());
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {
                    LOG.error("error creating " + nomedia.toString(), e);
                }
            }
        }
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
            ensureDirectory(getLogDirectory());
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
