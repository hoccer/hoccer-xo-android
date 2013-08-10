package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Fragment that shows a list of contacts
 *
 * This currently shows only contact data but should also be able to show
 * recent conversations for use as a "conversations" view.
 */
public class ContactsFragment extends TalkFragment {

	private static final Logger LOG = Logger.getLogger(ContactsFragment.class);

	ListView mContactList;

    public ContactsFragment() {
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.debug("onCreateView()");
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

		return v;
	}

	@Override
	public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        // do this late so activity has database initialized
        mContactList.setAdapter(getTalkActivity().makeContactListAdapter());
    }

    @Override
    public void onGroupCreationSucceeded(int contactId) {
        LOG.debug("onGroupCreationSucceeded(" + contactId + ")");
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
