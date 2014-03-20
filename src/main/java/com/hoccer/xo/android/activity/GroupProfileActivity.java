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
    private int mContactId;

    private Mode mMode;

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
        mStatusFragment.getView().setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_CLIENT_CREATE_GROUP)) {
                createGroup();
            } else if (intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                mContactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if (mContactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showProfile(refreshContact(mContactId));
                }
            }
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        MenuItem myProfile = menu.findItem(R.id.menu_my_profile);
        myProfile.setVisible(true);

        MenuItem editGroup = menu.findItem(R.id.menu_group_profile_edit);
        MenuItem rejectInvitation = menu.findItem(R.id.menu_group_profile_reject_invitation);
        MenuItem joinGroup = menu.findItem(R.id.menu_group_profile_join);
        MenuItem leaveGroup = menu.findItem(R.id.menu_group_profile_leave);
        editGroup.setVisible(true);
        rejectInvitation.setVisible(false);
        joinGroup.setVisible(false);
        leaveGroup.setVisible(false);

        TalkClientContact contact = mGroupProfileFragment.getContact();
        if (contact.isEditable()) {
            if (mMode == Mode.PROFILE) {
                //editGroup.setVisible(true);
            }
        } else {
            if (contact.isGroupInvited()) {
                rejectInvitation.setVisible(true);
                joinGroup.setVisible(true);
            } else if (contact.isGroupJoined()) {
                leaveGroup.setVisible(true);
            }
        }
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

        if (mMode == Mode.CREATE_SELF) {
            mStatusFragment.getView().setVisibility(View.GONE);
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
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
        mMode = Mode.PROFILE;
        mGroupProfileFragment.showProfile(contact);
    }

    public void createGroup() {
        LOG.debug("createGroup()");
        mMode = Mode.CREATE_SELF;
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

    public enum Mode {
        PROFILE,
        CREATE_SELF
    }

}
