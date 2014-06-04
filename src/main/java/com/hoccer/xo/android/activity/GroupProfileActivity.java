package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActionbarActivity;
import com.hoccer.xo.android.fragment.GroupProfileFragment;
import com.hoccer.xo.release.R;

/**
 * Activity wrapping a single profile fragment
 */
public class GroupProfileActivity extends XoActionbarActivity
        implements IXoContactListener {

    /* use this extra to open in "client registration" mode */
    public static final String EXTRA_CLIENT_CREATE_GROUP = "clientCreateGroup";

    /* use this extra to show the given contact */
    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    ActionBar mActionBar;

    GroupProfileFragment mGroupProfileFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_group_profile;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        enableUpNavigation();

        mActionBar = getActionBar();

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra(EXTRA_CLIENT_CREATE_GROUP)) {
                showCreateGroupProfileFragment();
            } else if (intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                int contactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if (contactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showGroupProfileFragment(contactId);
                }
            }
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
        super.hackReturnedFromDialog();
        mGroupProfileFragment.updateActionBar();
        mGroupProfileFragment.finishActivityIfContactDeleted();
        mGroupProfileFragment.refreshContact(mGroupProfileFragment.getContact());
    }

    private boolean isMyContact(TalkClientContact contact) {
        TalkClientContact myContact = mGroupProfileFragment.getContact();
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
            mGroupProfileFragment.updateActionBar();
            mGroupProfileFragment.finishActivityIfContactDeleted();
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            mGroupProfileFragment.updateActionBar();
            mGroupProfileFragment.finishActivityIfContactDeleted();
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            mGroupProfileFragment.updateActionBar();
            mGroupProfileFragment.finishActivityIfContactDeleted();
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            mGroupProfileFragment.updateActionBar();
            mGroupProfileFragment.finishActivityIfContactDeleted();
        }
    }

    private void showGroupProfileFragment(int contactId) {
        Bundle bundle = new Bundle();
        bundle.putInt(GroupProfileFragment.ARG_CLIENT_CONTACT_ID, contactId);

        mGroupProfileFragment = new GroupProfileFragment();
        mGroupProfileFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_group_profile_fragment_container, mGroupProfileFragment);
        ft.commit();
    }

    private void showCreateGroupProfileFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(GroupProfileFragment.ARG_CREATE_GROUP, true);

        mGroupProfileFragment = new GroupProfileFragment();
        mGroupProfileFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_group_profile_fragment_container, mGroupProfileFragment);
        ft.commit();
    }
}




//package com.hoccer.xo.android.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import com.hoccer.talk.client.model.TalkClientContact;
//import com.hoccer.talk.model.TalkRelationship;
//import com.hoccer.xo.android.base.XoActionbarActivity;
//import com.hoccer.xo.android.fragment.GroupProfileFragment;
//import com.hoccer.xo.release.R;
//
//import java.sql.SQLException;
//
///**
// * Activity wrapping a group profile fragment
// */
//public class GroupProfileActivity extends XoActionbarActivity {
//
//    /* use this extra to open in "group creation" mode */
//    public static final String EXTRA_CLIENT_CREATE_GROUP = "clientCreateGroup";
//    /* use this extra to show the given contact */
//    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";
//
//    private GroupProfileFragment mGroupProfileFragment;
//    private int mContactId;
//
//    private Mode mMode;
//
//    @Override
//    protected int getLayoutResource() {
//        return R.layout.activity_group_profile;
//    }
//
//    @Override
//    protected int getMenuResource() {
//        return R.menu.fragment_group_profile;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        LOG.debug("onCreate()");
//        super.onCreate(savedInstanceState);
//
//        enableUpNavigation();
//        getActionBar();
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        mGroupProfileFragment = (GroupProfileFragment) fragmentManager.findFragmentById(R.id.activity_group_profile_fragment);
//
//        Intent intent = getIntent();
//        if (intent != null) {
//            if (intent.hasExtra(EXTRA_CLIENT_CREATE_GROUP)) {
//                createGroup();
//            } else if (intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
//                mContactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
//                if (mContactId == -1) {
//                    LOG.error("invalid contact id");
//                } else {
//                    showProfile(refreshContact(mContactId));
//                }
//            }
//        }
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        LOG.debug("onCreateOptionsMenu()");
//        boolean result = super.onCreateOptionsMenu(menu);
//
//        if(mMode == Mode.PROFILE) {
//            TalkClientContact contact = mGroupProfileFragment == null ? null : mGroupProfileFragment.getContact();
//            if (contact != null) {
//                menu.findItem(R.id.menu_audio_attachment_list).setVisible(true);
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_audio_attachment_list:
////                showAudioAttachmentList(mGroupProfileFragment.getContact());
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//    @Override
//    protected void onResume() {
//        LOG.debug("onResume()");
//        super.onResume();
//    }
//
//    private TalkClientContact refreshContact(int contactId) {
//        LOG.debug("refreshContact(" + contactId + ")");
//        try {
//            return getXoDatabase().findClientContactById(contactId);
//        } catch (SQLException e) {
//            LOG.error("sql error", e);
//        }
//        return null;
//    }
//
//    public void showProfile(TalkClientContact contact) {
//        LOG.debug("showProfile(" + contact.getClientContactId() + ")");
//        mMode = Mode.PROFILE;
//        mGroupProfileFragment.showProfile(contact);
//    }
//
//    public void createGroup() {
//        LOG.debug("createGroup()");
//        mMode = Mode.CREATE_SELF;
//        mGroupProfileFragment.createGroup();
//    }
//
//    @Override
//    public void hackReturnedFromDialog() {
//        LOG.debug("hackReturnedFromDialog()");
//        super.hackReturnedFromDialog();
//        mGroupProfileFragment.refreshContact(mGroupProfileFragment.getContact());
//    }
//
//    public enum Mode {
//        PROFILE,
//        CREATE_SELF
//    }
//
//}
