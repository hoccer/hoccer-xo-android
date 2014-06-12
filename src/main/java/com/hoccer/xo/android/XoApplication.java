package com.hoccer.xo.android;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hoccer.talk.client.IXoClientHost;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.xo.android.error.EnvironmentUpdaterException;
import com.hoccer.xo.android.nearby.EnvironmentUpdater;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Application class
 *
 * This class handles the application lifecycle and is responsible
 * for such things as initializing the logger and setting up the
 * XO client itself. All global initialization should go here.
 *
 */
public class XoApplication extends Application implements Thread.UncaughtExceptionHandler {

    /** logger for this class (initialized in onCreate) */
    private static Logger LOG = null;

    /** global executor for client background activity (initialized in onCreate) */
    private static ScheduledExecutorService EXECUTOR = null;
    /** global executor for incoming connections */
    private static ScheduledExecutorService INCOMING_EXECUTOR = null;
    /** global xo host */
    private static IXoClientHost CLIENT_HOST = null;
    /** global xo client (initialized in onCreate) */
    private static XoClient CLIENT = null;
    /** global xo sound pool for system sounds (initialized in onCreate) */
    private static XoSoundPool SOUND_POOL = null;

    public static EnvironmentUpdater ENVIRONMENT_UPDATER = null;

    /** root of user-visible storage (initialized in onCreate) */
    private static File EXTERNAL_STORAGE = null;
    /** root of app-private storage (initialized in onCreate) */
    private static File INTERNAL_STORAGE = null;
    /** uncaught exception handler for the client and us */
    private static Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = null;
    private static DisplayImageOptions CONTENT_IMAGE_OPTIONS = null;
    /** @return common executor for background tasks */
    public static ScheduledExecutorService getExecutor() {
        return EXECUTOR;
    }
    /** @return the xo client */
    public static XoClient getXoClient() {
        return CLIENT;
    }
    /** @return the xo sound pool */
    public static XoSoundPool getXoSoundPool() {
        return SOUND_POOL;
    }

    public static EnvironmentUpdater getEnvironmentUpdater() {
        return ENVIRONMENT_UPDATER;
    }

    /**
     * @return user-visible storage directory
     */
    public static File getExternalStorage() {
        return EXTERNAL_STORAGE;
    }

    public static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return UNCAUGHT_EXCEPTION_HANDLER;
    }

    public static DisplayImageOptions getContentImageOptions() {
        return CONTENT_IMAGE_OPTIONS;
    }

    /**
     * @return internal storage directory
     */
    public static File getInternalStorage() {
        return INTERNAL_STORAGE;
    }

    private Thread.UncaughtExceptionHandler mPreviousHandler;

    public static File getAttachmentDirectory() {
        return new File(EXTERNAL_STORAGE, XoConfiguration.EXTERNAL_ATTACHMENTS);
    }

//    public static File getEncryptedUploadDirectory() {
//        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_UPLOADS);
//    }

    public static File getEncryptedDownloadDirectory() {
        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_DOWNLOADS);
    }

    public static File getGeneratedDirectory() {
        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_GENERATED);
    }

    public static File getAvatarDirectory() {
        return new File(EXTERNAL_STORAGE, XoConfiguration.INTERNAL_AVATARS);
    }


    public static File getThumbnailDirectory() {
        return new File(INTERNAL_STORAGE, XoConfiguration.INTERNAL_THUMBNAILS);
    }

    public static File getAvatarLocation(TalkClientDownload download) {
        if(download.getState() == TalkClientDownload.State.COMPLETE) {
            File avatarDir = getAvatarDirectory();
            ensureDirectory(avatarDir);
            String dataFile = download.getDataFile();
            if(dataFile != null) {
                return new File(avatarDir, dataFile);
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

        // currently we use our own instance here
        UNCAUGHT_EXCEPTION_HANDLER = this;

        // initialize storage roots (do so early for log files)
        EXTERNAL_STORAGE = Environment.getExternalStorageDirectory();
        INTERNAL_STORAGE = this.getFilesDir();

        // initialize logging system
        XoLogging.initialize(this);
        XoConfiguration.initialize(this);

        // configure ormlite to use log4j
        System.setProperty("com.j256.ormlite.logger.type", "LOG4J");

        // get logger for this class
        LOG = Logger.getLogger(XoApplication.class);

        // announce sdk version
        LOG.info("system sdk " + Build.VERSION.SDK_INT);
        LOG.info("system release " + Build.VERSION.RELEASE);
        LOG.info("system codename " + Build.VERSION.CODENAME);
        LOG.info("system revision " + Build.VERSION.INCREMENTAL);
        LOG.info("system brand " + Build.BRAND);
        LOG.info("system model " + Build.MODEL);
        LOG.info("system manufacturer " + Build.MANUFACTURER);
        LOG.info("system device " + Build.DEVICE);
        LOG.info("system product " + Build.PRODUCT);
        LOG.info("system type " + Build.TYPE);

        // install a default exception handler
        LOG.info("setting up default exception handler");
        mPreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        // log storage roots
        LOG.info("internal storage at " + INTERNAL_STORAGE.toString());
        LOG.info("external storage at " + EXTERNAL_STORAGE.toString());

        // initialize version information
        XoVersion.initialize(this);
        LOG.info("application build time " + XoVersion.getBuildTime());
        LOG.info("application branch " + XoVersion.getBranch());
        LOG.info("application commit " + XoVersion.getCommitId());
        LOG.info("application describe " + XoVersion.getCommitDescribe());

        // configure ssl
        XoSsl.initialize(this);

        // configure image loader
        LOG.info("configuring image loader");
        CONTENT_IMAGE_OPTIONS = new DisplayImageOptions.Builder()
                .cacheOnDisc(false)
                .cacheInMemory(false)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(2)
                .build();
        ImageLoader.getInstance().init(config);

        // set up directories
        LOG.info("setting up directory structure");
        ensureDirectory(getAttachmentDirectory());
        ensureDirectory(getAvatarDirectory());
        ensureDirectory(getGeneratedDirectory());
        ensureNoMedia(getGeneratedDirectory());
        ensureDirectory(getThumbnailDirectory());
//        ensureDirectory(getEncryptedUploadDirectory());
//        ensureNoMedia(getEncryptedUploadDirectory());
        ensureDirectory(getEncryptedDownloadDirectory());
        ensureNoMedia(getEncryptedDownloadDirectory());

        // create executor
        LOG.info("creating background executor");
        ThreadFactoryBuilder tfb = new ThreadFactoryBuilder();
        tfb.setNameFormat("client-%d");
        tfb.setUncaughtExceptionHandler(this);
        EXECUTOR = Executors.newScheduledThreadPool(
                        XoConfiguration.CLIENT_THREADS,
                        tfb.build());
        ThreadFactoryBuilder tfb2 = new ThreadFactoryBuilder();
        tfb2.setNameFormat("receiving client-%d");
        tfb2.setUncaughtExceptionHandler(this);
        INCOMING_EXECUTOR = Executors.newScheduledThreadPool(XoConfiguration.CLIENT_THREADS, tfb2.build());

        // create client instance
        LOG.info("creating client");
        CLIENT_HOST = new XoAndroidClientHost(this);
        XoClient client = new XoAndroidClient(CLIENT_HOST);
        client.setAvatarDirectory(getAvatarDirectory().toString());
        client.setAttachmentDirectory(getAttachmentDirectory().toString());
//        client.setEncryptedUploadDirectory(getEncryptedUploadDirectory().toString()); //TODO: to be deleted encryption happens on the fly now
        client.setEncryptedDownloadDirectory(getEncryptedDownloadDirectory().toString());
        CLIENT = client;

        // create sound pool instance
        SOUND_POOL = new XoSoundPool(this);

        ENVIRONMENT_UPDATER = new EnvironmentUpdater(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        LOG.info("deactivating client");
        if(CLIENT != null) {
            CLIENT.deactivateNow();
            CLIENT = null;
        }

        LOG.info("removing uncaught exception handler");
        if(mPreviousHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mPreviousHandler);
            mPreviousHandler = null;
        }

        LOG.info("shutting down executor");
        if(EXECUTOR != null) {
            EXECUTOR.shutdownNow();
            EXECUTOR = null;
        }

        LOG.info("shutting down logging");
        XoLogging.shutdown();
        LOG.info("shutting down configuration");
        XoConfiguration.shutdown();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LOG.error("uncaught exception on thread " + thread.getName(), ex);
        if(mPreviousHandler != null) {
            mPreviousHandler.uncaughtException(thread, ex);
        }
    }

    public static void ensureDirectory(File directory) {
        if(!directory.exists()) {
            LOG.info("creating directory " + directory.toString());
            if (!directory.mkdirs()) {
                LOG.info("Error creating directory " + directory.toString());
            }
        }
    }

    public static void ensureNoMedia(File directory) {
        if(directory.exists()) {
            File noMedia = new File(directory, ".nomedia");
            if(!noMedia.exists()) {
                LOG.info("creating noMedia marker " + noMedia.toString());
                try {
                    if (!noMedia.createNewFile()) {
                        LOG.info("Error creating directory " + noMedia.toString());
                    }
                } catch (IOException e) {
                    LOG.error("error creating " + noMedia.toString(), e);
                }
            }
        }
    }

    public static ScheduledExecutorService getIncomingExecutor() {
        return INCOMING_EXECUTOR;
    }

    public static void reinitializeXoClient() {
        if(CLIENT != null) {
            CLIENT.initialize(CLIENT_HOST);
        }
    }

    /**
     * Set to true if a nearby session is currently running.
     */
    private static boolean hasCurrentRunningNearbySession;

    /**
     * Starts a nearby session if not yet started.
     * Sets hasCurrentRunningNearbySession = true.
     */
    public static void startNearbySession() {
        if (!ENVIRONMENT_UPDATER.isEnabled()) {
            try {
                ENVIRONMENT_UPDATER.startEnvironmentTracking();
                hasCurrentRunningNearbySession = true;
            } catch (EnvironmentUpdaterException e) {
                LOG.error("Error when starting EnvironmentUpdater: ", e);
            }
        }
    }

    /**
     * Stops current nearby session if running.
     * Sets hasCurrentRunningNearbySession = true.
     */
    public static void suspendNearbySession() {
        if (ENVIRONMENT_UPDATER.isEnabled()) {
            hasCurrentRunningNearbySession = true;
            ENVIRONMENT_UPDATER.stopEnvironmentTracking();
        }
    }

    /**
     * Stops current nearby session if running.
     * Sets hasCurrentRunningNearbySession = false.
     */
    public static void stopNearbySession() {
        if (hasCurrentRunningNearbySession) {
            suspendNearbySession();
            hasCurrentRunningNearbySession = false;
        }
    }

    public static void enterBackgroundMode() {
        // set presence to inactive
        getXoClient().setClientConnectionStatus(TalkPresence.CONN_STATUS_BACKGROUND);

        // suspend nearby environment
        suspendNearbySession();

        LOG.info("Entered background mode");
    }

    public static void enterForegroundMode() {
        // set presence to active
        getXoClient().setClientConnectionStatus(TalkPresence.CONN_STATUS_ONLINE);

        // wake up suspended nearby session
        if (hasCurrentRunningNearbySession) {
            startNearbySession();
        }

        LOG.info("Entered foreground mode");
    }

    public static void enterBackgroundActiveMode() {
        LOG.info("Entered background active mode");
    }
}
