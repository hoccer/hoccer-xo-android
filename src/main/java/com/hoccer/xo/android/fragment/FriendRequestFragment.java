package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.FriendRequestAdapter;
import com.hoccer.xo.android.adapter.OnItemCountChangedListener;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * Fragment that shows a list of contacts
 * <p/>
 * This currently shows only contact data but should also be able to show
 * recent conversations for use as a "conversations" view.
 */
public class FriendRequestFragment extends XoListFragment implements OnItemCountChangedListener, IXoContactListener {

    private static final Logger LOG = Logger.getLogger(FriendRequestFragment.class);

    private FriendRequestAdapter mAdapter;
    private ListView mFriendRequestListView;

    private TextView mPlaceholderText;
    private ImageView mPlaceholderImage;

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
        mPlaceholderImage = (ImageView) view.findViewById(R.id.iv_contacts_placeholder);
        mPlaceholderText = (TextView) view.findViewById(R.id.tv_contacts_placeholder);
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
        initFriendRequestAdapter();
        getXoActivity().getXoClient().registerContactListener(this);
    }

    private void initFriendRequestAdapter() {
        if (mAdapter == null) {
            mAdapter = new FriendRequestAdapter(getXoActivity());
            mAdapter.onCreate();
            mAdapter.setOnItemCountChangedListener(this);
            mAdapter.requestReload();
            mFriendRequestListView.setAdapter(mAdapter);
            onItemCountChanged(mAdapter.getCount());
        }
        mAdapter.requestReload();
        mAdapter.onResume();
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
    public void onItemCountChanged(int count) {
        if (count > 0) {
            hidePlaceholder();
        } else if (count < 1) {
            showPlaceholder();
        }
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
