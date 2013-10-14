package com.hoccer.xo.android.activity;

import com.hoccer.xo.android.XoActivity;
import com.hoccer.xo.release.R;

public class ContactsActivity extends XoActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contacts;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_contacts;
    }

}
