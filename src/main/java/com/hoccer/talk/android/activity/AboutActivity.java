package com.hoccer.talk.android.activity;

import android.os.Bundle;
import com.hoccer.talk.android.XoActivity;
import com.hoccer.xo.release.R;

public class AboutActivity extends XoActivity {

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
