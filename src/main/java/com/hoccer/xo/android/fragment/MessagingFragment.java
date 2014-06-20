package com.hoccer.xo.android.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.adapter.ChatAdapter;
import com.hoccer.xo.android.base.IMessagingFragmentManager;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.gesture.Gestures;
import com.hoccer.xo.android.gesture.MotionInterpreter;
import com.hoccer.xo.android.view.CompositionView;
import com.hoccer.xo.android.view.OnOverscrollListener;
import com.hoccer.xo.android.view.OverscrollListView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Fragment for conversations
 */
public class MessagingFragment extends XoListFragment
        implements SearchView.OnQueryTextListener,
        XoAdapter.AdapterReloadListener, IXoContactListener {

    public static final String ARG_CLIENT_CONTACT_ID = "com.hoccer.xo.android.fragment.ARG_CLIENT_CONTACT_ID";

    private static final Logger LOG = Logger.getLogger(MessagingFragment.class);
    private static final int OVERSCROLL_THRESHOLD = -5;

    private OverscrollListView mMessageList;

    private MotionInterpreter mMotionInterpreter;

    private TextView mEmptyText;

    private TalkClientContact mContact;

    private ChatAdapter mAdapter;

    private View mOverscrollIndicator;

    private CompositionView mCompositionView;

    private boolean mInOverscroll = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int clientContactId = getArguments().getInt(ARG_CLIENT_CONTACT_ID);
            if (clientContactId == -1) {
                LOG.error("invalid contact id");
            } else {
                try {
                    mContact = XoApplication.getXoClient().getDatabase().findClientContactById(clientContactId);
                } catch (SQLException e) {
                    LOG.error("sql error", e);
                }
            }

        } else {
            LOG.error("Creating SingleProfileFragment without arguments is not supported.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_messaging, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessageList = (OverscrollListView) view.findViewById(android.R.id.list);
        mEmptyText = (TextView) view.findViewById(R.id.messaging_empty);
        mCompositionView = ((CompositionView) view.findViewById(R.id.cv_composition));
        mCompositionView.setCompositionViewListener(new CompositionView.ICompositionViewListener() {
            @Override
            public void onAddAttachmentClicked() {
                getXoActivity().selectAttachment();
            }

            @Override
            public void onAttachmentClicked() {
                getXoActivity().selectAttachment();
            }
        });
        mCompositionView.setContact(mContact);

        mMotionInterpreter = new MotionInterpreter(Gestures.Transaction.SHARE, getActivity(), mCompositionView);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mContact.isDeleted()) {
            getActivity().finish();
        }

        setHasOptionsMenu(true);

        if (mAdapter == null) {
            mAdapter = new ChatAdapter(mMessageList, getXoActivity(), mContact);
            mAdapter.setAdapterReloadListener(this);
            mAdapter.onCreate();
        }

        mMessageList.setAdapter(mAdapter);

        configureMotionInterpreterForContact(mContact);
        XoApplication.getXoClient().registerContactListener(this);
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
        mAdapter.onPause();
        mMotionInterpreter.deactivate();
        XoApplication.getXoClient().unregisterContactListener(this);
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.onDestroy();
            mAdapter = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // select client/group profile entry for appropriate icon
        if (mContact != null) {
            MenuItem clientItem = menu.findItem(R.id.menu_profile_single);
            clientItem.setVisible(mContact.isClient());
            MenuItem groupItem = menu.findItem(R.id.menu_profile_group);
            groupItem.setVisible(mContact.isGroup());
            menu.findItem(R.id.menu_audio_attachment_list).setVisible(true);
            getActivity().getActionBar().setTitle(mContact.getName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");

        IMessagingFragmentManager mgr = (IMessagingFragmentManager)getActivity();
        switch (item.getItemId()) {
            case R.id.menu_profile_single:
                if (mContact != null && mgr != null) {
                    mgr.showSingleProfileFragment();
                }
                break;
            case R.id.menu_profile_group:
                if (mContact != null && mgr != null) {
                    mgr.showGroupProfileFragment();
                }
                break;
            case R.id.menu_audio_attachment_list:
                if (mContact != null && mgr != null) {
                    mgr.showAudioAttachmentListFragment();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onAdapterReloadStarted(XoAdapter adapter) {
        LOG.debug("onAdapterReloadStarted()");
        mEmptyText.setText(R.string.messaging_loading);
    }

    @Override
    public void onAdapterReloadFinished(XoAdapter adapter) {
        LOG.debug("onAdapterReloadFinished()");
        mEmptyText.setText(R.string.messaging_no_messages);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        LOG.debug("onQueryTextChange(\"" + newText + "\")");
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        LOG.debug("onQueryTextSubmit(\"" + query + "\")");
        return true;
    }

//    @Override
//    public void clipBoardItemSelected(IContentObject contentObject) {
//        mCompositionView.onAttachmentSelected(contentObject);
//    }

    @Override
    public void onAttachmentSelected(IContentObject contentObject) {
        mCompositionView.onAttachmentSelected(contentObject);
    }

    public void configureMotionInterpreterForContact(TalkClientContact contact) {
        // react on gestures only when contact is nearby
        if (contact != null && (contact.isNearby() || (contact.isGroup() && contact.getGroupPresence().isTypeNearby()))) {
            mMotionInterpreter.activate();
        } else {
            mMotionInterpreter.deactivate();
        }
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {

    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        if (mContact != null && mContact.getClientContactId() == contact.getClientContactId()) {
            getActivity().finish();
        }
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {

    }

    public void applicationWillEnterBackground() {
        if (mContact.isGroup() && mContact.getGroupPresence().isTypeNearby()) {
            getActivity().finish();
        } else if (mContact.isClient() && mContact.isNearby()) {
            getActivity().finish();
        }
    }
}
