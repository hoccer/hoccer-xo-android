package com.hoccer.talk.android.push;

import android.content.Context;
import android.content.Intent;
import com.google.android.gcm.GCMBaseIntentService;
import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.logging.HoccerLoggers;

import java.util.logging.Logger;

/**
 * GCM push notification service
 *
 * This service processes all our GCM events.
 *
 * Its lifecycle is automatically controlled by the GCM client implementation.
 *
 */
public class TalkPushService extends GCMBaseIntentService {

    private static final Logger LOG = HoccerLoggers.getLogger(TalkPushService.class);

    public TalkPushService() {
        super(TalkConfiguration.GCM_SENDER_ID);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        LOG.info("onMessage(" + intent.toString() + ")");
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        LOG.info("onDeletedMessages(" + total + ")");
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
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        LOG.info("onUnregistered(" + registrationId + ")");
    }

}
