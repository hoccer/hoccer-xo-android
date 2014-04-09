package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.hoccer.xo.android.adapter.ContactsPageAdapter;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

public class ContactsActivity extends XoActivity implements ActionBar.TabListener {
    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private ContactsPageAdapter mAdapter;
    private String[] tabs = { "Top Rated", "Games"};

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contacts;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_contacts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getXoClient().isRegistered()) {
            LOG.info("not registered - redirecting to profile activity");
            Intent intent = new Intent(this, SingleProfileActivity.class);
            intent.putExtra(SingleProfileActivity.EXTRA_CLIENT_CREATE_SELF, true);
            startActivity(intent);
        } else {

        
            // Initilization
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mActionBar = getActionBar();
            mAdapter = new ContactsPageAdapter(getSupportFragmentManager());

            mViewPager.setAdapter(mAdapter);
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Adding Tabs
            for (String tab_name : tabs) {
                mActionBar.addTab(mActionBar.newTab().setText(tab_name)
                        .setTabListener(this));
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!getXoClient().isRegistered()) {
            LOG.info("not registered - finishing");
            finish();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}
