package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkListFragment;
import com.hoccer.talk.android.adapter.ContactsAdapter;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Fragment that shows a list of contacts
 *
 * This currently shows only contact data but should also be able to show
 * recent conversations for use as a "conversations" view.
 */
public class ContactsFragment extends TalkListFragment {

	private static final Logger LOG = Logger.getLogger(ContactsFragment.class);

	ListView mContactList;

    public ContactsFragment() {
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.debug("onCreateView()");
		View v = inflater.inflate(R.layout.fragment_contacts, container, false);

		mContactList = (ListView)v.findViewById(android.R.id.list);

		return v;
	}

	@Override
	public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        // create list adapter
        ContactsAdapter adapter = getTalkActivity().makeContactListAdapter();

        // filter out never-related contacts (which we know only via groups)
        adapter.setFilter(new ContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
                LOG.info("contact " + contact.getName() + " related " + contact.isEverRelated());
                return (contact.isGroup() && contact.isGroupInvolved())
                        || (contact.isClient() && contact.isClientRelated())
                        || contact.isEverRelated();
            }
        });

        // do this late so activity has database initialized
        mContactList.setAdapter(adapter);
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(l == mContactList) {
            Object item = mContactList.getItemAtPosition(position);
            if(item instanceof TalkClientContact) {
                TalkClientContact contact = (TalkClientContact)item;
                getTalkActivity().showContactConversation(contact);
            }
        }
    }
}
