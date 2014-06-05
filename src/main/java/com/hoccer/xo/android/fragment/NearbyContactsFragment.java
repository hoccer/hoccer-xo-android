package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyContactsAdapter;
import com.hoccer.xo.android.adapter.OnItemCountChangedListener;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.test.R;
import org.apache.log4j.Logger;


public class NearbyContactsFragment extends XoListFragment {
    private static final Logger LOG = Logger.getLogger(NearbyContactsFragment.class);

    private NearbyContactsAdapter mNearbyAdapter;
    private ListView mContactList;

    private ImageView mPlaceholderImage;

    private TextView mPlaceholderText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactList = (ListView) view.findViewById(android.R.id.list);
        mPlaceholderImage = (ImageView) view.findViewById(R.id.iv_contacts_placeholder);
        mPlaceholderImage.setImageResource(R.drawable.placeholder_nearby);
        mPlaceholderText = (TextView) view.findViewById(R.id.tv_contacts_placeholder);
        mPlaceholderText.setText(R.string.placeholder_nearby_text);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNearbyAdapter = new NearbyContactsAdapter(getXoDatabase(), getXoActivity());
        mNearbyAdapter.retrieveDataFromDb();
        mNearbyAdapter.registerListeners();
        mNearbyAdapter.setOnItemCountChangedListener(new OnItemCountChangedListener() {
            @Override
            public void onItemCountChanged(int count) {
                if (count > 0) {
                    hidePlaceholder();
                } else if (count < 1) {
                    showPlaceholder();
                }
            }
        });
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
        mNearbyAdapter.unregisterListeners();
        super.onDestroy();
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
