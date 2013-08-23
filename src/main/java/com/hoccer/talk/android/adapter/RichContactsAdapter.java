package com.hoccer.talk.android.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.model.TalkPresence;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

public class RichContactsAdapter extends ContactsAdapter {

    private static final Logger LOG = Logger.getLogger(RichContactsAdapter.class);

    public RichContactsAdapter(TalkActivity activity) {
        super(activity);
    }

    @Override
    protected int getClientLayout() {
        return R.layout.item_contact_client;
    }

    @Override
    protected int getGroupLayout() {
        return R.layout.item_contact_group;
    }

    @Override
    protected int getSeparatorLayout() {
        return R.layout.item_contact_separator;
    }

    protected void updateContact(final View view, final TalkClientContact contact) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showContactConversation(contact);
            }
        });

        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        nameView.setText(contact.getName());

        if(contact.isClient()) {
            TalkPresence presence = contact.getClientPresence();
            TextView connectedView = (TextView) view.findViewById(R.id.contact_connected);
            if(presence != null && presence.getConnectionStatus().equals("online")) {
                connectedView.setVisibility(View.VISIBLE);
            } else {
                connectedView.setVisibility(View.GONE);
            }
        }

        long unseenMessages = 0;
        try {
            unseenMessages = mDatabase.findUnseenMessageCountByContactId(contact.getClientContactId());
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
        TextView unseenView = (TextView) view.findViewById(R.id.contact_unseen_messages);
        if(unseenMessages > 0) {
            unseenView.setText(Long.toString(unseenMessages));
            unseenView.setVisibility(View.VISIBLE);
        } else {
            unseenView.setVisibility(View.GONE);
        }

        TextView lastMessageText = (TextView) view.findViewById(R.id.contact_last_message);
        try {
            TalkClientMessage lastMessage = mDatabase.findLatestMessageByContactId(contact.getClientContactId());
            if(lastMessage != null) {
                lastMessageText.setVisibility(View.VISIBLE);
                lastMessageText.setText(lastMessage.getText());
            } else {
                lastMessageText.setVisibility(View.GONE);
            }
        } catch (SQLException e) {
            LOG.error("sql error", e);
            lastMessageText.setVisibility(View.GONE);
        }

        TalkClientDownload avatarDownload = contact.getAvatarDownload();
        ImageView iconView = (ImageView) view.findViewById(R.id.contact_icon);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showContactProfile(contact);
            }
        });
        String avatarUri;
        if(avatarDownload == null || !avatarDownload.getState().equals(TalkClientDownload.State.COMPLETE)) {
            if(contact.isGroup()) {
                avatarUri = "content://" + R.drawable.avatar_default_group;
            } else {
                avatarUri = "content://" + R.drawable.avatar_default_contact;
            }
        } else {
            File avatarFile = TalkApplication.getAvatarLocation(avatarDownload);
            if(avatarFile != null) {
                avatarUri = "file://" + avatarFile.toString();
            } else {
                if(contact.isGroup()) {
                    avatarUri = "content://" + R.drawable.avatar_default_group;
                } else {
                    avatarUri = "content://" + R.drawable.avatar_default_contact;
                }
            }
        }
        ImageLoader.getInstance().displayImage(avatarUri, iconView);
    }

}
