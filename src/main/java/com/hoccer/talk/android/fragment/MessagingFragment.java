package com.hoccer.talk.android.fragment;

import java.sql.SQLException;
import java.util.UUID;

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
import com.hoccer.talk.android.ITalkActivity;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.android.database.AndroidTalkDatabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.model.TalkDelivery;
import com.hoccer.talk.model.TalkMessage;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import org.apache.log4j.Logger;

/**
 * Fragment for conversations
 */
public class MessagingFragment extends TalkFragment
        implements View.OnClickListener, SearchView.OnQueryTextListener {

	private static final Logger LOG = Logger.getLogger(MessagingFragment.class);

	ListView mMessageList;

    EditText mTextEdit;
    Button mSendButton;
    Button mAttachButton;

    TalkClientContact mContact;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LOG.info("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.fragment_messaging, container, false);

		mMessageList = (ListView)v.findViewById(R.id.messaging_message_list);

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
        super.onCreateOptionsMenu(menu, inflater);

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
    public void onResume() {
        super.onResume();
        LOG.info("onResume()");
        // do this late so activity has database initialized
        //mMessageList.setAdapter(getTalkActivity().makeMessageListAdapter());
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

    public void converseWithContact(TalkClientContact contact) {
        LOG.info("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
    }

    private void clearComposedMessage() {
        mTextEdit.setText(null);
    }

    void refreshContact() {
        TalkClientDatabase db = getTalkDatabase();

        if(mContact != null) {
            try {
                mContact = db.findClientContactById(mContact.getClientContactId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendComposedMessage() {
        TalkClientDatabase db = getTalkDatabase();

        if(mContact == null) {
            return;
        }

        // construct message and delivery objects
        final TalkClientMessage clientMessage = new TalkClientMessage();
        final TalkMessage message = new TalkMessage();
        final TalkDelivery delivery = new TalkDelivery();

        final String messageTag = UUID.randomUUID().toString();

        message.setMessageTag(messageTag);
        message.setBody(mTextEdit.getText().toString());

        delivery.setMessageTag(messageTag);
        if(mContact.isGroup()) {
            delivery.setGroupId(mContact.getGroupId());
        }
        if(mContact.isClient()) {
            delivery.setReceiverId(mContact.getClientId());
        }

        clientMessage.setMessageTag(messageTag);
        clientMessage.setContact(mContact);
        clientMessage.setMessage(message);
        clientMessage.setOutgoingDelivery(delivery);

        try {
            db.saveMessage(message);
            db.saveDelivery(delivery);
            db.saveClientMessage(clientMessage);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // log to help debugging
        LOG.info("created message with id " + clientMessage.getClientMessageId() + " and tag " + message.getMessageTag());

        try {
            getTalkService().performDeliveries();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // clear the composer UI to prepare it for the next message
        clearComposedMessage();
    }

}
