package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.actionbarsherlock.widget.SearchView;
import com.hoccer.xo.R;
import com.hoccer.talk.android.TalkListFragment;
import com.hoccer.talk.android.adapter.ConversationAdapter;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

/**
 * Fragment for conversations
 */
public class MessagingFragment extends TalkListFragment
        implements SearchView.OnQueryTextListener {

	private static final Logger LOG = Logger.getLogger(MessagingFragment.class);

	ListView mMessageList;

    TalkClientContact mContact;

    ConversationAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.fragment_messaging, container, false);

		mMessageList = (ListView)v.findViewById(android.R.id.list);

		return v;
	}

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        // do this late so activity has database initialized
        if(mAdapter == null) {
            mAdapter = getTalkActivity().makeConversationAdapter();
        }

        // update adapter conversation
        if(mContact != null) {
            mAdapter.converseWithContact(mContact);
        }

        // reload the adapter
        mAdapter.reload();

        // pass the adapter to the list
        mMessageList.setAdapter(mAdapter);
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
        if(mAdapter != null) {
            mAdapter.converseWithContact(contact);
        }
    }

}
