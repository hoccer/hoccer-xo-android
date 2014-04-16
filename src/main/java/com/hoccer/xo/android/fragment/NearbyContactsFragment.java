package com.hoccer.xo.android.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyContactsAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.error.EnvironmentUpdaterException;
import com.hoccer.xo.android.nearby.EnvironmentUpdater;

import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import android.os.Bundle;

/**
 * Created by jacob on 10.04.14.
 */
public class NearbyContactsFragment extends XoListFragment {

    private static final Logger LOG = Logger.getLogger(NearbyContactsFragment.class);

    private EnvironmentUpdater mEnvironmentUpdater;
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
        mNearbyAdapter.registerListeners();
        mEnvironmentUpdater = new EnvironmentUpdater(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mEnvironmentUpdater.startEnvironmentTracking();
        } catch (EnvironmentUpdaterException e) {
            // TODO: notify the user that we dont see any environment currently
            LOG.error("no environment information available", e);
        }
        setListAdapter(mNearbyAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEnvironmentUpdater.stopEnvironmentTracking();
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
