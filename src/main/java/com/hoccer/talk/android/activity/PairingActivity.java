package com.hoccer.talk.android.activity;

import android.os.Bundle;
import com.hoccer.xo.R;
import com.hoccer.talk.android.TalkActivity;

public class PairingActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pairing;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_pairing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        enableUpNavigation();
    }

}
