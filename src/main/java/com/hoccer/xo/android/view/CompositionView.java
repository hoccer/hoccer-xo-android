package com.hoccer.xo.android.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.gesture.Gestures;
import com.hoccer.xo.android.gesture.MotionGestureListener;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class CompositionView extends LinearLayout implements View.OnClickListener,
        View.OnLongClickListener, MotionGestureListener {

    private EditText mTextEdit;

    private TextWatcher mTextWatcher;

    private ImageButton mSendButton;

    private IContentObject mAttachment;

    private TalkClientContact mContact;

    private String mLastMessage = null;

    private ImageButton mAddAttachmentButton;

    private Context mContext = null;

    protected Logger LOG = Logger.getLogger(getClass());

    public interface ICompositionViewListener {
        public void onAddAttachmentClicked();

        public void onAttachmentClicked();
    }

    private ICompositionViewListener mCompositionViewListener;

    public CompositionView(Context context, AttributeSet attSet) {
        super(context, attSet);
        mContext = context;
        initialize();
    }

    public void showAttachment(IContentObject contentObject) {
        mAddAttachmentButton.setOnClickListener(new AttachmentOnClickListener());
        mAttachment = contentObject;
        String mediaType = contentObject.getContentMediaType();

        int imageResource = -1;
        if (mediaType != null) {
            if (mediaType.equals("image")) {
                imageResource = R.drawable.ic_dark_image;
            } else if (mediaType.equals("video")) {
                imageResource = R.drawable.ic_dark_video;
            } else if (mediaType.equals("vcard")) {
                imageResource = R.drawable.ic_dark_contact;
            } else if (mediaType.equals("geolocation")) {
                imageResource = R.drawable.ic_dark_location;
            } else if (mediaType.equals("data")) {
                imageResource = R.drawable.ic_dark_data;
            } else if (mediaType.equals("audio")) {
                imageResource = R.drawable.ic_dark_video;
            }
        } else {
            imageResource = android.R.drawable.stat_notify_error;
        }
        mAddAttachmentButton.setImageResource(imageResource);

        mSendButton.setEnabled(isComposed());
    }

    public void setContact(TalkClientContact contact) {
        mContact = contact;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mTextWatcher != null) {
            mTextEdit.removeTextChangedListener(mTextWatcher);
            mTextWatcher = null;
        }
    }

    @Override
    public void onMotionGesture(int pType) {
        String gestureName = Gestures.GESTURE_NAMES.get(pType);
        LOG.debug("Received gesture of type: " + gestureName);

        if (isComposed()) {
            XoApplication.getXoSoundPool().playThrowSound();
            sendComposedMessage();
        }
    }

    @Override
    public void onClick(View v) {
        sendComposedMessage();
    }

    private boolean isComposed() {
        return mTextEdit.getText().length() > 0 || mAttachment != null;
    }

    @Override
    public boolean onLongClick(View v) {
        boolean longpressHandled = false;
        if (mLastMessage != null && !mLastMessage.equals("")) {
            for (int i = 0; i < 15; i++) {
                XoApplication.getXoClient().requestDelivery(XoApplication.getXoClient()
                        .composeClientMessage(mContact, mLastMessage + " " + Integer.toString(i)));
            }
            longpressHandled = true;
            clearComposedMessage();
        }
        return longpressHandled;
    }

    public void onAttachmentSelected(IContentObject contentObject) {
        LOG.debug("onAttachmentSelected(" + contentObject.getContentDataUrl() + ")");
        showAttachment(contentObject);
        mSendButton.setEnabled(isComposed());
    }

    public void setCompositionViewListener(ICompositionViewListener compositionViewListener) {
        this.mCompositionViewListener = compositionViewListener;
    }

    private void initialize() {

        addView(inflate(mContext, R.layout.view_composition, null));

        mTextEdit = (EditText) findViewById(R.id.messaging_composer_text);
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

        mSendButton = (ImageButton) findViewById(R.id.btn_messaging_composer_send);
        mSendButton.setEnabled(false || XoConfiguration.DEVELOPMENT_MODE_ENABLED);
        mSendButton.setOnClickListener(this);
        if (XoConfiguration.DEVELOPMENT_MODE_ENABLED) {
            mSendButton.setOnLongClickListener(this);
            mSendButton.setLongClickable(true);
        }

        mAddAttachmentButton = (ImageButton) findViewById(R.id.btn_messaging_composer_add_attachment);
        mAddAttachmentButton.setOnClickListener(new AddAttachmentOnClickListener());

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

    private boolean isSendMessagePossible() {
        return !(mContact.isGroup() && mContact.getGroupMemberships().size() == 1);
    }

    private void sendComposedMessage() {
        if (mContact == null) {
            return;
        }

        if (!isSendMessagePossible()) {
            showAlertSendMessageNotPossible();
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
        XoApplication.getXoClient()
                .requestDelivery(XoApplication.getXoClient().composeClientMessage(mContact, messageText, upload));
        clearComposedMessage();
    }

    private void showAlertSendMessageNotPossible() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.composition_alert_empty_group_title);
        builder.setMessage(R.string.composition_alert_empty_group_text);
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private class AddAttachmentOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mCompositionViewListener.onAddAttachmentClicked();
        }
    }

    private class AttachmentOnClickListener implements View.OnClickListener, DialogInterface.OnClickListener {

        @Override
        public void onClick(View v) {

            // start dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

            switch (which) {
                case 0:
                    mAttachment = null;
                    mCompositionViewListener.onAttachmentClicked();
                    break;
                case 1:
                    clearAttachment();
                    break;
            }
        }
    }
}
