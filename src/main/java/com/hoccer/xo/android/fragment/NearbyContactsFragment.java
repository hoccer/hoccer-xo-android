package com.hoccer.xo.android.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyContactsAdapter;
import com.hoccer.xo.android.base.XoListFragment;

import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import android.os.Bundle;


public class NearbyContactsFragment extends XoListFragment {
    private static final Logger LOG = Logger.getLogger(NearbyContactsFragment.class);

    NearbyContactsAdapter mNearbyAdapter;
    ListView mContactList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactList = (ListView) view.findViewById(android.R.id.list);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNearbyAdapter = new NearbyContactsAdapter(getXoDatabase(), getXoActivity());
        mNearbyAdapter.setFilter(new NearbyContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
//                if (contact.getGroupPresence().isTypeNearby()) {
                if (contact.isGroupInvolved() && contact.isGroupExisting() && !contact.getGroupPresence().isTypeNearby()) {
                    return true;
                }
                return false;
            }
        });
        mNearbyAdapter.retrieveDataFromDb();
        mNearbyAdapter.registerListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        setListAdapter(mNearbyAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNearbyAdapter.unregisterListeners();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TalkClientContact contact = (TalkClientContact) mContactList.getItemAtPosition(position);
        getXoActivity().showContactConversation(contact);
    }
}
