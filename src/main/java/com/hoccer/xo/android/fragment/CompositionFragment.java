package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.xo.android.XoFragment;
import com.hoccer.xo.release.R;
import com.hoccer.xo.android.content.ContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.model.TalkDelivery;
import com.hoccer.talk.model.TalkMessage;

import java.sql.SQLException;
import java.util.UUID;

public class CompositionFragment extends XoFragment implements View.OnClickListener {

    EditText mTextEdit;
    TextWatcher mTextWatcher;

    ImageButton mSendButton;
    ImageButton mClearButton;

    ImageButton mAttachmentSelectButton;

    ContentView mAttachmentView;
    ContentObject mAttachment;

    TalkClientContact mContact;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_composition, container, false);

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
        int displayHeight = getResources().getDisplayMetrics().heightPixels;
        mAttachmentView.setMaxContentHeight(Math.round(displayHeight * 0.3f)); // XXX better place
        mAttachmentView.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

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
            getXoActivity().selectAttachment();
        }
        if(v == mClearButton) {
            LOG.debug("onClick(attachmentClearButton)");
            clearComposedMessage();
        }
    }

    @Override
    public void onAttachmentSelected(ContentObject contentObject) {
        LOG.debug("onAttachmentSelected(" + contentObject.getContentUrl() + ")");
        showAttachment(contentObject);
    }

    public void converseWithContact(TalkClientContact contact) {
        LOG.debug("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
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
        mAttachmentView.displayContent(getXoActivity(), contentObject);
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
        XoClientDatabase db = getXoDatabase();

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
            LOG.error("sql error", e);
        }

        // log to help debugging
        LOG.info("created message with id " + clientMessage.getClientMessageId() + " and tag " + message.getMessageTag());

        try {
            getXoService().performDeliveries();
        } catch (RemoteException e) {
            LOG.error("remote error", e);
        }

        // clear the composer UI to prepare it for the next message
        clearComposedMessage();
    }

}
