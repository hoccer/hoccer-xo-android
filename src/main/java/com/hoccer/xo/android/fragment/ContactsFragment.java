package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import com.hoccer.xo.android.XoDialogs;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.release.R;
import com.hoccer.xo.android.adapter.ContactsAdapter;
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
            mAdapter.onCreate();

            // filter out never-related contacts (which we know only via groups)
            mAdapter.setFilter(new ContactsAdapter.Filter() {
                @Override
                public boolean shouldShow(TalkClientContact contact) {
                    return (contact.isGroup() && contact.isGroupInvolved())
                            || (contact.isClient() && contact.isClientRelated())
                            || contact.isEverRelated();
                }
            });

            mAdapter.requestReload();
        }

        mAdapter.requestReload(); // XXX fix contact adapter

        mAdapter.onResume();

        // do this late so activity has database initialized
        mContactList.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
        if(mAdapter != null) {
            mAdapter.onPause();
            mAdapter.onDestroy();
            mAdapter = null;
        }
    }

    // XXX @Override
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
    public void onClick(View v) {
        if(v == mAddUserButton) {
            LOG.debug("onClick(addUserButton)");
            getXoActivity().showPairing();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(l == mContactList) {
            LOG.debug("onListItemClick(contactList," + position + ")");
            Object item = mContactList.getItemAtPosition(position);
            if(item instanceof TalkClientContact) {
                TalkClientContact contact = (TalkClientContact)item;
                getXoActivity().showContactConversation(contact);
            }
            if(item instanceof TalkClientSmsToken) {
                TalkClientSmsToken token = (TalkClientSmsToken)item;
                XoDialogs.showTokenDialog(getXoActivity(), token);
            }
        }
    }
}
