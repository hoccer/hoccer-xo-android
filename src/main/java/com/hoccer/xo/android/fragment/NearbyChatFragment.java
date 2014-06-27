package com.hoccer.xo.android.fragment;

import android.support.v4.app.FragmentTransaction;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyChatAdapter;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.view.OverscrollListView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.List;


public class NearbyChatFragment extends XoListFragment implements IXoContactListener {
    private static final Logger LOG = Logger.getLogger(NearbyChatFragment.class);
    private NearbyChatAdapter mNearbyAdapter;
    private OverscrollListView mList;
    private ImageView mPlaceholderImage;
    private TextView mPlaceholderText;
    private CompositionFragment mCompositionFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_chat, container, false);
        mList = (OverscrollListView)  view.findViewById(android.R.id.list);
        mPlaceholderImage = (ImageView) view.findViewById(R.id.iv_contacts_placeholder);
        mPlaceholderImage.setImageResource(R.drawable.placeholder_nearby);
        mPlaceholderText = (TextView) view.findViewById(R.id.tv_contacts_placeholder);
        mPlaceholderText.setText(R.string.placeholder_nearby_text);
        mCompositionFragment = new CompositionFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mCompositionFragment).commit();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getXoActivity().getXoClient().registerContactListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChatContact();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getXoActivity().getXoClient().unregisterContactListener(this);
        destroyAdapter();
        super.onDestroy();
    }

    public void showPlaceholder() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCompositionFragment.blockInput(true);
                mPlaceholderImage.setVisibility(View.VISIBLE);
                mPlaceholderText.setVisibility(View.VISIBLE);
                mList.setVisibility(View.GONE);
            }
        });
    }

    private void hidePlaceholder() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCompositionFragment.blockInput(false);
                mPlaceholderImage.setVisibility(View.GONE);
                mPlaceholderText.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateChatContact() {
        try {
            List<TalkClientContact> nearbyGroups = getXoDatabase().findAllNearbyGroups();
            if (nearbyGroups.size() > 0) {
                hidePlaceholder();
                createAdapterForContact(nearbyGroups.get(0));
                mCompositionFragment.setContact(nearbyGroups.get(0));
            } else {
                showPlaceholder();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createAdapterForContact(TalkClientContact contact) {
        destroyAdapter();

        mNearbyAdapter = new NearbyChatAdapter(mList, getXoActivity(), contact);
        mNearbyAdapter.onCreate();
        mNearbyAdapter.onResume();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.setAdapter(mNearbyAdapter);
            }
        });
    }

    private void destroyAdapter() {
        if (mNearbyAdapter != null) {
            mNearbyAdapter.onDestroy();
        }
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        updateChatContact();
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        updateChatContact();
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        updateChatContact();
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        updateChatContact();
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        updateChatContact();
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        updateChatContact();
    }


}
