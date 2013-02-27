package com.hoccer.talk.android;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.hoccer.talk.android.fragment.ContactsFragment;
import com.hoccer.talk.android.fragment.MessagingFragment;
import com.hoccer.talk.android.model.TalkMessage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hoccer.talk.android.service.ITalkClientService;

public class MainActivity extends SherlockFragmentActivity
		implements TalkActivity {

	private static final int VIEW_CONTACTS  = 0;
	private static final int VIEW_MESSAGING = 1;
	
	FragmentManager mFragmentManager;
	
	ActionBar mActionBar;
	
	ViewPager mViewPager;
	MainPagerAdapter mViewPagerAdapter;
	
	ContactsFragment mContactsFragment;
	MessagingFragment mMessagingFragment;
	
	ScheduledExecutorService mBackgroundExecutor = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		
		// get the fragment manager
		mFragmentManager = getSupportFragmentManager();
		
		// apply the layout (this is indirect and depends on the device)
		setContentView(R.layout.activity_main);
		
		// get and configure the action bar
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		// set up fragment handling
		mViewPager = (ViewPager)findViewById(R.id.main_fragment_pager);
		if(mViewPager != null) {
			// if we have a ViewPager we use a one-pane layout (handset version)
			mViewPagerAdapter = new MainPagerAdapter();
			
			mViewPager.setAdapter(mViewPagerAdapter);
			mViewPager.setCurrentItem(VIEW_MESSAGING);
		} else {
			// if we have a ViewPager we use a one-pane layout (handset version)
			mContactsFragment = (ContactsFragment)
					mFragmentManager.findFragmentById(
							R.id.main_fragment_contacts);
			mMessagingFragment = (MessagingFragment)
					mFragmentManager.findFragmentById(
							R.id.main_fragment_messaging);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.common, menu);
		return true;
	}
	
	public class MainPagerAdapter extends FragmentPagerAdapter {

		public MainPagerAdapter() {
			super(mFragmentManager);
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch(position) {
			case VIEW_CONTACTS:
				if(mContactsFragment == null) {
					mContactsFragment = new ContactsFragment();
				}
				fragment = mContactsFragment;
				break;
			case VIEW_MESSAGING:
				if(mMessagingFragment == null) {
					mMessagingFragment = new MessagingFragment();
				}
				fragment = mMessagingFragment;
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
	}
	
	public BaseAdapter makeContactListAdapter() {
		ContactListAdapter a = new ContactListAdapter(this);
		a.add("Pia");
		a.add("Udo");
		a.add("George");
		return a;
	}
	
	public class ContactListAdapter extends ArrayAdapter<String> {

		LayoutInflater mInflater;
		
		public ContactListAdapter(Context context) {
			super(context, R.id.contact_name);
			mInflater = getLayoutInflater();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if(v == null) {
				v = mInflater.inflate(R.layout.item_contact, null);
			}
			
			return v;
		}
		
	}

    @Override
    public ITalkClientService getTalkClientService() {
        return null;
    }

    public BaseAdapter makeMessageListAdapter() {
		MessageListAdapter a = new MessageListAdapter(this);
		generateTestMessages(a);
		generateMessagesInBackground(a);
		return a;
	}
	
	private void generateTestMessages(MessageListAdapter a) {
		for(int i = 0; i < 10; i++) {
			boolean foo = (i & 2) == 0;
			TalkMessage m = new TalkMessage(foo ? "Pia" : "George", "Test message #" + i);
			a.add(m);
		}
	}
	
	private void generateMessagesInBackground(final MessageListAdapter a) {
		mBackgroundExecutor.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				Log.d("Foo", "Delivering message from Udo");
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.d("Foo", "Scheduling next delivery");
						TalkMessage m = new TalkMessage("Udo", "Foobar!");

						a.add(m);
					}
				});

			}
		}, 5000, 5000, TimeUnit.MILLISECONDS);
	}
	
	class MessageListAdapter extends ArrayAdapter<TalkMessage> {

		LayoutInflater mInflater;
		
		public MessageListAdapter(Context context) {
			super(context, R.layout.item_message, R.id.item_content, new ArrayList<TalkMessage>());
			mInflater = getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TalkMessage m = getItem(position);
			View v = convertView;
			
			if(v == null) {
				v = mInflater.inflate(R.layout.item_message, null);
			}
			
			TextView content = (TextView)v.findViewById(R.id.item_content);
			TextView sender = (TextView)v.findViewById(R.id.item_sender_name);
			
			content.setText(m.getContent());
			sender.setText(m.getSender());
			
			return v;
		}
		
	}

}
