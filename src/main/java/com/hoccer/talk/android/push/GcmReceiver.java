package com.hoccer.talk.android.push;

import android.content.Context;
import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * Broadcast receiver for GCM broadcasts
 *
 * This is a simple subclass providing the name of our
 * intent service to the GCM client implementation.
 *
 */
public class GcmReceiver extends GCMBroadcastReceiver {

    @Override
    protected String getGCMIntentServiceClassName(Context context) {
        return GcmService.class.getCanonicalName();
    }

}
