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

import java.util.Date;

/**
 * This class represents a chat message. It layouts a message as incoming or
 * outgoing and displays the message text. To configure the cell only the TalkClientMessage object is needed.
 */
public class ChatMessageItem {

    protected Context mContext;

    public ChatMessageItem(Context context) {
        super();
        mContext = context;

    }

    private View createViewForMessage(TalkClientMessage message) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;

        if (message.isIncoming()) {
            view = inflater.inflate(R.layout.item_conversation_incoming, null);
        } else {
            view = inflater.inflate(R.layout.item_conversation_outgoing, null);
        }
        return view;
    }

    private void configureViewForMessage(View view, TalkClientMessage message) {
        AvatarView avatarView = (AvatarView) view.findViewById(R.id.av_message_avatar);
        TextView messageTime = (TextView) view.findViewById(R.id.tv_message_time);
        TextView messageText = (TextView) view.findViewById(R.id.tv_message_text);
        TextView messageName = (TextView) view.findViewById(R.id.tv_message_contact_name);

        // Only for incoming messages from groups
        if (message.isIncoming()) {
            if (message.getConversationContact().isGroup()) {
                setAvatar(avatarView, message.getSenderContact());
            } else {
                avatarView.setVisibility(View.GONE);
            }
        }

        messageTime.setText(getMessageTimestamp(message));
        messageText.setText(message.getText());

        // Only for incoming messages
        if (message.isIncoming()) {
            messageName.setText(message.getSenderContact().getName());
        }
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
                        ((XoActivity)mContext).showContactProfile(contact);
                    }
                }
            });
        }
    }

    // TODO: configure AvatarView, attachment progress indicator

    public View getViewForMessage(TalkClientMessage message) {
        View view = createViewForMessage(message);

        configureViewForMessage(view, message);

        return view;
    }

    public View recycleViewForMessage(View view, TalkClientMessage message) {
        view = createViewForMessage(message);
        configureViewForMessage(view, message);
        return view;
    }
}
