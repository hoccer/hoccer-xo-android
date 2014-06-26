package com.hoccer.xo.android.activity;

import android.support.v4.app.FragmentManager;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.model.TalkRelationship;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.SingleProfileFragment;
import com.hoccer.xo.android.fragment.StatusFragment;
import com.hoccer.xo.release.R;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import java.sql.SQLException;

/**
 * Activity wrapping a single profile fragment
 */
public class SingleProfileActivity extends XoActivity
        implements IXoContactListener {

    /* use this extra to open in "client registration" mode */
    public static final String EXTRA_CLIENT_CREATE_SELF = "clientCreateSelf";

    /* use this extra to show the given contact */
    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    Mode mMode;

    ActionBar mActionBar;

    SingleProfileFragment mSingleProfileFragment;

    StatusFragment mStatusFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_single_profile;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_single_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        enableUpNavigation();

        mActionBar = getActionBar();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mSingleProfileFragment = (SingleProfileFragment) fragmentManager
                .findFragmentById(R.id.activity_single_profile_fragment);
        mStatusFragment = (StatusFragment) fragmentManager
                .findFragmentById(R.id.activity_profile_status_fragment);
        mStatusFragment.getView().setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_CLIENT_CREATE_SELF)) {
                createSelf();
            } else if (intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                int contactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if (contactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showProfile(refreshContact(contactId));
                }
            }
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        TalkClientContact contact = mSingleProfileFragment == null ? null
                : mSingleProfileFragment.getContact();

        boolean isSelf = mMode == Mode.CREATE_SELF || (contact != null && contact.isSelf());
        menu.findItem(R.id.menu_my_profile).setVisible(!isSelf);

        menu.findItem(R.id.menu_profile_edit).setVisible(false);
        menu.findItem(R.id.menu_profile_delete).setVisible(false);
        menu.findItem(R.id.menu_profile_block).setVisible(false);
        menu.findItem(R.id.menu_profile_unblock).setVisible(false);

        if(contact != null && contact.isSelf()) {
            menu.findItem(R.id.menu_profile_edit).setVisible(true);
            menu.findItem(R.id.menu_profile_block).setVisible(false);
            menu.findItem(R.id.menu_profile_unblock).setVisible(false);
            menu.findItem(R.id.menu_profile_delete).setVisible(false);
        } else {
            if (contact != null && !contact.isNearby()) {
                TalkRelationship relationship = contact.getClientRelationship();
                if (relationship != null) {
                    if (relationship.isBlocked()) {
                        menu.findItem(R.id.menu_profile_delete).setVisible(true);
                        menu.findItem(R.id.menu_profile_block).setVisible(false);
                        menu.findItem(R.id.menu_profile_unblock).setVisible(true);
                    } else if (relationship.isFriend()) {
                        menu.findItem(R.id.menu_profile_delete).setVisible(true);
                        menu.findItem(R.id.menu_profile_block).setVisible(true);
                        menu.findItem(R.id.menu_profile_unblock).setVisible(false);
                    }
                }
            }
        }
        return result;
    }


    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        getXoClient().registerContactListener(this);

        if (mMode == Mode.CREATE_SELF) {
            mStatusFragment.getView().setVisibility(View.GONE);
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();

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
        mSingleProfileFragment.showProfile(contact);
        update(contact);

    }

    public void createSelf() {
        LOG.debug("createSelf()");
        mMode = Mode.CREATE_SELF;
        mSingleProfileFragment.createSelf();
        update(mSingleProfileFragment.getContact());
    }

    public void confirmSelf() {
        LOG.debug("confirmSelf()");
        mMode = Mode.CONFIRM_SELF;
        mSingleProfileFragment.confirmSelf();
        update(mSingleProfileFragment.getContact());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(SingleProfileActivity.this, ContactsActivity.class));
            }
        });
    }

    @Override
    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
        super.hackReturnedFromDialog();
        update(mSingleProfileFragment.getContact());
        mSingleProfileFragment.refreshContact(mSingleProfileFragment.getContact());
    }

    private void update(final TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionBar.setTitle(contact.getName());
                if (mMode == Mode.CREATE_SELF) {
                    mActionBar.setTitle(R.string.welcome_to_title);
                } else {
                    if (contact.isSelf()) {
                        mActionBar.setTitle(R.string.my_profile_title);
                    }
                }
                if (contact.isDeleted()) {
                    finish();
                }
            }
        });
    }

    private boolean isMyContact(TalkClientContact contact) {
        TalkClientContact myContact = mSingleProfileFragment.getContact();
        return myContact != null && myContact.getClientContactId() == contact.getClientContactId();
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        // we don't care
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        if (isMyContact(contact)) {
            finish();
        }
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            update(contact);
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            update(contact);
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            update(contact);
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            update(contact);
        }
    }

    public enum Mode {
        PROFILE,
        CREATE_SELF,
        CONFIRM_SELF
    }

}
