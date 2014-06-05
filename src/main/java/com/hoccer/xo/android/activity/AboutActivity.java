package com.hoccer.xo.android.activity;

import android.os.Bundle;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.test.R;

public class AboutActivity extends XoActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        enableUpNavigation();
    }

}
