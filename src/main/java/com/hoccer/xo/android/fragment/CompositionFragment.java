package com.hoccer.xo.android.fragment;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class CompositionFragment extends XoFragment implements View.OnClickListener,
        View.OnLongClickListener {

    private EditText mTextEdit;

    private TextWatcher mTextWatcher;

    private ImageButton mSendButton;

    private IContentObject mAttachment;

    private TalkClientContact mContact;

    private String mLastMessage = null;

    private ImageButton mAddAttachmentButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_composition, container, false);

        setHasOptionsMenu(true);

        mTextEdit = (EditText) v.findViewById(R.id.messaging_composer_text);
        mTextEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (isComposed()) {
                        sendComposedMessage();
                    }
                }
                return false;
            }
        });

        mSendButton = (ImageButton) v.findViewById(R.id.btn_messaging_composer_send);
        mSendButton.setEnabled(false || XoConfiguration.DEVELOPMENT_MODE_ENABLED);
        mSendButton.setOnClickListener(this);
        if (XoConfiguration.DEVELOPMENT_MODE_ENABLED) {
            mSendButton.setOnLongClickListener(this);
            mSendButton.setLongClickable(true);
        }

        mAddAttachmentButton = (ImageButton) v
                .findViewById(R.id.btn_messaging_composer_add_attachment);
        mAddAttachmentButton.setOnClickListener(new AddAttachmentOnClickListener());

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_composition, menu);
    }

    @Override
    public void onResume() {
        super.onResume();

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
                mSendButton.setEnabled(enable || XoConfiguration.DEVELOPMENT_MODE_ENABLED);
            }
        };
        mTextEdit.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTextWatcher != null) {
            mTextEdit.removeTextChangedListener(mTextWatcher);
            mTextWatcher = null;
        }
    }

    @Override
    public void onClick(View v) {
        sendComposedMessage();
    }

    @Override
    public void onAttachmentSelected(IContentObject contentObject) {
        LOG.debug("onAttachmentSelected(" + contentObject.getContentDataUrl() + ")");
        showAttachment(contentObject);
        mSendButton.setEnabled(isComposed());
    }

    private void showAttachment(IContentObject contentObject) {
        mAddAttachmentButton.setOnClickListener(new AttachmentOnClickListener());
        mAttachment = contentObject;
        String mediaType = contentObject.getContentMediaType();

        int imageResource = -1;
        if(mediaType != null) {
            if(mediaType.equals("image")) {
                imageResource = R.drawable.ic_dark_image;
            } else if(mediaType.equals("video")) {
                imageResource = R.drawable.ic_dark_video;
            } else if(mediaType.equals("vcard")) {
                imageResource = R.drawable.ic_dark_contact;
            } else if(mediaType.equals("geolocation")) {
                imageResource = R.drawable.ic_dark_location;
            } else if(mediaType.equals("data")) {
                imageResource = R.drawable.ic_dark_data;
            } else if(mediaType.equals("audio")) {
                imageResource = R.drawable.ic_dark_music;
            }
        } else {
            imageResource = android.R.drawable.stat_notify_error;
        }
        mAddAttachmentButton.setImageResource(imageResource);
    }

    public void converseWithContact(TalkClientContact contact) {
        LOG.debug("converseWithContact(" + contact.getClientContactId() + ")");
        mContact = contact;
    }

    private boolean isComposed() {
        return mTextEdit.getText().length() > 0 || mAttachment != null;
    }

    private void clearComposedMessage() {
        mTextEdit.setText(null);
        mSendButton.setEnabled(false || XoConfiguration.DEVELOPMENT_MODE_ENABLED);
        clearAttachment();
    }

    private void clearAttachment() {
        mAddAttachmentButton.setOnClickListener(new AddAttachmentOnClickListener());
        mAddAttachmentButton.setImageResource(R.drawable.ic_light_content_attachment);
        mAttachment = null;
    }

    private void sendComposedMessage() {
        if (mContact == null) {
            return;
        }

        String messageText = mTextEdit.getText().toString();

        if (messageText != null && !messageText.equals("")) {
            mLastMessage = messageText;
        }

        TalkClientUpload upload = null;
        if (mAttachment != null) {
            upload = SelectedContent.createAttachmentUpload(mAttachment);
        }
        getXoClient()
                .requestDelivery(getXoClient().composeClientMessage(mContact, messageText, upload));
        clearComposedMessage();
    }

    @Override
    public boolean onLongClick(View v) {
        boolean longpressHandled = false;
        if (mLastMessage != null && !mLastMessage.equals("")) {
            for (int i = 0; i < 15; i++) {
                getXoClient().requestDelivery(getXoClient()
                        .composeClientMessage(mContact, mLastMessage + " " + Integer.toString(i)));
            }
            longpressHandled = true;
            clearComposedMessage();
        }
        return longpressHandled;
    }

    private class AddAttachmentOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            getXoActivity().selectAttachment();
        }
    }

    private class AttachmentOnClickListener implements View.OnClickListener, DialogInterface.OnClickListener {

        @Override
        public void onClick(View v) {
            // start dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getXoActivity());
            builder.setTitle(R.string.dialog_attachment_title);
            builder.setItems(R.array.dialog_attachment_choose, this);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case 0:
                    mAttachment = null;
                    getXoActivity().selectAttachment();
                    break;
                case 1:
                    clearAttachment();
                    break;
            }
        }
    }
}
