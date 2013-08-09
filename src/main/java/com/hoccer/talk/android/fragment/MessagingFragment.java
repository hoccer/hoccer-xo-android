package com.hoccer.talk.android.fragment;

import java.sql.SQLException;
import java.util.UUID;

import android.app.SearchManager;
import android.content.Context;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
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
import com.hoccer.talk.android.adapter.ConversationAdapter;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentView;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
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
    TextWatcher mTextWatcher;

    ImageButton mSendButton;
    ImageButton mClearButton;

    ImageButton mAttachmentSelectButton;

    ContentView mAttachmentView;
    ContentObject mAttachment;

    TalkClientContact mContact;

    ConversationAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.fragment_messaging, container, false);

		mMessageList = (ListView)v.findViewById(R.id.messaging_message_list);

        mTextEdit = (EditText)v.findViewById(R.id.messaging_composer_text);
        mTextEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    if(isComposed()) {
                        sendComposedMessage();
                    }
                }
                return false;
            }
        });

        mSendButton = (ImageButton)v.findViewById(R.id.messaging_composer_send);
        mSendButton.setEnabled(false);
        mSendButton.setOnClickListener(this);

        mAttachmentSelectButton = (ImageButton)v.findViewById(R.id.messaging_composer_attachment_select);
        mAttachmentSelectButton.setOnClickListener(this);

        mClearButton = (ImageButton)v.findViewById(R.id.messaging_composer_clear);
        mClearButton.setVisibility(View.GONE);
        mClearButton.setOnClickListener(this);

        mAttachmentView = (ContentView)v.findViewById(R.id.messaging_composer_attachment);
        mAttachmentView.setVisibility(View.GONE);

		return v;
	}

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        // do this late so activity has database initialized
        if(mAdapter == null) {
            mAdapter = getTalkActivity().makeConversationAdapter();
            if(mContact != null) {
                mAdapter.converseWithContact(mContact);
            }
        }
        mMessageList.setAdapter(mAdapter);

        // watch message editor
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                boolean enable = isComposed() || s.toString().length() > 0;
                mSendButton.setEnabled(enable);
                mClearButton.setVisibility(enable ? View.VISIBLE : View.GONE);
            }
        };
        mTextEdit.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();

        // unregister text watcher
        if(mTextWatcher != null) {
            mTextEdit.removeTextChangedListener(mTextWatcher);
            mTextWatcher = null;
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mSendButton) {
            LOG.debug("onClick(sendButton)");
            sendComposedMessage();
        }
        if(v == mAttachmentSelectButton) {
            LOG.debug("onClick(attachmentSelectButton)");
            getTalkActivity().selectAttachment();
        }
        if(v == mClearButton) {
            LOG.debug("onClick(attachmentClearButton)");
            clearComposedMessage();
        }
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

    @Override
    public void onAttachmentSelected(ContentObject contentObject) {
        LOG.debug("onAttachmentSelected(" + contentObject.getContentUrl() + ")");
        showAttachment(contentObject);
    }

    public void converseWithContact(TalkClientContact contact) {
        LOG.debug("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
        if(mAdapter != null) {
            mAdapter.converseWithContact(contact);
        }
    }

    private boolean isComposed() {
        return mTextEdit.getText().length() > 0 || mAttachment != null;
    }

    private void clearComposedMessage() {
        LOG.debug("clearComposedMessage()");
        mTextEdit.setText(null);
        mSendButton.setEnabled(false);
        mClearButton.setVisibility(View.GONE);
        clearAttachment();
    }

    private void showAttachment(ContentObject contentObject) {
        LOG.debug("showAttachment(" + contentObject.getContentUrl() + ")");
        mAttachment = contentObject;
        mAttachmentView.displayContent(getTalkActivity(), contentObject);
        mAttachmentView.setVisibility(View.VISIBLE);
        mAttachmentSelectButton.setVisibility(View.GONE);
        mSendButton.setEnabled(isComposed());
        mClearButton.setVisibility(isComposed() ? View.VISIBLE : View.GONE);
    }

    private void clearAttachment() {
        LOG.debug("clearAttachment()");
        mAttachmentView.clear();
        mAttachmentView.setVisibility(View.GONE);
        mAttachmentSelectButton.setVisibility(View.VISIBLE);
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

        TalkClientUpload upload = null;
        if(mAttachment != null) {
            upload = ContentObject.createAttachmentUpload(mAttachment);
            clientMessage.setAttachmentUpload(upload);
        }

        try {
            clientMessage.setSenderContact(db.findSelfContact(false));
            if(upload != null) {
                db.saveClientUpload(upload);
            }
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
