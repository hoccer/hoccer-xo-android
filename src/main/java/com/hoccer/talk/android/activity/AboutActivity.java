package com.hoccer.talk.android.activity;

import android.os.Bundle;
import com.hoccer.xo.release.R;
import com.hoccer.talk.android.TalkActivity;

public class AboutActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        enableUpNavigation();
    }

}
