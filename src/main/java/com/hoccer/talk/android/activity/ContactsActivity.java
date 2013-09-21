package com.hoccer.talk.android.activity;

import com.hoccer.xo.release.R;
import com.hoccer.talk.android.TalkActivity;

public class ContactsActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contacts;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_contacts;
    }

}
