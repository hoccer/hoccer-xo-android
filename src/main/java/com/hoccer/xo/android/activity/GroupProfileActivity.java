package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoStateListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.model.TalkClientContact;
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

        TalkClientContact contact = refreshContact(mContactId);
        if (!contact.isEditable()) {
            menu.findItem(R.id.menu_group_profile_delete).setVisible(false);
            menu.findItem(R.id.menu_group_profile_add_person).setVisible(false);
        }

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case R.id.menu_group_profile_delete:
                // TODO: delete whatever...
                break;
            case R.id.menu_group_profile_add_person:
                manageContacts();
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

    public void addContact() {
        LOG.debug("addContact()");
        mGroupProfileFragment.addContact();
    }

    public void removeContact() {
        LOG.debug("removeContact()");
        mGroupProfileFragment.removeContact();
    }

    private void manageContacts() {
        LOG.debug("manageContacts()");
        TalkClientContact contact = refreshContact(mContactId);
        XoDialogs.selectGroupManage(this, contact);
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
                String title = contact.getName();
                if(contact.isSelf()) {
                    title = "My profile";
                }
                if(title == null) {
                    title = "<unnamed>";
                }
                mActionBar.setTitle(title);
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
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        // we don't care
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
