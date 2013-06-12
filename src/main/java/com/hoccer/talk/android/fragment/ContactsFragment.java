package com.hoccer.talk.android.fragment;

import android.widget.AdapterView;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.ITalkActivity;
import com.hoccer.talk.android.database.AndroidTalkDatabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import org.apache.log4j.Logger;

public class ContactsFragment extends SherlockFragment {

	private static final Logger LOG = Logger.getLogger(ContactsFragment.class);
	
	ITalkActivity mActivity;

	ListView mContactList;
    Button mPairingButton;

    public ContactsFragment(ITalkActivity activity) {
        mActivity = activity;
    }
	
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

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("onCreateView()");
		View v = inflater.inflate(R.layout.fragment_contacts, container, false);
		
		mContactList = (ListView)v.findViewById(R.id.contacts_contact_list);
		mContactList.setAdapter(mActivity.makeContactListAdapter());
        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TalkClientContact contact = (TalkClientContact)parent.getItemAtPosition(position);
                mActivity.selectContact(contact);
            }
        });

        mPairingButton = (Button)v.findViewById(R.id.contacts_find_contacts);
        mPairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showPairing();
            }
        });


		
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
