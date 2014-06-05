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
import com.hoccer.xo.android.adapter.ConversationAdapter;
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
        XoAdapter.AdapterReloadListener, OnOverscrollListener, View.OnTouchListener, IXoContactListener {

    private static final Logger LOG = Logger.getLogger(MessagingFragment.class);

    private static final int OVERSCROLL_THRESHOLD = -5;
    private static final String ARG_CLIENT_CONTACT_ID = "ARG_CLIENT_CONTACT_ID";

    private OverscrollListView mMessageList;

    private MotionInterpreter mMotionInterpreter;

    private TextView mEmptyText;

    private TalkClientContact mContact;

    private ConversationAdapter mAdapter;

    private View mOverscrollIndicator;

    private CompositionView mCompositionView;

    private boolean mInOverscroll = false;

    public interface IMessagingFragmentListener {
        public void onShowSingleProfileFragment();

        public void onShowGroupProfileFragment();

        public void onShowAudioAttachmentListFragment();
    }

    private IMessagingFragmentListener mMessagingFragmentListener;

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
        mMessageList.setOverScrollMode(ListView.OVER_SCROLL_IF_CONTENT_SCROLLS);
        mMessageList.addOverScrollListener(this);
        mMessageList.setOnTouchListener(this);
        mMessageList.setMaxOverScrollY(150);
        mEmptyText = (TextView) view.findViewById(R.id.messaging_empty);
        mOverscrollIndicator = view.findViewById(R.id.overscroll_indicator);
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


        mMotionInterpreter = new MotionInterpreter(Gestures.Transaction.SHARE, getActivity(), mCompositionView);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // select client/group profile entry for appropriate icon
        if (mContact != null) {
            MenuItem clientItem = menu.findItem(R.id.menu_profile_single);
            clientItem.setVisible(mContact.isClient());
            MenuItem groupItem = menu.findItem(R.id.menu_profile_group);
            groupItem.setVisible(mContact.isGroup());
            menu.findItem(R.id.menu_audio_attachment_list).setVisible(true);
        }
        menu.findItem(R.id.menu_profile_single).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case R.id.menu_profile_single:
                if (mContact != null) {
                    mMessagingFragmentListener.onShowSingleProfileFragment();
                }
                break;
            case R.id.menu_profile_group:
                if (mContact != null) {
                    mMessagingFragmentListener.onShowGroupProfileFragment();
                }
                break;
            case R.id.menu_audio_attachment_list:
                if (mContact != null) {
                    mMessagingFragmentListener.onShowAudioAttachmentListFragment();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        setHasOptionsMenu(true);

        if (mAdapter == null) {
            mAdapter = new ConversationAdapter(getXoActivity());
            mAdapter.setAdapterReloadListener(this);
            mAdapter.onCreate();
            mAdapter.requestReload();
            mMessageList.setAdapter(mAdapter);
        }

        converseWithContact();

        mAdapter.onResume();

        if (mContact != null) {
            mAdapter.converseWithContact(mContact);
        }

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
    public void onOverscroll(int deltaX, int deltaY, boolean clampedX, boolean clampedY) {
        if (deltaY < OVERSCROLL_THRESHOLD && !mInOverscroll && clampedY) {
            mInOverscroll = true;
            mAdapter.loadNextMessages();
        }
    }

    public void setMessagingFragmentListener(IMessagingFragmentListener messagingFragmentListener) {
        this.mMessagingFragmentListener = messagingFragmentListener;
    }

    private void animateOverscroll() {
        mOverscrollIndicator.setVisibility(View.VISIBLE);
        mOverscrollIndicator.animate().scaleX(0.0f).setDuration(0).start();
        mOverscrollIndicator.animate().scaleX(1.0f).setDuration(3000)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Log.d("zalem", "start animation");
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mOverscrollIndicator.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mInOverscroll = false;
        }
        return false;
    }

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

    private void converseWithContact() {
        LOG.debug("converseWithContact(" + mContact.getClientContactId() + ")");

        getActivity().getActionBar().setTitle(mContact.getName());
        if (mContact.isDeleted()) {
            getActivity().finish();
        }
        // invalidate menu so that profile buttons get disabled/enabled
        getActivity().invalidateOptionsMenu();

        configureMotionInterpreterForContact(mContact);

        if (mAdapter != null) {
            mAdapter.converseWithContact(mContact);
        }

        mCompositionView.converseWithContact(mContact);
    }
}
