package com.hoccer.xo.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.XoActivity;
import com.hoccer.xo.android.fragment.CompositionFragment;
import com.hoccer.xo.android.fragment.MessagingFragment;
import com.hoccer.xo.release.R;

import java.sql.SQLException;

public class MessagingActivity extends XoActivity implements IXoContactListener {

    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    ActionBar mActionBar;

    MessagingFragment mMessagingFragment;
    CompositionFragment mCompositionFragment;

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
        mMessagingFragment = (MessagingFragment) fragmentManager.findFragmentById(R.id.activity_messaging_fragment);
        mCompositionFragment = (CompositionFragment) fragmentManager.findFragmentById(R.id.activity_messaging_composer);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        Intent intent = getIntent();

        // handle converse intent
        if(intent != null && intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
            int contactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
            if(contactId == -1) {
                LOG.error("invalid contact id");
            } else {
                try {
                    TalkClientContact contact = getXoDatabase().findClientContactById(contactId);
                    if(contact != null) {
                        converseWithContact(contact);
                    }
                } catch (SQLException e) {
                    LOG.error("sql error", e);
                }
            }
        }

        getXoClient().registerContactListener(this);
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();

        getXoClient().unregisterContactListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        // select client/group profile entry for appropriate icon
        if(mContact != null) {
            MenuItem clientItem = menu.findItem(R.id.menu_profile_client);
            clientItem.setVisible(mContact.isClient());
            MenuItem groupItem = menu.findItem(R.id.menu_profile_group);
            groupItem.setVisible(mContact.isGroup());
        }

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
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
        LOG.debug("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
        mActionBar.setTitle(contact.getName());
        mMessagingFragment.converseWithContact(contact);
        mCompositionFragment.converseWithContact(contact);
        if(mContact.isDeleted()) {
            finish();
        }
        // invalidate menu so that profile buttons get disabled/enabled
        invalidateOptionsMenu();
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        // we don't care
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        if(mContact != null && mContact.getClientContactId() == contact.getClientContactId()) {
            finish();
        }
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        // we don't care
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        // we don't care
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        // we don't care
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        // we don't care
    }

}
