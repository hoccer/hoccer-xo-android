package com.hoccer.talk.android;

public class TalkConfiguration {

    /** GCM sender id for push notifications */
    public static final String GCM_SENDER_ID = "";

    /** Delay after which new activities send their first keepalive (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_DELAY    = 30;

    /** Interval at which activities send keepalives to the client service (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_INTERVAL = 60;

    /** Timeout after which the client service terminates automatically (seconds) */
    public static final int SERVICE_KEEPALIVE_TIMEOUT       = 600;

}
