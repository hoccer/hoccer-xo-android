package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import com.hoccer.talk.android.XoListFragment;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.release.R;
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
public class ContactsFragment extends XoListFragment implements View.OnClickListener {

	private static final Logger LOG = Logger.getLogger(ContactsFragment.class);

    ContactsAdapter mAdapter;

    Button mAddUserButton;

	ListView mContactList;

    public ContactsFragment() {
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.debug("onCreateView()");
		View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        mAddUserButton = (Button)v.findViewById(R.id.contacts_pairing);
        mAddUserButton.setOnClickListener(this);

		mContactList = (ListView)v.findViewById(android.R.id.list);

		return v;
	}

	@Override
	public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        if(mAdapter == null) {
            // create list adapter
            mAdapter = getXoActivity().makeContactListAdapter();

            // filter out never-related contacts (which we know only via groups)
            mAdapter.setFilter(new ContactsAdapter.Filter() {
                @Override
                public boolean shouldShow(TalkClientContact contact) {
                    LOG.trace("contact " + contact.getName() + " related " + contact.isEverRelated());
                    return (contact.isGroup() && contact.isGroupInvolved())
                            || (contact.isClient() && contact.isClientRelated())
                            || contact.isEverRelated();
                }
            });
        }

        // reload the adapter
        mAdapter.reload();

        // do this late so activity has database initialized
        mContactList.setAdapter(mAdapter);
    }

    @Override
    public void onGroupCreationSucceeded(int contactId) {
        LOG.debug("onGroupCreationSucceeded(" + contactId + ")");
        try {
            TalkClientContact contact = getXoDatabase().findClientContactById(contactId);
            if(contact != null) {
                getXoActivity().showContactProfile(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGroupCreationFailed() {
    }

    @Override
    public void onClick(View v) {
        if(v == mAddUserButton) {
            getXoActivity().showPairing();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(l == mContactList) {
            Object item = mContactList.getItemAtPosition(position);
            if(item instanceof TalkClientContact) {
                TalkClientContact contact = (TalkClientContact)item;
                getXoActivity().showContactConversation(contact);
            }
            if(item instanceof TalkClientSmsToken) {
                TalkClientSmsToken token = (TalkClientSmsToken)item;
                getXoActivity().showTokenDialog(token);
            }
        }
    }
}
