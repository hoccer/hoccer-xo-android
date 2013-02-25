package com.hoccer.talk.android;

import com.hoccer.talk.android.fragment.ContactsFragment;
import com.hoccer.talk.android.fragment.MessagingFragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity {

	private static final int VIEW_CONTACTS  = 0;
	private static final int VIEW_MESSAGING = 1;
	
	ActionBar mActionBar;
	
	ViewPager mViewPager;
	MainPagerAdapter mViewPagerAdapter;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		
		setContentView(R.layout.activity_main);
		
		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		mViewPager = (ViewPager)findViewById(R.id.main_fragment_pager);
		if(mViewPager != null) {
			mViewPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
			
			mViewPager.setAdapter(mViewPagerAdapter);
			mViewPager.setCurrentItem(VIEW_MESSAGING);
		}
	}
	
	public class MainPagerAdapter extends FragmentPagerAdapter {

		public MainPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch(position) {
			case VIEW_CONTACTS:
				fragment = new ContactsFragment();
				break;
			case VIEW_MESSAGING:
				fragment = new MessagingFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}
		
	}

}
