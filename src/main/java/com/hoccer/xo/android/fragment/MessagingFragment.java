package com.hoccer.xo.android.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.ConversationAdapter;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.view.OnOverscrollListener;
import com.hoccer.xo.android.view.OverscrollListView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * Fragment for conversations
 */
public class MessagingFragment extends XoListFragment
        implements SearchView.OnQueryTextListener,
        XoAdapter.AdapterReloadListener, OnOverscrollListener, View.OnTouchListener {

    private static final Logger LOG = Logger.getLogger(MessagingFragment.class);

    private static final int OVERSCROLL_THRESHOLD = -5;

    private OverscrollListView mMessageList;

    private TextView mEmptyText;

    private TalkClientContact mContact;

    private ConversationAdapter mAdapter;

    private View mOverscrollIndicator;

    private boolean mInOverscroll = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_messaging, container, false);

        mMessageList = (OverscrollListView) view.findViewById(android.R.id.list);
        mMessageList.setOverScrollMode(ListView.OVER_SCROLL_IF_CONTENT_SCROLLS);
        mMessageList.addOverScrollListener(this);
        mMessageList.setOnTouchListener(this);
        mMessageList.setMaxOverScrollY(150);
//        mMessageList.setOverscrollHeader(getResources().getDrawable(R.drawable.ic_light_av_replay));
        mEmptyText = (TextView) view.findViewById(R.id.messaging_empty);
        mOverscrollIndicator = view.findViewById(R.id.overscroll_indicator);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        if (mAdapter == null) {
            mAdapter = new ConversationAdapter(getXoActivity());
            mAdapter.setAdapterReloadListener(this);
            mAdapter.onCreate();
            mAdapter.requestReload();
            mMessageList.setAdapter(mAdapter);
        }

        mAdapter.onResume();

        if (mContact != null) {
            mAdapter.converseWithContact(mContact);
        }
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
        mAdapter.onPause();
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

    public void converseWithContact(TalkClientContact contact) {
        LOG.debug("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
        if (mAdapter != null) {
            mAdapter.converseWithContact(contact);
        }
    }

    @Override
    public void onOverscroll(int deltaX, int deltaY, boolean clampedX, boolean clampedY) {
        if (deltaY < OVERSCROLL_THRESHOLD && !mInOverscroll && clampedY) {
            mInOverscroll = true;
            mAdapter.loadNextMessages();
        }
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
}
