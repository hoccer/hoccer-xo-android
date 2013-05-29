package com.hoccer.talk.android.fragment;

import java.util.logging.Logger;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.ITalkActivity;
import com.hoccer.talk.android.database.AndroidTalkDatabase;
import com.hoccer.talk.logging.HoccerLoggers;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ContactsFragment extends SherlockFragment {

	private static final Logger LOG =
			HoccerLoggers.getLogger(ContactsFragment.class);
	
	ITalkActivity mActivity;

    AndroidTalkDatabase mDatabase;
	
	ListView mContactList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOG.info("onCreate()");
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	}
	
	@Override
	public void onAttach(Activity activity) {
		LOG.info("onAttach()");
		super.onAttach(activity);
		
		if (activity instanceof ITalkActivity) {
			mActivity = (ITalkActivity) activity;
		} else {
			throw new ClassCastException(
				activity.toString() + " must implement ITalkActivity");
		}

        mDatabase = OpenHelperManager.getHelper(activity, AndroidTalkDatabase.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("onCreateView()");
		View v = inflater.inflate(R.layout.fragment_contacts, container, false);
		
		mContactList = (ListView)v.findViewById(R.id.contacts_contact_list);
		mContactList.setAdapter(mActivity.makeContactListAdapter());
		
		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        SherlockFragmentActivity activity = getSherlockActivity();
        inflater.inflate(R.menu.fragment_messaging, menu);
    }

	@Override
	public void onResume() {
		LOG.info("onResume()");
		super.onResume();
	}
	
	@Override
	public void onPause() {
		LOG.info("onPause()");
		super.onPause();
	}

}
