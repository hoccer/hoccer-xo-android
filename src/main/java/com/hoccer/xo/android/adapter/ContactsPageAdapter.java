package com.hoccer.xo.android.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.hoccer.xo.android.fragment.ContactsFragment;
import com.hoccer.xo.android.fragment.FriendRequestFragment;
import com.hoccer.xo.android.fragment.NearbyChatFragment;

public class ContactsPageAdapter extends FragmentPagerAdapter {
    private int mCount;
    private ContactsFragment mContactsFragment;
    private FriendRequestFragment mFriendRequestFragment;
    private NearbyChatFragment mNearbyChatFragment;

    public ContactsPageAdapter(FragmentManager fm, int count) {
        super(fm);
        mCount = count;

        mContactsFragment = new ContactsFragment();
        mFriendRequestFragment = new FriendRequestFragment();
        mNearbyChatFragment = new NearbyChatFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mContactsFragment;
            case 1:
                return mFriendRequestFragment;
            case 2:
                return mNearbyChatFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }
}
