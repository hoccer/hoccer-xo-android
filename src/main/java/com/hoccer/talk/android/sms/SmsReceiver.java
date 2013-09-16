package com.hoccer.talk.android.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.hoccer.talk.android.service.TalkClientService;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String EXTRA_SMS_URL_RECEIVED = "com.hoccer.talk.android.SMS_REGISTERED";

    private static final String URL_PATTERN = ".*(hxo://[a-zA-Z0-9/]).*";
    private static final Pattern URL_PAT = Pattern.compile(URL_PATTERN);

    private static final Logger LOG = Logger.getLogger(SmsReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ACTION_SMS_RECEIVED) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);
                    String body = message.getMessageBody();
                    if(body != null) {
                        LOG.info("received sms: \"" + body + "\"");
                        handleMessage(context, body);
                    }
                }
            }
        }
    }

    private void handleMessage(Context context, String body) {
        if(body.contains("hxo://")) {
            Matcher matcher = URL_PAT.matcher(body);
            if(matcher.matches()) {
                if(matcher.groupCount() >= 1) {
                    String url = matcher.group(0);
                    sendServiceIntent(context, EXTRA_SMS_URL_RECEIVED, url);
                }
            }
        }
    }

    private void sendServiceIntent(Context context, String extra, String extraValue) {
        Intent serviceIntent = new Intent(context, TalkClientService.class);
        serviceIntent.putExtra(extra, extraValue);
        context.startService(serviceIntent);
    }

}
