package com.hoccer.talk.android.fragment;

import java.sql.SQLException;
import java.util.UUID;

import android.app.SearchManager;
import android.content.Context;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.android.adapter.ConversationAdapter;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentView;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.model.TalkDelivery;
import com.hoccer.talk.model.TalkMessage;
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
    Button mAttachmentSelectButton;
    Button mAttachmentClearButton;

    ContentView mAttachmentView;
    ContentObject mAttachment;

    TalkClientContact mContact;

    ConversationAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LOG.info("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.fragment_messaging, container, false);

		mMessageList = (ListView)v.findViewById(R.id.messaging_message_list);

        mTextEdit = (EditText)v.findViewById(R.id.messaging_composer_text);

        mSendButton = (Button)v.findViewById(R.id.messaging_composer_send);
        mSendButton.setOnClickListener(this);

        mAttachmentSelectButton = (Button)v.findViewById(R.id.messaging_composer_attachment_select);
        mAttachmentSelectButton.setOnClickListener(this);

        mAttachmentClearButton = (Button)v.findViewById(R.id.messaging_composer_attachment_clear);
        mAttachmentClearButton.setOnClickListener(this);

        mAttachmentView = (ContentView)v.findViewById(R.id.messaging_composer_attachment);
        mAttachmentView.setVisibility(View.GONE);

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
        if(mAdapter == null) {
            mAdapter = getTalkActivity().makeConversationAdapter();
            if(mContact != null) {
                mAdapter.converseWithContact(mContact);
            }
        }
        mMessageList.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        if(v == mSendButton) {
            LOG.info("onClick(sendButton)");
            sendComposedMessage();
        }
        if(v == mAttachmentSelectButton) {
            LOG.info("onClick(attachmentSelectButton)");
            getTalkActivity().selectAttachment();
        }
        if(v == mAttachmentClearButton) {
            LOG.info("onClick(attachmentClearButton)");
            clearAttachment();
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

    @Override
    public void onAttachmentSelected(ContentObject contentObject) {
        LOG.info("onAttachmentSelected(" + contentObject.getContentUrl() + ")");
        showAttachment(contentObject);
    }

    public void converseWithContact(TalkClientContact contact) {
        LOG.info("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
        if(mAdapter != null) {
            mAdapter.converseWithContact(contact);
        }
    }

    private void clearComposedMessage() {
        LOG.info("clearComposedMessage()");
        mTextEdit.setText(null);
        clearAttachment();
    }

    private void showAttachment(ContentObject contentObject) {
        LOG.info("showAttachment(" + contentObject.getContentUrl() + ")");
        mAttachment = contentObject;
        mAttachmentView.displayContent(getTalkActivity(), contentObject);
        mAttachmentView.setVisibility(View.VISIBLE);
        mAttachmentClearButton.setVisibility(View.VISIBLE);
    }

    private void clearAttachment() {
        LOG.info("clearAttachment()");
        mAttachmentView.clear();
        mAttachmentView.setVisibility(View.GONE);
        mAttachmentClearButton.setVisibility(View.GONE);
        mAttachment = null;
    }

    private void sendComposedMessage() {
        TalkClientDatabase db = getTalkDatabase();

        if(mContact == null) {
            return;
        }

        String messageText = mTextEdit.getText().toString();

        // construct message and delivery objects
        final TalkClientMessage clientMessage = new TalkClientMessage();
        final TalkMessage message = new TalkMessage();
        final TalkDelivery delivery = new TalkDelivery();

        final String messageTag = UUID.randomUUID().toString();

        message.setMessageTag(messageTag);
        message.setBody(messageText);

        delivery.setMessageTag(messageTag);
        if(mContact.isGroup()) {
            delivery.setGroupId(mContact.getGroupId());
        }
        if(mContact.isClient()) {
            delivery.setReceiverId(mContact.getClientId());
        }

        clientMessage.setSeen(true);
        clientMessage.setText(messageText);
        clientMessage.setMessageTag(messageTag);
        clientMessage.setConversationContact(mContact);
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

        // reload the message list
        mAdapter.reload();
    }

}
