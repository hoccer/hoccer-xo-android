package com.hoccer.xo.android.fragment;

import android.os.Bundle;
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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkDelivery;
import com.hoccer.talk.model.TalkMessage;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

import java.sql.SQLException;
import java.util.UUID;

public class CompositionFragment extends XoFragment implements View.OnClickListener {

    Menu mMenu;

    EditText mTextEdit;
    TextWatcher mTextWatcher;

    ImageButton mSendButton;
    ImageButton mClearButton;

    IContentObject mAttachment;
    ContentView mAttachmentView;

    TalkClientContact mContact;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_composition, container, false);

        setHasOptionsMenu(true);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.debug("onCreateOptionsMenu()");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_composition, menu);
        mMenu = menu;
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
        if(v == mClearButton) {
            LOG.debug("onClick(clearButton)");
            clearComposedMessage();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean res = super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_attachment) {
            LOG.debug("onOptionsItemSelected(add_attachment)");
            getXoActivity().selectAttachment();
        }
        return res;
    }

    @Override
    public void onAttachmentSelected(IContentObject contentObject) {
        LOG.debug("onAttachmentSelected(" + contentObject.getContentDataUrl() + ")");
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

    private void showAttachment(IContentObject contentObject) {
        LOG.debug("showAttachment(" + contentObject.getContentDataUrl() + ")");
        mAttachment = contentObject;
        mAttachmentView.displayContent(getXoActivity(), contentObject);
        mAttachmentView.setVisibility(View.VISIBLE);
        mSendButton.setEnabled(isComposed());
        mClearButton.setVisibility(isComposed() ? View.VISIBLE : View.GONE);
        mMenu.findItem(R.id.menu_attachment).setVisible(false);
    }

    private void clearAttachment() {
        LOG.debug("clearAttachment()");
        mAttachmentView.clear();
        mAttachmentView.setVisibility(View.GONE);
        mMenu.findItem(R.id.menu_attachment).setVisible(true);
        mAttachment = null;
    }

    private void sendComposedMessage() {
        LOG.debug("sendComposedMessage()");
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

        clientMessage.markAsSeen();
        clientMessage.setText(messageText);
        clientMessage.setMessageTag(messageTag);
        clientMessage.setConversationContact(mContact);
        clientMessage.setSenderContact(getXoClient().getSelfContact());
        clientMessage.setMessage(message);
        clientMessage.setOutgoingDelivery(delivery);

        TalkClientUpload upload = null;
        if(mAttachment != null) {
            upload = SelectedContent.createAttachmentUpload(mAttachment);
            clientMessage.setAttachmentUpload(upload);
        }

        try {
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

        // request delivery from the client
        getXoClient().requestDelivery(clientMessage);

        // clear the composer UI to prepare it for the next message
        clearComposedMessage();
    }

}
