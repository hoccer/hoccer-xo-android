package com.hoccer.talk.android.push;

import android.content.Context;
import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * Broadcast receiver for GCM broadcasts
 *
 * This is a simple subclass providing the name of our
 * intent service to the GCM client implementation.
 *
 * NOTE this class can not be renamed because of upgrade
 *      issues with respect to GCM registration state
 *      on target devices
 */
public class TalkPushReceiver extends GCMBroadcastReceiver {

    @Override
    protected String getGCMIntentServiceClassName(Context context) {
        return TalkPushService.class.getCanonicalName();
    }

}
