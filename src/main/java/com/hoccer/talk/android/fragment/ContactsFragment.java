package com.hoccer.talk.android.fragment;

import java.util.logging.Logger;

import android.app.SearchManager;
import android.content.Context;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.logging.HoccerLoggers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ContactsFragment extends SherlockFragment {

	private static final Logger LOG =
			HoccerLoggers.getLogger(ContactsFragment.class);
	
	TalkActivity mActivity;
	
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
		
		if (activity instanceof TalkActivity) {
			mActivity = (TalkActivity) activity;
		} else {
			throw new ClassCastException(
				activity.toString() + " must implement TalkActivity");
		}
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
        SherlockFragmentActivity activity = getSherlockActivity();
        inflater.inflate(R.menu.fragment_messaging, menu);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (searchView != null)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
            searchView.setIconifiedByDefault(false);

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
            {
                public boolean onQueryTextChange(String newText)
                {
                    return true;
                }

                public boolean onQueryTextSubmit(String query)
                {
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
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
