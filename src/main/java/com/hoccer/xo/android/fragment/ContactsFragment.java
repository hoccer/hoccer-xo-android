package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.FriendRequestAdapter;
import com.hoccer.xo.android.adapter.OnItemCountChangedListener;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.dialog.TokenDialog;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Fragment that shows a list of contacts
 * <p/>
 * This currently shows only contact data but should also be able to show
 * recent conversations for use as a "conversations" view.
 */
public class ContactsFragment extends XoListFragment implements OnItemCountChangedListener,
        IXoContactListener {

    private static final Logger LOG = Logger.getLogger(ContactsFragment.class);

    private XoClientDatabase mDatabase;
    private ContactsAdapter mAdapter;

    private ListView mContactList;

    private TextView mPlaceholderText;

    private ImageView mPlaceholderImage;

    private ListView mFriendRequestListView;

    private FriendRequestAdapter mFriendRequestAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = getXoActivity().getXoDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactList = (ListView) view.findViewById(android.R.id.list);
        mPlaceholderImage = (ImageView) view.findViewById(R.id.iv_contacts_placeholder);
        mPlaceholderText = (TextView) view.findViewById(R.id.tv_contacts_placeholder);

        registerForContextMenu(mContactList);
        return view;
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
        getXoActivity().getXoClient().unregisterContactListener(this);
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        initContactListAdapter();
        initFriendRequestAdapter();
        getXoActivity().getXoClient().registerContactListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object object = mAdapter.getItem(info.position);
        if (object instanceof TalkClientContact) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.context_menu_contacts, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_clear_conversation:
                TalkClientContact contact = (TalkClientContact) mAdapter.getItem(info.position);
                clearConversationForContact(contact);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void clearConversationForContact(TalkClientContact contact) {
        try {
            mDatabase.deleteAllMessagesFromContactId(contact.getClientContactId());
            mAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            LOG.error("SQLException while clearing conversation with contact " + contact.getClientContactId(), e);
        }
    }

    private void initFriendRequestAdapter() {
        mFriendRequestListView = (ListView) getView().findViewById(R.id.lv_contacts_friend_requests);
        if (getXoDatabase().hasPendingFriendRequests()) {
            mFriendRequestAdapter = new FriendRequestAdapter(getXoActivity());
            mFriendRequestAdapter.setOnItemCountChangedListener(this);
            mFriendRequestListView.setAdapter(mFriendRequestAdapter);
        }

        mFriendRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getXoActivity().showContactProfile((TalkClientContact) adapterView.getItemAtPosition(i));
            }
        });

        updateDisplay();
    }

    private void initContactListAdapter() {
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
                        if (contact.isClientRelated() && contact.getClientRelationship().isFriend()) {
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
        updateDisplay();
    }

    private void updateDisplay() {
        showPlaceholder();
        if (mFriendRequestAdapter != null) {
            if (getXoDatabase().hasPendingFriendRequests()) {
                mFriendRequestListView.setVisibility(View.VISIBLE);
                hidePlaceholder();
            } else {
                mFriendRequestListView.setVisibility(View.GONE);
            }
        }
        if (mAdapter != null && mAdapter.getCount() > 0) {
            hidePlaceholder();
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
        if (count > 0) {
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

    @Override
    public void onContactAdded(TalkClientContact contact) {
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if (mFriendRequestAdapter == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFriendRequestAdapter.notifyDataSetChanged();
                updateDisplay();
            }
        });
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
    }
}
