package com.hoccer.talk.android.push;

import android.content.Context;
import android.content.Intent;
import com.google.android.gcm.GCMBaseIntentService;
import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.android.service.TalkClientService;
import org.apache.log4j.Logger;

/**
 * GCM push notification service
 *
 * This service processes all our GCM events.
 * Its lifecycle is intent-driven.
 *
 * For most events this service receives it will generate an Intent
 * to call the client service to perform appropriate actions.
 *
 */
public class TalkPushService extends GCMBaseIntentService {

    public static final String EXTRA_GCM_REGISTERED = "com.hoccer.xo.GCM_REGISTERED";
    public static final String EXTRA_GCM_UNREGISTERED = "com.hoccer.xo.GCM_UNREGISTERED";
    public static final String EXTRA_WAKE_CLIENT = "com.hoccer.xo.WAKE_CLIENT";

    private static final Logger LOG = Logger.getLogger(TalkPushService.class);

    public TalkPushService() {
        super(TalkConfiguration.GCM_SENDER_ID);
    }

    private void sendServiceIntent(String extra, String extraValue) {
        Intent serviceIntent = new Intent(getApplicationContext(), TalkClientService.class);
        serviceIntent.putExtra(extra, extraValue);
        startService(serviceIntent);
    }

    private void wakeClient() {
        sendServiceIntent(EXTRA_WAKE_CLIENT, "dummy");
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        LOG.info("onMessage(" + intent.toString() + ")");
        // simply wake the client
        wakeClient();
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        LOG.info("onDeletedMessages(" + total + ")");
        // wake the client, no matter what the message might have been
        wakeClient();
    }

    @Override
    protected void onError(Context context, String errorId) {
        LOG.info("onError(" + errorId + ")");
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        LOG.info("onRecoverableError(" + errorId + ")");
        return super.onRecoverableError(context, errorId);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        LOG.info("onRegistered(" + registrationId + ")");
        sendServiceIntent(EXTRA_GCM_REGISTERED, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        LOG.info("onUnregistered(" + registrationId + ")");
        sendServiceIntent(EXTRA_GCM_UNREGISTERED, registrationId);
    }

}
