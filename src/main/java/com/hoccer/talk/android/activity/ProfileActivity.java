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

import java.sql.SQLException;

public class ProfileActivity extends TalkActivity {

    ActionBar mActionBar;

    FragmentManager mFragmentManager;
    ProfileFragment mFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mFragmentManager = getSupportFragmentManager();
        mFragment = (ProfileFragment)mFragmentManager.findFragmentById(R.id.activity_profile_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("clientContactId")) {
            int contactId = intent.getIntExtra("clientContactId", -1);
            if(contactId == -1) {
                LOG.error("Invalid ccid");
                return;
            }
            try {
                TalkClientContact contact = getTalkClientDatabase().findClientContactById(contactId);
                if(contact != null) {
                    showProfile(contact);
                }
            } catch (SQLException e) {
                LOG.error("NO EXTRA", e);
            }

        } else {
            LOG.info("NO EXTRA");
        }
    }

    public void showProfile(TalkClientContact contact) {
        mActionBar.setTitle(contact.getName());
        mFragment.showProfile(contact);
    }

}
