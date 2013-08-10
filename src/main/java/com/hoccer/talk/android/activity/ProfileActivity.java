package com.hoccer.talk.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.fragment.MessagingFragment;
import com.hoccer.talk.android.fragment.ProfileFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Activity wrapping a profile fragment
 */
public class ProfileActivity extends TalkActivity {

    ActionBar mActionBar;

    ProfileFragment mFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        enableUpNavigation();

        mActionBar = getSupportActionBar();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (ProfileFragment)fragmentManager.findFragmentById(R.id.activity_profile_fragment);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        Intent intent = getIntent();

        // handle show intent
        if(intent != null && intent.hasExtra("clientContactId")) {
            int contactId = intent.getIntExtra("clientContactId", -1);
            if(contactId == -1) {
                LOG.error("invalid contact id");
            } else {
                try {
                    TalkClientContact contact = getTalkClientDatabase().findClientContactById(contactId);
                    if(contact != null) {
                        showProfile(contact);
                    }
                } catch (SQLException e) {
                    LOG.error("sql error", e);
                }
            }
        }
    }

    public void showProfile(TalkClientContact contact) {
        LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        mActionBar.setTitle(contact.getName());
        mFragment.showProfile(contact);
    }

}
