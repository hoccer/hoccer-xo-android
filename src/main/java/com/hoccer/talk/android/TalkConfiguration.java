package com.hoccer.talk.android;

public class TalkConfiguration {

    /** If true, GCM registration will be performed forcibly on every connect */
    public static final boolean GCM_ALWAYS_REGISTER = false;

    /** If true, GCM registration should always be pushed to server */
    public static final boolean GCM_ALWAYS_UPDATE = false;

    /** GCM sender id for push notifications */
    public static final String GCM_SENDER_ID = "1894273085";

    /** GCM server registration expiration (seconds) */
    public static final long GCM_REGISTRATION_EXPIRATION = 24 * 3600;

    /** Delay after which new activities send their first keepalive (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_DELAY    = 60;

    /** Interval at which activities send keepalives to the client service (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_INTERVAL = 300;

    /** Timeout after which the client service terminates automatically (seconds) */
    public static final int SERVICE_KEEPALIVE_TIMEOUT       = 600;

}
