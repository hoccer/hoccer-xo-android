package com.hoccer.xo.android.fragment;

import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.NearbyChatAdapter;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.view.OverscrollListView;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.List;


public class NearbyChatFragment extends XoListFragment implements XoAdapter.AdapterReloadListener, IXoContactListener {
    private static final Logger LOG = Logger.getLogger(NearbyChatFragment.class);
    private NearbyChatAdapter mNearbyAdapter;
    private OverscrollListView mList;
    private ImageView mPlaceholderImage;
    private TextView mPlaceholderText;
    private CompositionFragment mCompositionFragment;

    private TextView mUserCountText;

    private View mNearbyInfoContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_chat, container, false);
        mList = (OverscrollListView) view.findViewById(android.R.id.list);
        mPlaceholderImage = (ImageView) view.findViewById(R.id.iv_contacts_placeholder);
        mPlaceholderImage.setImageResource(R.drawable.placeholder_nearby);
        mPlaceholderText = (TextView) view.findViewById(R.id.tv_contacts_placeholder);
        mPlaceholderText.setText(R.string.placeholder_nearby_text);
        mUserCountText = (TextView) view.findViewById(R.id.tv_nearby_usercount);
        mNearbyInfoContainer = view.findViewById(R.id.rl_nearby_info);
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
        showPlaceholder();
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

    public void showPlaceholder() {
        mCompositionFragment.blockInput();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlaceholderImage.setVisibility(View.VISIBLE);
                mPlaceholderText.setVisibility(View.VISIBLE);
                mList.setVisibility(View.GONE);
                mNearbyInfoContainer.setVisibility(View.GONE);
            }
        });
    }

    private void hidePlaceholder() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlaceholderImage.setVisibility(View.GONE);
                mPlaceholderText.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
                mNearbyInfoContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    public void converseWithContact() {
        if (mNearbyAdapter == null) {
            mNearbyAdapter = new NearbyChatAdapter(mList, getXoActivity(), null);
            mNearbyAdapter.setAdapterReloadListener(this);
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
            final List<TalkClientContact> nearbyGroups = getXoDatabase().findAllNearbyGroups();
            final List<TalkClientContact> allNearbyContacts = getXoDatabase().findAllNearbyContacts();
            if (nearbyGroups.size() > 0) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hidePlaceholder();
                        final TalkClientContact nearbyGroup = nearbyGroups.get(0);
                        mNearbyAdapter.setConverseContact(nearbyGroup);
                        mCompositionFragment.converseWithContact(nearbyGroup);

                        mUserCountText.setText(mActivity.getResources().getString(
                                R.string.nearby_info_usercount, allNearbyContacts.size() - 1));
                        mUserCountText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mActivity.showContactProfile(nearbyGroup);
                            }
                        });
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterReloadStarted(XoAdapter adapter) {

    }

    @Override
    public void onAdapterReloadFinished(XoAdapter adapter) {

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
