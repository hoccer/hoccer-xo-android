package com.hoccer.xo.android.view.chat;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * This class creates and configures layouts for incoming or outgoing messages.
 */
public class ChatMessageItem {

    protected Logger LOG = Logger.getLogger(getClass());

    protected Context mContext;

    public ChatMessageItem(Context context) {
        super();
        mContext = context;
    }

    /**
     * Creates a new empty message layout.
     *
     * @return a View object containing an empty message layout
     */
    private View createView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.item_chat_message, null);
    }

    /**
     * Configures a given message layout using data from a given TalkClientMessage object.
     *
     * Subtypes will have to overwrite this method to enhance the configuration of the message layout.
     *
     * @param view    The given layout
     * @param message The given TalkClientMessage object
     */
    protected void configureViewForMessage(View view, TalkClientMessage message) {
        AvatarView avatarView = (AvatarView) view.findViewById(R.id.av_message_avatar);
        TextView messageTime = (TextView) view.findViewById(R.id.tv_message_time);
        TextView messageText = (TextView) view.findViewById(R.id.tv_message_text);
        TextView messageName = (TextView) view.findViewById(R.id.tv_message_contact_name);

        // Adjust layout for incoming / outgoing message
        if (message.isIncoming()) {
            if (message.getConversationContact().isGroup()) {
                setAvatar(avatarView, message.getSenderContact());
            } else {
                avatarView.setVisibility(View.GONE);
            }
            messageName.setVisibility(View.VISIBLE);
            messageName.setText(message.getSenderContact().getName());

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

        messageTime.setText(getMessageTimestamp(message));
        messageText.setText(message.getText());
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
     * Returns a new and fully configured View object containing the layout for a given message.
     *
     * @param message The given TalkClientMessage object
     * @return A new View object containing the message layout
     */
    public View getViewForMessage(TalkClientMessage message) {
        View view = createView();
        configureViewForMessage(view, message);
        return view;
    }

    /**
     * Reconfigures a given message layout from a given message.
     *
     * @param view The message layout to reconfigure
     * @param message The given TalkClientMessage object
     * @return The fully reconfigured message layout
     */
    public View recycleViewForMessage(View view, TalkClientMessage message) {
        configureViewForMessage(view, message);
        return view;
    }
}
