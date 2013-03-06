package com.hoccer.talk.android.fragment;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import android.app.SearchManager;
import android.content.Context;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.database.TalkDatabase;
import com.hoccer.talk.logging.HoccerLoggers;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.model.TalkDelivery;
import com.hoccer.talk.model.TalkMessage;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

public class MessagingFragment extends SherlockFragment
        implements View.OnClickListener, SearchView.OnQueryTextListener {

	private static final Logger LOG =
			HoccerLoggers.getLogger(MessagingFragment.class);

	TalkActivity mActivity;

    TalkDatabase mDatabase;

	ListView mMessageList;

    EditText mTextEdit;
    Button mSendButton;
    Button mAttachButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOG.info("onCreate()");
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		LOG.info("onAttach()");
		super.onAttach(activity);

		if (activity instanceof TalkActivity) {
			mActivity = (TalkActivity) activity;
		} else {
			throw new ClassCastException(
				activity.toString() + " must implement TalkActivity");
		}

        mDatabase = OpenHelperManager.getHelper(activity, TalkDatabase.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("onCreateView()");

		View v = inflater.inflate(R.layout.fragment_messaging, container, false);

		mMessageList = (ListView)v.findViewById(R.id.messaging_message_list);
		mMessageList.setAdapter(mActivity.makeMessageListAdapter());

        mTextEdit = (EditText)v.findViewById(R.id.messaging_composer_text);

        mSendButton = (Button)v.findViewById(R.id.messaging_composer_send);
        mSendButton.setOnClickListener(this);

        mAttachButton = (Button)v.findViewById(R.id.messaging_composer_attach);
        mAttachButton.setOnClickListener(this);

		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        SherlockFragmentActivity activity = getSherlockActivity();
        inflater.inflate(R.menu.fragment_messaging, menu);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setOnQueryTextListener(this);
        }
    }

    @Override
    public void onPause() {
        LOG.info("onPause()");
        super.onPause();
    }

	@Override
	public void onResume() {
		LOG.info("onResume()");
		super.onResume();
	}

    @Override
    public void onClick(View v) {
        if(v == mSendButton) {
            LOG.info("onClick(SendButton)");
            sendComposedMessage();
        }
        if(v == mAttachButton) {
            LOG.info("onClick(AttachButton)");
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        LOG.info("onQueryTextChange(\"" + newText + "\")");
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        LOG.info("onQueryTextSubmit(\"" + query + "\")");
        return true;
    }

    private void clearComposedMessage() {
        mTextEdit.setText(null);
    }

    private void sendComposedMessage() {
        // construct message and delivery objects
        final TalkMessage message = new TalkMessage();
        final TalkDelivery[] deliveries = new TalkDelivery[0];

        final String messageTag = UUID.randomUUID().toString();

        message.setMessageTag(messageTag);
        message.setBody(mTextEdit.getText().toString());

        for(int i = 0; i < deliveries.length; i++) {
            deliveries[i] = new TalkDelivery();
            deliveries[i].setMessageTag(messageTag);
        }

        // log to help debugging
        LOG.info("created message with tag " + message.getMessageTag());

        // save new objects to database
        try {
            LOG.info("saving message to database");
            TransactionManager.callInTransaction(mDatabase.getConnectionSource(),
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        // save the message itself
                        mDatabase.getMessageDao().create(message);
                        // save related deliveries
                        for(int i = 0; i < deliveries.length; i++) {
                            mDatabase.getDeliveryDao().create(deliveries[i]);
                        }
                        return null;
                    }
                });
        } catch (SQLException e) {
            // XXX fail horribly
            e.printStackTrace();
            return;
        }

        // notify the client service so it'll start delivery
        try {
            LOG.info("notifying service");
            mActivity.getTalkClientService().messageCreated(message.getMessageTag());
        } catch (RemoteException e) {
            // XXX fail horribly
            e.printStackTrace();
            return;
        }

        // clear the composer UI to prepare it for the next message
        clearComposedMessage();
    }

}
