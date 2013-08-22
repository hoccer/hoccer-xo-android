package com.hoccer.talk.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.fragment.ProfileFragment;
import com.hoccer.talk.client.model.TalkClientContact;

import java.sql.SQLException;

/**
 * Activity wrapping a profile fragment
 */
public class ProfileActivity extends TalkActivity {

    ActionBar mActionBar;

    ProfileFragment mFragment;

    TalkClientContact mContact;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_profile;
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
                showProfile(refreshContact(contactId));
            }
        } else {
            if(mContact != null) {
                showProfile(refreshContact(mContact.getClientContactId()));
            }
        }
    }

    private TalkClientContact refreshContact(int contactId) {
        try {
            return getTalkClientDatabase().findClientContactById(contactId);
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
        return null;
    }

    public void showProfile(TalkClientContact contact) {
        LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        mContact = contact;
        if(mContact != null) {
            mActionBar.setTitle(contact.getName());
            mFragment.showProfile(contact);
            if(contact.isDeleted()) {
                finish();
            }
        }
    }

    @Override
    public void hackReturnedFromDialog() {
        super.hackReturnedFromDialog();
        if(mContact != null) {
            showProfile(refreshContact(mContact.getClientContactId()));
        }
    }

    @Override
    public void onContactRemoved(int contactId) throws RemoteException {
        if(mContact != null && mContact.getClientContactId() == contactId) {
            finish();
        }
    }
}
