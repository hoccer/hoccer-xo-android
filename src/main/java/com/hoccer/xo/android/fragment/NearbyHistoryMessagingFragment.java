package com.hoccer.xo.android.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyChatAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.view.OverscrollListView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class NearbyHistoryMessagingFragment extends XoListFragment implements IXoContactListener {

    private static final Logger LOG = Logger.getLogger(NearbyChatFragment.class);

    private NearbyChatAdapter mNearbyAdapter;
    private OverscrollListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_history_messaging, container, false);

        mList = (OverscrollListView) view.findViewById(android.R.id.list);

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
        converseWithContact();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mNearbyAdapter.onDestroy();
        super.onDestroy();
    }

    public void converseWithContact() {
        if (mNearbyAdapter == null) {
            mNearbyAdapter = new NearbyChatAdapter(mList, getXoActivity());
            mNearbyAdapter.onCreate();
            mList.setAdapter(mNearbyAdapter);
        }
        mNearbyAdapter.onResume();
        updateViews();
    }

    private void updateViews() {
        try {
            if (mActivity == null) {
                return;
            }

            // TODD: find all nearby messages
            final List<TalkClientContact> nearbyGroups = getXoDatabase().findAllNearbyGroups();
            if (nearbyGroups.size() > 0) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final TalkClientContact nearbyGroup = nearbyGroups.get(0);
                        mNearbyAdapter.setConverseContact(nearbyGroup);
                        mNearbyAdapter.requestReload();
                        mNearbyAdapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        updateViews();
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        updateViews();
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        updateViews();
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        updateViews();
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        updateViews();
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        updateViews();
    }

}
