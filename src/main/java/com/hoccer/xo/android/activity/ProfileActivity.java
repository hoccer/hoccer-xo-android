package com.hoccer.xo.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.actionbarsherlock.app.ActionBar;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.ProfileFragment;
import com.hoccer.xo.release.R;

import java.sql.SQLException;

/**
 * Activity wrapping a profile fragment
 */
public class ProfileActivity extends XoActivity implements IXoContactListener {

    /* use this extra to open in "group creation" mode */
    public static final String EXTRA_CLIENT_CREATE_GROUP = "clientCreateGroup";
    /* use this extra to open in "client registration" mode */
    public static final String EXTRA_CLIENT_CREATE_SELF  = "clientCreateSelf";
    /* use this extra to show the given contact */
    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    public enum Mode {
        PROFILE,
        CREATE_GROUP,
        CREATE_SELF,
    }

    Mode mMode;

    ActionBar mActionBar;

    ProfileFragment mFragment;

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

        // handle intents
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(EXTRA_CLIENT_CREATE_SELF)) {
                createSelf();
            } else if(intent.hasExtra(EXTRA_CLIENT_CREATE_GROUP)) {
                createGroup();
            } else if(intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                int contactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if(contactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showProfile(refreshContact(contactId));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        getXoClient().registerContactListener(this);
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();

        getXoClient().unregisterContactListener(this);
    }

    private TalkClientContact refreshContact(int contactId) {
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
        mFragment.showProfile(contact);
        update(contact);

    }

    public void createGroup() {
        LOG.debug("createGroup()");
        mMode = Mode.CREATE_GROUP;
        mFragment.createGroup();
        update(mFragment.getContact());
    }

    public void createSelf() {
        LOG.debug("createSelf()");
        mMode = Mode.CREATE_SELF;
        mFragment.createSelf();
        update(mFragment.getContact());
    }

    @Override
    public void hackReturnedFromDialog() {
        super.hackReturnedFromDialog();
        mFragment.refreshContact();
    }

    private void update(final TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(contact.isSelf()) {
                    mActionBar.setTitle("My profile");
                } else {
                    String title = contact.getName();
                    if(title == null) {
                        if(mMode == Mode.CREATE_GROUP) {
                            title = "New group";
                        }
                        if(mMode == Mode.CREATE_SELF) {
                            title = "Welcome to XO!";
                        }
                    }
                    if(title == null) {
                        title = "<unnamed>";
                    }
                    mActionBar.setTitle(title);
                }
                if (contact.isDeleted()) {
                    finish();
                }
            }
        });
    }

    private boolean isMyContact(TalkClientContact contact) {
        TalkClientContact myContact = mFragment.getContact();
        return myContact != null && myContact.getClientContactId() == contact.getClientContactId();
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
