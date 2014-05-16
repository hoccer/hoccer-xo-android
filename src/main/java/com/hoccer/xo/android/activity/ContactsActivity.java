package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.adapter.ContactsPageAdapter;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.error.EnvironmentUpdaterException;
import com.hoccer.xo.android.fragment.NearbyContactsFragment;
import com.hoccer.xo.android.nearby.EnvironmentUpdater;
import com.hoccer.xo.release.R;

public class ContactsActivity extends XoActivity {

    private SharedPreferences mPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener;

    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private ContactsPageAdapter mAdapter;

    private EnvironmentUpdater mEnvironmentUpdater;

    private boolean mEnvironmentUpdatesEnabled;
    private boolean mNoUserInput = false;

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

        if (!getXoClient().isRegistered()) {
            Intent intent = new Intent(this, SingleProfileActivity.class);
            intent.putExtra(SingleProfileActivity.EXTRA_CLIENT_CREATE_SELF, true);
            startActivity(intent);
        } else {
            String[] tabs = getResources().getStringArray(R.array.tab_names);
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setOnPageChangeListener(new ConversationsPageListener());

            mActionBar = getActionBar();
            mAdapter = new ContactsPageAdapter(getSupportFragmentManager(), tabs.length);
            mViewPager.setAdapter(mAdapter);
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            for (String tabName : tabs) {
                mActionBar.addTab(mActionBar.newTab().setText(tabName).setTabListener(new ConversationsTabListener()));
            }

            mEnvironmentUpdater = XoApplication.getEnvironmentUpdater();

            mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("preference_environment_update")) {
                        mEnvironmentUpdatesEnabled = sharedPreferences.getBoolean("preference_environment_update", true);
                        refreshEnvironmentUpdater();
                    }
                }
            };
            mPreferences.registerOnSharedPreferenceChangeListener(mPreferencesListener);
            mEnvironmentUpdatesEnabled = mPreferences.getBoolean("preference_environment_update", true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getXoClient().isRegistered()) {
            finish();
        } else {
            refreshEnvironmentUpdater();
        }

    }

    private void refreshEnvironmentUpdater() {
        int position = mViewPager.getCurrentItem();
        Fragment fragment = mAdapter.getItem(position);
        if (fragment instanceof NearbyContactsFragment) {
            if (mEnvironmentUpdatesEnabled) {
                if (checkIfGpsIsTurnedOn()) {
                    XoApplication.startNearbySession();
                }
            }
        } else {
            XoApplication.stopNearbySession();
        }
    }

    private boolean checkIfGpsIsTurnedOn() {
        final LocationManager manager = (LocationManager)getSystemService(getBaseContext().LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled( LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.nearby_enable_location_service))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.nearby_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.nearby_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
        return true;
    }

    private class ConversationsPageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            refreshEnvironmentUpdater();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                mNoUserInput = true;
                mActionBar.setSelectedNavigationItem(mViewPager.getCurrentItem());
                mNoUserInput = false;
            }
        }
    }

    private class ConversationsTabListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (!mNoUserInput) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }
}
