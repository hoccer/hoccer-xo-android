package com.hoccer.talk.android.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.hoccer.talk.android.service.XoClientService;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TalkSmsReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logger.getLogger(TalkSmsReceiver.class);

    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String EXTRA_SMS_SENDER       = "com.hoccer.talk.android.SMS_SENDER";
    public static final String EXTRA_SMS_URL_RECEIVED = "com.hoccer.talk.android.SMS_URL_RECEIVED";

    private static final String URL_PATTERN = ".*(hxo://[a-zA-Z0-9]*).*";
    private static final Pattern URL_PAT = Pattern.compile(URL_PATTERN, Pattern.DOTALL);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ACTION_SMS_RECEIVED) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);
                    String body = message.getMessageBody();
                    String sender = message.getOriginatingAddress();
                    if(sender != null && body != null) {
                        LOG.trace("received sms \"" + body + "\" from " + sender);
                        handleMessage(context, sender, body);
                    }
                }
            }
        }
    }

    private void handleMessage(Context context, String sender, String body) {
        if(body.contains("hxo://")) {
            LOG.debug("message contains an hxo url");
            Matcher matcher = URL_PAT.matcher(body);
            if(matcher.matches() && matcher.groupCount() > 0) {
                String url = matcher.group(1);
                LOG.info("received message containing url " + url);
                sendServiceIntent(context, sender, url);
            } else {
                LOG.trace("pattern did not match");
            }
        } else {
            LOG.trace("message does not contain an hxo url");
        }
    }

    private void sendServiceIntent(Context context, String sender, String url) {
        Intent serviceIntent = new Intent(context, XoClientService.class);
        serviceIntent.putExtra(EXTRA_SMS_SENDER, sender);
        serviceIntent.putExtra(EXTRA_SMS_URL_RECEIVED, url);
        context.startService(serviceIntent);
    }

}
