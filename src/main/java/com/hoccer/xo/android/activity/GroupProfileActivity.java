package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoStateListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.model.TalkGroup;
import com.hoccer.xo.android.XoDialogs;
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

    private ActionBar mActionBar;
    private GroupProfileFragment mGroupProfileFragment;
    private StatusFragment mStatusFragment;

    private int mContactId;

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
        mActionBar = getActionBar();

        // add the custom view to the action bar
        mActionBar.setCustomView(R.layout.view_actionbar_group_profile);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageButton doneButton = (ImageButton)mActionBar.getCustomView().findViewById(R.id.image_button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save name and leave
                saveGroup();
                finish();
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        mGroupProfileFragment = (GroupProfileFragment)fragmentManager.findFragmentById(R.id.activity_group_profile_fragment);
        mStatusFragment = (StatusFragment)fragmentManager.findFragmentById(R.id.activity_profile_status_fragment);

        // handle intents
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(EXTRA_CLIENT_CREATE_GROUP)) {
                createGroup();
            } else if(intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                mContactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if(mContactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showProfile(refreshContact(mContactId));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_my_profile).setVisible(true);

        menu.findItem(R.id.menu_group_profile_add_person).setVisible(false);
        menu.findItem(R.id.menu_group_profile_delete).setVisible(false);
        menu.findItem(R.id.menu_group_profile_reject_invitation).setVisible(false);
        menu.findItem(R.id.menu_group_profile_join).setVisible(false);
        menu.findItem(R.id.menu_group_profile_leave).setVisible(false);
        menu.findItem(R.id.menu_group_profile_add_person).setVisible(false);

        TalkClientContact contact = refreshContact(mContactId);
        if (contact != null) {
            if (contact.isEditable()) {
                menu.findItem(R.id.menu_group_profile_delete).setVisible(true);
                menu.findItem(R.id.menu_group_profile_add_person).setVisible(true);
                menu.findItem(R.id.menu_group_profile_join).setVisible(false);
                menu.findItem(R.id.menu_group_profile_leave).setVisible(false);
                menu.findItem(R.id.menu_group_profile_reject_invitation).setVisible(false);
            } else {
                menu.findItem(R.id.menu_group_profile_delete).setVisible(false);
                menu.findItem(R.id.menu_group_profile_add_person).setVisible(false);

                // TODO check wether we are invited or joined
                if (contact.isGroupInvited()) {
                    menu.findItem(R.id.menu_group_profile_reject_invitation).setVisible(true);
                    menu.findItem(R.id.menu_group_profile_join).setVisible(true);
                    menu.findItem(R.id.menu_group_profile_leave).setVisible(false);
                } else if (contact.isGroupJoined()) {
                    menu.findItem(R.id.menu_group_profile_reject_invitation).setVisible(false);
                    menu.findItem(R.id.menu_group_profile_join).setVisible(false);
                    menu.findItem(R.id.menu_group_profile_leave).setVisible(true);
                }
            }
        }

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case R.id.menu_group_profile_delete:
                deleteGroup();
                break;
            case R.id.menu_group_profile_add_person:
                manageGroupMembers();
                break;
            case R.id.menu_group_profile_reject_invitation:
                rejectInvitation();
                break;
            case R.id.menu_group_profile_join:
                joinGroup();
                break;
            case R.id.menu_group_profile_leave:
                leaveGroup();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        getXoClient().registerContactListener(this);
        getXoClient().registerStateListener(this);

        mStatusFragment.getView().setVisibility(View.VISIBLE);
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
        update(contact);
    }

    public void createGroup() {
        LOG.debug("createGroup()");

        mGroupProfileFragment.createGroup();
        update(mGroupProfileFragment.getContact());
    }

    public void saveGroup() {
        LOG.debug("saveGroup()");

        mGroupProfileFragment.saveGroup();
    }

    private void manageGroupMembers() {
        LOG.debug("manageGroupMembers()");
        TalkClientContact contact = refreshContact(mContactId);
        XoDialogs.selectGroupManage(this, contact);
    }

    private void deleteGroup() {
        TalkClientContact contact = refreshContact(mContactId);
        getXoClient().deleteContact(contact);
    }

    private void rejectInvitation() {
        leaveGroup();
        finish();
    }

    private void joinGroup() {
        TalkClientContact contact = refreshContact(mContactId);
        getXoClient().joinGroup(contact.getGroupId());
        finish();
    }

    private void leaveGroup() {
        TalkClientContact contact = refreshContact(mContactId);
        getXoClient().leaveGroup(contact.getGroupId());
        finish();
    }

    @Override
    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
        super.hackReturnedFromDialog();
        update(mGroupProfileFragment.getContact());
        mGroupProfileFragment.refreshContact(mGroupProfileFragment.getContact());
    }

    private void update(final TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contact.isDeleted()) {
                    finish();
                }
            }
        });
    }

    private boolean isMyContact(TalkClientContact contact) {
        TalkClientContact myContact = mGroupProfileFragment.getContact();
        return myContact != null && myContact.getClientContactId() == contact.getClientContactId();
    }

    @Override
    public void onClientStateChange(XoClient client, int state) {
        // we don't care
        LOG.debug("onClientStateChange()");
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        // we don't care
        LOG.debug("onContactAdded()");
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        if(isMyContact(contact)) {
            finish();
        }
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            update(contact);
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            update(contact);
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            update(contact);
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            update(contact);
        }
    }

}
