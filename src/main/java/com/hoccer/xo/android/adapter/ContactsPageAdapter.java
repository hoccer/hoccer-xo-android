package com.hoccer.xo.android.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.hoccer.xo.android.fragment.ContactsFragment;
import com.hoccer.xo.android.fragment.NearbyContactsFragment;

public class ContactsPageAdapter extends FragmentPagerAdapter {
    private int mCount;
    private ContactsFragment mContactsFragment;
    private NearbyContactsFragment mNearbyContactsFragment;

    public ContactsPageAdapter(FragmentManager fm, int count) {
        super(fm);
        mCount = count;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (mContactsFragment == null) {
                    mContactsFragment = new ContactsFragment();
                }
                return mContactsFragment;
            case 1:
                if (mNearbyContactsFragment == null) {
                    mNearbyContactsFragment = new NearbyContactsFragment();
                }
                return mNearbyContactsFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }
}
