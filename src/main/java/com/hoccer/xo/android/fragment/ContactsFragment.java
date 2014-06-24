package com.hoccer.xo.android.fragment;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.OnItemCountChangedListener;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.dialog.TokenDialog;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.SQLException;

/**
 * Fragment that shows a list of contacts
 *
 * This currently shows only contact data but should also be able to show
 * recent conversations for use as a "conversations" view.
 */
public class ContactsFragment extends XoListFragment implements OnItemCountChangedListener {

    private static final Logger LOG = Logger.getLogger(ContactsFragment.class);

    private ContactsAdapter mAdapter;

    private ListView mContactList;

    private TextView mPlaceholderText;

    private ImageView mPlaceholderImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactList = (ListView) view.findViewById(android.R.id.list);
        mPlaceholderImage = (ImageView) view.findViewById(R.id.iv_contacts_placeholder);
        mPlaceholderText = (TextView) view.findViewById(R.id.tv_contacts_placeholder);
        return view;
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        if (mAdapter == null) {
            // create list adapter
            mAdapter = getXoActivity().makeContactListAdapter();
            mAdapter.onCreate();
            // filter out never-related contacts (which we know only via groups)
            mAdapter.setFilter(new ContactsAdapter.Filter() {
                @Override
                public boolean shouldShow(TalkClientContact contact) {
                    if (contact.isGroup()) {
                        if (contact.isGroupInvolved() && contact.isGroupExisting() && !contact.getGroupPresence().isTypeNearby()) {
                            return true;
                        }
                    } else if (contact.isClient()) {
                        if (contact.isClientRelated()) {
                            return true;
                        }
                    } else if (contact.isEverRelated()) {
                        return true;
                    }
                    return false;
                }
            });

            mAdapter.requestReload();
            mContactList.setAdapter(mAdapter);
            mAdapter.setOnItemCountChangedListener(this);
        }
        mAdapter.requestReload(); // XXX fix contact adapter and only do this on new adapter
        mAdapter.onResume();
        onItemCountChanged(mAdapter.getCount());
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
        if (mAdapter != null) {
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
            if (contact != null) {
                getXoActivity().showContactProfile(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (l == mContactList) {
            LOG.debug("onListItemClick(contactList," + position + ")");
            Object item = mContactList.getItemAtPosition(position);
            if (item instanceof TalkClientContact) {
                TalkClientContact contact = (TalkClientContact) item;
                if (contact.isGroup() && contact.isGroupInvited()) {
                    getXoActivity().showContactProfile(contact);
                } else {
                    getXoActivity().showContactConversation(contact);
                }
            }
            if (item instanceof TalkClientSmsToken) {
                TalkClientSmsToken token = (TalkClientSmsToken) item;
                new TokenDialog(getXoActivity(), token)
                        .show(getXoActivity().getFragmentManager(), "TokenDialog");
            }
        }
    }

    @Override
    public void onItemCountChanged(int count) {
        if(count > 0) {
            hidePlaceholder();
        } else if (count < 1) {
            showPlaceholder();
        }
    }

    private void showPlaceholder() {
        mPlaceholderImage.setVisibility(View.VISIBLE);
        mPlaceholderText.setVisibility(View.VISIBLE);
    }

    private void hidePlaceholder() {
        mPlaceholderImage.setVisibility(View.GONE);
        mPlaceholderText.setVisibility(View.GONE);
    }
}
