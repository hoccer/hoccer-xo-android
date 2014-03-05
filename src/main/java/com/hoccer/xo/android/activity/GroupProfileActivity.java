package com.hoccer.xo.android.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoStateListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.GroupProfileFragment;
import com.hoccer.xo.android.fragment.StatusFragment;
import com.hoccer.xo.release.R;

import java.sql.SQLException;

/**
 * Activity wrapping a group profile fragment
 */
public class GroupProfileActivity extends XoActivity implements IXoContactListener, IXoStateListener {

    /* use this extra to open in "group creation" mode */
    public static final String EXTRA_CLIENT_CREATE_GROUP = "clientCreateGroup";
    /* use this extra to show the given contact */
    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    private GroupProfileFragment mGroupProfileFragment;
    private StatusFragment mStatusFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_group_profile;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_group_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        enableUpNavigation();
        getActionBar();

        FragmentManager fragmentManager = getFragmentManager();
        mGroupProfileFragment = (GroupProfileFragment) fragmentManager.findFragmentById(R.id.activity_group_profile_fragment);
        mStatusFragment = (StatusFragment) fragmentManager.findFragmentById(R.id.activity_profile_status_fragment);

        // handle intents
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_CLIENT_CREATE_GROUP)) {
                createGroup();
            } else if (intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                int contactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if (contactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showProfile(refreshContact(contactId));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        MenuItem myProfile = menu.findItem(R.id.menu_my_profile);
        myProfile.setVisible(true);

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        getXoClient().registerContactListener(this);
        getXoClient().registerStateListener(this);

        View statusView = mStatusFragment.getView();
        statusView.setVisibility(View.VISIBLE);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();

        getXoClient().unregisterStateListener(this);
        getXoClient().unregisterContactListener(this);
    }

    private TalkClientContact refreshContact(int contactId) {
        LOG.debug("refreshContact(" + contactId + ")");
        try {
            return getXoDatabase().findClientContactById(contactId);
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
        return null;
    }

    public void showProfile(TalkClientContact contact) {
        LOG.debug("showProfile(" + contact.getClientContactId() + ")");

        mGroupProfileFragment.showProfile(contact);
    }

    public void createGroup() {
        LOG.debug("createGroup()");

        mGroupProfileFragment.createGroup();
    }

    @Override
    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
        super.hackReturnedFromDialog();
        mGroupProfileFragment.refreshContact(mGroupProfileFragment.getContact());
    }

    @Override
    public void onClientStateChange(XoClient client, int state) {
        LOG.debug("onClientStateChange()");
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        LOG.debug("onContactAdded()");
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {

    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {

    }

}
