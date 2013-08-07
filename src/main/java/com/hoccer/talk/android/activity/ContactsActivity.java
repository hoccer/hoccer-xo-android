package com.hoccer.talk.android.activity;

import com.actionbarsherlock.view.Menu;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;

public class ContactsActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contacts;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.info("onCreateOptionsMenu()");
        getSupportMenuInflater().inflate(R.menu.common, menu);
        getSupportMenuInflater().inflate(R.menu.fragment_contacts, menu);
        return true;
    }

}
