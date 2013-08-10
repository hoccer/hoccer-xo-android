package com.hoccer.talk.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.fragment.MessagingFragment;
import com.hoccer.talk.client.model.TalkClientContact;

import java.sql.SQLException;

public class MessagingActivity extends TalkActivity {

    ActionBar mActionBar;

    MessagingFragment mFragment;

    TalkClientContact mContact;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_messaging;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_messaging;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        // get action bar (for setting title)
        mActionBar = getSupportActionBar();

        // enable up navigation
        enableUpNavigation();

        // get our primary fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (MessagingFragment) fragmentManager.findFragmentById(R.id.activity_messaging_fragment);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        Intent intent = getIntent();

        // handle converse intent
        if(intent != null && intent.hasExtra("clientContactId")) {
            int contactId = intent.getIntExtra("clientContactId", -1);
            if(contactId == -1) {
                LOG.error("invalid contact id");
            } else {
                try {
                    TalkClientContact contact = getTalkClientDatabase().findClientContactById(contactId);
                    if(contact != null) {
                        converseWithContact(contact);
                    }
                } catch (SQLException e) {
                    LOG.error("sql error", e);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile_client:
            case R.id.menu_profile_group:
                if(mContact != null) {
                    showContactProfile(mContact);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void converseWithContact(TalkClientContact contact) {
        LOG.debug("converseWithContact(" + contact + ")");
        mContact = contact;
        mActionBar.setTitle(contact.getName());
        mFragment.converseWithContact(contact);
    }

}
