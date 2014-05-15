package com.hoccer.xo.android.activity;

import android.os.Bundle;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.PairingFragment;
import com.whitelabel.gw.release.R;

public class PairingActivity extends XoActivity {

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

    @Override
    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
        super.hackReturnedFromDialog();
        PairingFragment fragment = (PairingFragment)getSupportFragmentManager()
                .findFragmentById(R.id.activity_pairing_fragment);
        fragment.requestNewToken();
    }

}
