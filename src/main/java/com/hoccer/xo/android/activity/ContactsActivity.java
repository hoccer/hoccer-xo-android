package com.hoccer.xo.android.activity;

import android.content.Intent;
import android.os.Bundle;
import com.hoccer.xo.android.base.XoActivity;
import com.whitelabel.gw.release.R;

public class ContactsActivity extends XoActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contacts;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_contacts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getXoClient().isRegistered()) {
            LOG.info("not registered - redirecting to profile activity");
            Intent intent = new Intent(this, SingleProfileActivity.class);
            intent.putExtra(SingleProfileActivity.EXTRA_CLIENT_CREATE_SELF, true);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!getXoClient().isRegistered()) {
            LOG.info("not registered - finishing");
            finish();
        }
    }

}
