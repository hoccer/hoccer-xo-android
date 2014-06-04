package com.hoccer.xo.android.view.chat;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.hoccer.talk.client.XoTransferAgent;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AttachmentTransferControlView;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.android.view.chat.attachments.AttachmentTransferHandler;
import com.hoccer.xo.android.view.chat.attachments.AttachmentTransferListener;
import com.hoccer.xo.android.view.chat.attachments.ChatItemType;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * This class creates and configures layouts for incoming or outgoing messages.
 */
public class ChatMessageItem implements AttachmentTransferListener {

    protected Logger LOG = Logger.getLogger(getClass());

    protected Context mContext;
    protected AttachmentTransferHandler mAttachmentTransferHandler;
    protected TalkClientMessage mMessage;

    protected TextView mMessageText;
    protected RelativeLayout mContentTransferProgress;
    protected LinearLayout mContentWrapper;
    protected AttachmentTransferControlView mTransferControl;


    public ChatMessageItem(Context context, TalkClientMessage message) {
        super();
        mContext = context;
        mMessage = message;
    }

    public TalkClientMessage getMessage() {
        return mMessage;
    }

    public void setMessage(TalkClientMessage message) {
        mMessage = message;
    }

    /**
     * Returns a new and fully configured View object containing the layout for a given message.
     *
     * @return A new View object containing the message layout
     */
    public View getViewForMessage() {
        View view = createView();
        configureViewForMessage(view);
        return view;
    }

    /**
     * Reconfigures a given message layout from a given message.
     *
     * @param view    The message layout to reconfigure
     * @return The fully reconfigured message layout
     */
    public View recycleViewForMessage(View view) {
        configureViewForMessage(view);
        return view;
    }

    /**
     * Returns the type of message item defined in ChatItemType.
     *
     * Subtypes need to overwrite this method and return the appropriate ChatItemType.
     *
     * @return The ChatItemType of this message item
     */
    public ChatItemType getType() {
        return ChatItemType.ChatItemWithText;
    }

    /**
     * Creates a new empty message layout.
     *
     * @return a View object containing an empty message layout
     */
    private View createView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_chat_message, null);
        return view;
    }

    /**
     * Configures a given message layout using data from a given TalkClientMessage object.
     * <p/>
     * Subtypes will have to overwrite this method to enhance the configuration of the message layout.
     *
     * @param view    The given layout
     */
    protected void configureViewForMessage(View view) {
        AvatarView avatarView = (AvatarView) view.findViewById(R.id.av_message_avatar);
        TextView messageTime = (TextView) view.findViewById(R.id.tv_message_time);
        TextView messageName = (TextView) view.findViewById(R.id.tv_message_contact_name);
        TextView messageText = (TextView) view.findViewById(R.id.tv_message_text);

        // Adjust layout for incoming / outgoing message
                setAvatar(avatarView, mMessage.getSenderContact());
        if (mMessage.isIncoming()) {
            if (mMessage.getConversationContact().isGroup()) {
            } else {
                avatarView.setVisibility(View.GONE);
            }
            messageName.setVisibility(View.VISIBLE);
            messageName.setText(mMessage.getSenderContact().getName());

            messageText.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bubble_grey));
            messageText.setTextColor(mContext.getResources().getColorStateList(android.R.color.black));
            messageText.setLinkTextColor(mContext.getResources().getColorStateList(android.R.color.black));
        } else {
            avatarView.setVisibility(View.GONE);
            messageName.setVisibility(View.GONE);

            messageText.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bubble_green));
            messageText.setTextColor(mContext.getResources().getColorStateList(android.R.color.white));
            messageText.setLinkTextColor(mContext.getResources().getColorStateList(android.R.color.white));
        }

        messageTime.setText(getMessageTimestamp(mMessage));
        messageText.setText(mMessage.getText());

        mMessageText = messageText;
    }

    private String getMessageTimestamp(TalkClientMessage message) {
        String timeStamp = null;
        Date time = message.getTimestamp();
        if (time != null) {

            timeStamp = (String) DateUtils.getRelativeDateTimeString(
                    mContext,
                    time.getTime(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0
            );
        }
        return timeStamp;
    }

    private void setAvatar(AvatarView avatarView, final TalkClientContact sendingContact) {
        avatarView.setContact(sendingContact);
        avatarView.setVisibility(View.VISIBLE);
        if (sendingContact != null) {
            avatarView.setOnClickListener(new View.OnClickListener() {
                final TalkClientContact contact = sendingContact;

                @Override
                public void onClick(View v) {
                    if (!contact.isSelf()) {

                        // TODO: reevaluate - might not work
                        ((XoActivity) mContext).showContactProfile(contact);
                    }
                }
            });
        }
    }

    /**
     * Configures the attachment view for a given message / attachment.
     *
     * Subtypes will have to call this method to trigger the configuration of the attachment layout.
     *
     * @param view    The chat message item's view to configure
     */
    protected void configureAttachmentViewForMessage(View view) {

        RelativeLayout attachmentView = (RelativeLayout) view.findViewById(R.id.rl_message_attachment);

        // add content view
        if (attachmentView.getChildCount() == 0) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View attachmentWrapper = inflater.inflate(R.layout.view_attachment_wrapper, null);
            attachmentView.addView(attachmentWrapper);
        }
        attachmentView.setVisibility(View.VISIBLE);

        mContentWrapper = (LinearLayout) attachmentView.findViewById(R.id.content_wrapper);

        // adjust layout for incoming / outgoing attachment
        if (mMessage.isIncoming()) {
            attachmentView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bubble_grey));
        } else {
            attachmentView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bubble_green));
        }

        // configure transfer progress view
        mContentTransferProgress = (RelativeLayout) attachmentView.findViewById(R.id.content_transfer_progress);
        mTransferControl = (AttachmentTransferControlView) attachmentView.findViewById(R.id.content_transfer_control);
        IContentObject contentObject = mMessage.getAttachmentUpload();
        if (contentObject == null) {
            contentObject = mMessage.getAttachmentDownload();
        }

        if (shouldDisplayTransferControl(getTransferState(contentObject))) {
            mContentWrapper.setVisibility(View.GONE);
            mContentTransferProgress.setVisibility(View.VISIBLE);

            // create handler for a pending attachment transfer
            mAttachmentTransferHandler = new AttachmentTransferHandler(mTransferControl, contentObject, this);
            XoApplication.getXoClient().registerTransferListener(mAttachmentTransferHandler);
            mTransferControl.setOnClickListener(new AttachmentTransferHandler(mTransferControl, contentObject, this));

        } else {
            mTransferControl.setOnClickListener(null);
            displayAttachment(contentObject);
        }

        // hide message text field when empty - there is still an attachment to display
        if (mMessage.getText() == null || mMessage.getText().isEmpty()) {
            mMessageText.setVisibility(View.GONE);
        } else {
            mMessageText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configures the attachment view for a given message / attachment.
     *
     * Subtypes will have to overwrite this method to configure the attachment layout.
     *
     * @param contentObject The IContentObject to display
     */
    protected void displayAttachment(IContentObject contentObject) {
        mContentTransferProgress.setVisibility(View.GONE);
        mContentWrapper.setVisibility(View.VISIBLE);
    }

    /**
     * Returns true when the transfer (upload or download) of the attachment is not completed.
     *
     * @param state
     * @return
     */
    protected boolean shouldDisplayTransferControl(ContentState state) {
        return !(state == ContentState.SELECTED || state == ContentState.UPLOAD_COMPLETE || state == ContentState.DOWNLOAD_COMPLETE);
    }

    protected ContentState getTransferState(IContentObject object) {
        XoTransferAgent agent = XoApplication.getXoClient().getTransferAgent();
        ContentState state = object.getContentState();
        if (object instanceof TalkClientDownload) {
            TalkClientDownload download = (TalkClientDownload) object;
            switch (state) {
                case DOWNLOAD_DOWNLOADING:
                case DOWNLOAD_DECRYPTING:
                case DOWNLOAD_DETECTING:
                    if (agent.isDownloadActive(download)) {
                        return state;
                    } else {
                        return ContentState.DOWNLOAD_PAUSED;
                    }
            }
        }
        if (object instanceof TalkClientUpload) {
            TalkClientUpload upload = (TalkClientUpload) object;
            switch (state) {
                case UPLOAD_REGISTERING:
                case UPLOAD_ENCRYPTING:
                case UPLOAD_UPLOADING:
                    if (agent.isUploadActive(upload)) {
                        return state;
                    } else {
                        return ContentState.UPLOAD_PAUSED;
                    }
            }
        }
        return state;
    }

    @Override
    public void onAttachmentTransferComplete(IContentObject contentObject) {
        displayAttachment(contentObject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessageItem)) return false;

        ChatMessageItem that = (ChatMessageItem) o;

        if (mMessage != null ? !mMessage.equals(that.mMessage) : that.mMessage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mMessage != null ? mMessage.hashCode() : 0;
    }
}
