package com.hoccer.xo.android.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.hoccer.xo.android.fragment.ContactsFragment;
import com.hoccer.xo.android.fragment.NearbyContactsFragment;
import com.hoccer.xo.release.R;

public class ContactsPageAdapter extends FragmentPagerAdapter {
    int mCount;

    public ContactsPageAdapter(FragmentManager fm, int count) {
        super(fm);
        mCount = count;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ContactsFragment();
            case 1:
                return new NearbyContactsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }
}
