package com.hoccer.xo.android.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.hoccer.xo.android.service.XoClientService;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logger.getLogger(SmsReceiver.class);

    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String EXTRA_SMS_SENDER       = "com.hoccer.talk.android.SMS_SENDER";
    public static final String EXTRA_SMS_BODY       = "com.hoccer.talk.android.SMS_BODY";
    public static final String EXTRA_SMS_URL_RECEIVED = "com.hoccer.talk.android.SMS_URL_RECEIVED";

    private static final String URL_PATTERN_SOURCE = ".*(hxo://[a-zA-Z0-9]*).*";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_PATTERN_SOURCE, Pattern.DOTALL);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ACTION_SMS_RECEIVED) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                boolean abort = false;
                // scan the message for things we are interested in
                Object[] pdus = (Object[])bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);
                    String body = message.getMessageBody();
                    String sender = message.getOriginatingAddress();
                    if(sender != null && body != null) {
                        // process the received message
                        abort |= handleMessage(context, sender, body);
                    }
                }
                // abort delivery of this message to other apps
                if(abort) {
                    this.abortBroadcast();
                }
            }
        }
    }

    /**
     * This gets called for every SMS received by the device
     *
     * It will filter out messages containing hxo:// tokens
     * and deliver them exclusively to the client service.
     *
     * @param context
     * @param sender
     * @param body
     * @return true if the message is for us
     */
    private boolean handleMessage(Context context, String sender, String body) {
        // do a simple string check first
        if(body.contains("hxo://")) {
            // regex-match the URL
            Matcher matcher = URL_PATTERN.matcher(body);
            if(matcher.matches() && matcher.groupCount() > 0) {
                String url = matcher.group(1);
                // log the received token
                LOG.info("received message containing url " + url);
                // send intent to the client service
                sendServiceIntent(context, sender, url, body);
                // this message is for us
                return true;
            }
        }
        // message is not for us
        return false;
    }

    /**
     * Send the client service an intent about the given message
     *
     * @param context
     * @param sender
     * @param url
     * @param body
     */
    private void sendServiceIntent(Context context, String sender, String url, String body) {
        Intent serviceIntent = new Intent(context, XoClientService.class);
        serviceIntent.putExtra(EXTRA_SMS_SENDER, sender);
        serviceIntent.putExtra(EXTRA_SMS_BODY, body);
        serviceIntent.putExtra(EXTRA_SMS_URL_RECEIVED, url);
        context.startService(serviceIntent);
    }

}
