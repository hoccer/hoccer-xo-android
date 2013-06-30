package com.hoccer.talk.android;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;

public class TalkConfiguration {

    /** If true, GCM registration will be performed forcibly on every connect */
    public static final boolean GCM_ALWAYS_REGISTER = false;

    /** If true, GCM registration should always be pushed to server */
    public static final boolean GCM_ALWAYS_UPDATE = false;

    /** GCM sender id for push notifications */
    public static final String GCM_SENDER_ID = "1894273085";

    /** GCM server registration expiration (seconds) */
    public static final long GCM_REGISTRATION_EXPIRATION = 24 * 3600;

    /** The layout for android logcat */
    public static final Layout LOG_LOGCAT_LAYOUT = new PatternLayout("[%t] %-5p %c - %m%n");

    /** The maximum number of log files to keep */
    public static final int LOG_FILE_COUNT = 10;

    /** The maximum size of each log file */
    public static final int LOG_FILE_SIZE = 512 * 1024;

    /** The layout for log files */
    public static final Layout LOG_FILE_LAYOUT = new PatternLayout("[%t] %-5p %c - %m%n");

    /** Delay after which new activities send their first keepalive (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_DELAY    = 60;

    /** Interval at which activities send keepalives to the client service (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_INTERVAL = 600;

    /** Timeout after which the client service terminates automatically (seconds) */
    public static final int SERVICE_KEEPALIVE_TIMEOUT       = 1800;

}
