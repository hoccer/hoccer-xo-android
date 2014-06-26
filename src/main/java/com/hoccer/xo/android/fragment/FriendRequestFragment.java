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
public class FriendRequestFragment extends XoListFragment implements IXoContactListener {

    private static final Logger LOG = Logger.getLogger(FriendRequestFragment.class);

    private FriendRequestAdapter mAdapter;
    private ListView mFriendRequestListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        mFriendRequestListView = (ListView) view.findViewById(android.R.id.list);
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
        getXoActivity().getXoClient().registerContactListener(this);
    }

    private void initContactListAdapter() {
        if (mAdapter == null) {
            mAdapter = new FriendRequestAdapter(getXoActivity());
            mAdapter.onCreate();
            mAdapter.requestReload();
            mFriendRequestListView.setAdapter(mAdapter);
        }
        mAdapter.requestReload();
        mAdapter.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (l == mFriendRequestListView) {
            LOG.debug("onListItemClick(contactList," + position + ")");
            TalkClientContact contact = (TalkClientContact) mFriendRequestListView.getItemAtPosition(position);
            getXoActivity().showContactProfile(contact);
        }
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
        if (mAdapter == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
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
