package com.hoccer.xo.android.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.hoccer.xo.android.fragment.ContactsFragment;

public class ContactsPageAdapter extends FragmentPagerAdapter {

    public ContactsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ContactsFragment();
            case 1:
                return new ContactsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
