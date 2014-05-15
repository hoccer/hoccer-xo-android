package com.hoccer.xo.android.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyContactsAdapter;
import com.hoccer.xo.android.base.XoListFragment;

import com.whitelabel.gw.release.R;
import org.apache.log4j.Logger;

import android.os.Bundle;


public class NearbyContactsFragment extends XoListFragment {
    private static final Logger LOG = Logger.getLogger(NearbyContactsFragment.class);

    private NearbyContactsAdapter mNearbyAdapter;
    private ListView mContactList;
    private Button mAddContactButton;
    private TextView mNoContactsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactList = (ListView) view.findViewById(android.R.id.list);
        mAddContactButton = (Button) view.findViewById(R.id.contacts_pairing);
        mNoContactsTextView = (TextView) view.findViewById(R.id.txt_no_contacts);
        mAddContactButton.setVisibility(View.GONE);
        mNoContactsTextView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNearbyAdapter = new NearbyContactsAdapter(getXoDatabase(), getXoActivity());
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
        if (l == mContactList) {
            LOG.debug("onListItemClick(contactList," + position + ")");
            Object item = mContactList.getItemAtPosition(position);
            if (item instanceof TalkClientContact) {
                TalkClientContact contact = (TalkClientContact) item;
                getXoActivity().showContactConversation(contact);
            }
        }
    }
}
