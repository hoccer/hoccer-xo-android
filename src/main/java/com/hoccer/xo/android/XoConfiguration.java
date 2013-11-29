package com.hoccer.xo.android;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;

/**
 * Static client configuration
 *
 * This class collects various android-specific settings for the XO client.
 */
public class XoConfiguration {

    /* Directories in external storage */
    public static final String EXTERNAL_ATTACHMENTS = "Hoccer XO";

    /* Directories in internal storage */
    public static final String INTERNAL_AVATARS = "avatars";
    public static final String INTERNAL_DOWNLOADS = "downloads";
    public static final String INTERNAL_UPLOADS = "uploads";

    /** Background executor thread count */
    public static final int CLIENT_THREADS = 4;

    /** Notification alarm back-off (msecs) */
    public static final long NOTIFICATION_ALARM_BACKOFF = 5000;
    /** Notification cancellation back-off (msecs) */
    public static final long NOTIFICATION_CANCEL_BACKOFF = 2000;

    /** If true, GCM registration will be performed forcibly on every connect */
    public static final boolean GCM_ALWAYS_REGISTER = true;
    /** If true, GCM registration should always be pushed to server */
    public static final boolean GCM_ALWAYS_UPDATE = true;
    /** GCM sender id for push notifications */
    public static final String GCM_SENDER_ID = "1894273085";
    /** GCM server registration expiration (seconds) */
    public static final long GCM_REGISTRATION_EXPIRATION = 24 * 3600;

    /** Log tag to use in logcat */
    public static final String LOG_LOGCAT_TAG = "HoccerXO";
    /** The layout for android logcat */
    public static final Layout LOG_LOGCAT_LAYOUT = new PatternLayout("[%t] %-5p %c - %m%n");
    /** Base name of log files */
    public static final String LOG_FILE_NAME = "hoccer-xo.log";
    /** The maximum number of log files to keep */
    public static final int LOG_FILE_COUNT = 10;
    /** The maximum size of each log file */
    public static final int LOG_FILE_SIZE = 512 * 1024;
    /** The layout for log files */
    public static final Layout LOG_FILE_LAYOUT = new PatternLayout("[%t] %-5p %c - %m%n");

    /** Whether to reconnect explicitly on connection changes */
    public static final boolean CONNECTIVITY_RECONNECT_ON_CHANGE = true;

    /** Delay after which new activities send their first keepalive (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_DELAY    = 60;
    /** Interval at which activities send keepalives to the client service (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_INTERVAL = 600;
    /** Timeout after which the client service terminates automatically (seconds) */
    public static final int SERVICE_KEEPALIVE_TIMEOUT       = 1800;

}
