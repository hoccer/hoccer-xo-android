package com.hoccer.talk.android.fragment;

import android.os.RemoteException;
import android.widget.AdapterView;
import android.widget.Button;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Fragment that shows a list of contacts
 *
 * This currently shows only contact data but should also be able to show
 * recent conversations for use as a "conversations" view.
 */
public class ContactsFragment extends TalkFragment implements View.OnClickListener {

	private static final Logger LOG = Logger.getLogger(ContactsFragment.class);

	ListView mContactList;
    Button mFindContactsButton;
    Button mCreateGroupButton;

    public ContactsFragment() {
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("onCreateView()");
		View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        // the contact list itself
		mContactList = (ListView)v.findViewById(R.id.contacts_contact_list);
        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if(item instanceof TalkClientContact) {
                    TalkClientContact contact = (TalkClientContact)item;
                    getTalkActivity().showContactConversation(contact);
                }
            }
        });

        // button leading to pairing activity
        mFindContactsButton = (Button)v.findViewById(R.id.contacts_find_contacts_button);
        mFindContactsButton.setOnClickListener(this);

        // button for creating new groups
        mCreateGroupButton = (Button)v.findViewById(R.id.contacts_create_group_button);
        mCreateGroupButton.setOnClickListener(this);

		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        inflater.inflate(R.menu.fragment_contacts, menu);
    }

	@Override
	public void onResume() {
        super.onResume();
        LOG.info("onResume()");
        // do this late so activity has database initialized
        mContactList.setAdapter(getTalkActivity().makeContactListAdapter());
    }

    @Override
    public void onClick(View v) {
        if(v == mFindContactsButton) {
            getTalkActivity().showPairing();
        }
        if(v == mCreateGroupButton) {
            try {
                getTalkService().createGroup();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGroupCreationSucceeded(int contactId) {
        LOG.info("onGroupCreationSucceeded(" + contactId + ")");
        try {
            TalkClientContact contact = getTalkDatabase().findClientContactById(contactId);
            if(contact != null) {
                getTalkActivity().showContactProfile(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGroupCreationFailed() {
    }
}
