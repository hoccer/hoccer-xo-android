package com.hoccer.xo.android;

import android.app.Application;
import android.os.Build;
import android.os.Environment;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Application class
 *
 * This class handles the application lifecycle,
 * setting up various things as it does so.
 */
public class XoApplication extends Application {

    /** logger for this class (initialized in onCreate) */
    private static Logger LOG = null;

    /** global executor for client background activity (lazy init) */
    private static ScheduledExecutorService EXECUTOR = null;

    /** root of user-visible storage (initialized in onCreate) */
    private static File EXTERNAL_STORAGE = null;
    /** root of app-private storage (initialized in onCreate) */
    private static File INTERNAL_STORAGE = null;

    /** @return common executor for background tasks */
    public static ScheduledExecutorService getExecutor() {
        if(EXECUTOR == null) {
            EXECUTOR = Executors.newScheduledThreadPool(2);
        }
        return EXECUTOR;
    }

    /**
     * @return user-visible storage directory
     */
    public static File getExternalStorage() {
        return EXTERNAL_STORAGE;
    }

    /**
     * @return internal storage directory
     */
    public static File getInternalStorage() {
        return INTERNAL_STORAGE;
    }

    private Thread.UncaughtExceptionHandler mPreviousHandler;

    public static File getLogDirectory() {
        return EXTERNAL_STORAGE;
    }

    public static File getAttachmentDirectory() {
        return new File(EXTERNAL_STORAGE, XoConfiguration.EXTERNAL_ATTACHMENTS);
    }

    public static File getEncryptedUploadDirectory() {
        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_UPLOADS);
    }

    public static File getEncryptedDownloadDirectory() {
        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_DOWNLOADS);
    }

    public static File getAvatarDirectory() {
        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_AVATARS);
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
        XoLogging.initialize(this);

        // configure ormlite to use log4j
        System.setProperty("com.j256.ormlite.logger.type", "LOG4J");

        // get logger for this class
        LOG = Logger.getLogger(XoApplication.class);

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

        // configure ssl
        XoSsl.initialize(this);

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

        XoLogging.shutdown();

        if(EXECUTOR != null) {
            EXECUTOR.shutdownNow();
            EXECUTOR = null;
        }
    }

    public static void ensureDirectory(File directory) {
        if(!directory.exists()) {
            LOG.info("creating directory " + directory.toString());
            directory.mkdirs();
        }
    }

    public static void ensureNomedia(File directory) {
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

}
