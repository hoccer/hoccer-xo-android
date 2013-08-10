package com.hoccer.talk.android.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PairingActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pairing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        enableUpNavigation();
    }

}
