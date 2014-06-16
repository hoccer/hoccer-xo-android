package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.model.TalkRelationship;
import com.hoccer.xo.android.base.XoActionbarActivity;
import com.hoccer.xo.android.fragment.SingleProfileFragment;
import com.hoccer.xo.release.R;

/**
 * Activity wrapping a single profile fragment
 */
public class SingleProfileActivity extends XoActionbarActivity {

    /* use this extra to open in "client registration" mode */
    public static final String EXTRA_CLIENT_CREATE_SELF = "clientCreateSelf";

    /* use this extra to show the given contact */
    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    Mode mMode;

    ActionBar mActionBar;

    SingleProfileFragment mSingleProfileFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_single_profile;
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
            if (intent.hasExtra(EXTRA_CLIENT_CREATE_SELF)) {
                showCreateSingleProfileFragment();
            } else if (intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
                int contactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
                if (contactId == -1) {
                    LOG.error("invalid contact id");
                } else {
                    showSingleProfileFragment(contactId);
                }
            }
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        if (mMode == Mode.CREATE_SELF) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public void confirmSelf() {
        LOG.debug("confirmSelf()");
        mMode = Mode.CONFIRM_SELF;
        mSingleProfileFragment.confirmSelf();
        mSingleProfileFragment.updateActionBar();
        mSingleProfileFragment.finishActivityIfContactDeleted();

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
        mSingleProfileFragment.updateActionBar();
        mSingleProfileFragment.finishActivityIfContactDeleted();
        mSingleProfileFragment.refreshContact(mSingleProfileFragment.getContact());
    }

    private void showSingleProfileFragment(int contactId) {
        Bundle bundle = new Bundle();
        bundle.putInt(SingleProfileFragment.ARG_CLIENT_CONTACT_ID, contactId);

        mSingleProfileFragment = new SingleProfileFragment();
        mSingleProfileFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_single_profile_fragment_container, mSingleProfileFragment);
        ft.commit();
    }

    private void showCreateSingleProfileFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SingleProfileFragment.ARG_CREATE_SELF, true);

        mSingleProfileFragment = new SingleProfileFragment();
        mSingleProfileFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_single_profile_fragment_container, mSingleProfileFragment);
        ft.commit();
    }

    public enum Mode {
        PROFILE,
        CREATE_SELF,
        CONFIRM_SELF
    }

}
