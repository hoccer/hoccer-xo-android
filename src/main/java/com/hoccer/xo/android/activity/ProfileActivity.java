package com.hoccer.xo.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoStateListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.ProfileFragment;
import com.hoccer.xo.android.fragment.StatusFragment;
import com.hoccer.xo.release.R;

import java.sql.SQLException;

/**
 * Activity wrapping a profile fragment
 */
public class ProfileActivity extends XoActivity implements IXoContactListener, IXoStateListener {

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

    StatusFragment mStatusFragment;

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
        mStatusFragment = (StatusFragment)fragmentManager.findFragmentById(R.id.activity_profile_status_fragment);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        TalkClientContact contact = mFragment == null ? null : mFragment.getContact();

        boolean isSelf = mMode == Mode.CREATE_SELF || (contact != null && contact.isSelf());

        menu.findItem(R.id.menu_my_profile).setVisible(!isSelf);

        return result;
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        getXoClient().registerContactListener(this);
        getXoClient().registerStateListener(this);

        if(mMode == Mode.CREATE_SELF) {
            mStatusFragment.getView().setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            mStatusFragment.getView().setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void confirmSelf() {
        LOG.debug("confirmSelf()");
        mMode = Mode.CONFIRM_SELF;
        mFragment.confirmSelf();
        update(mFragment.getContact());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(ProfileActivity.this, ContactsActivity.class));
            }
        });
    }

    @Override
    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
        super.hackReturnedFromDialog();
        update(mFragment.getContact());
        mFragment.refreshContact(mFragment.getContact());
    }

    private void update(final TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String title = contact.getName();
                if(mMode == Mode.CREATE_SELF) {
                    title = "Welcome to XO!";
                } else if (mMode == Mode.CREATE_GROUP) {
                    title = "New group";
                } else {
                    if(contact.isSelf()) {
                        title = "My profile";
                    }
                    if(title == null) {
                        title = "<unnamed>";
                    }
                }
                mActionBar.setTitle(title);
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
    public void onClientStateChange(XoClient client, int state) {
//        if(mMode == Mode.CONFIRM_SELF && state == XoClient.STATE_LOGIN) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    finish();
//                    startActivity(new Intent(ProfileActivity.this, ContactsActivity.class));
//                }
//            });
//        }
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
