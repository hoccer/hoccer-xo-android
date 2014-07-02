package com.hoccer.xo.android.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Contacts adapter for the main contact list
 *
 * This displays detailed contact status with various nooks and crannies.
 */
public class RichContactsAdapter extends ContactsAdapter {


    public RichContactsAdapter(XoActivity activity) {
        super(activity);
        setShowTokens(true);
    }

    public RichContactsAdapter(XoActivity activity, boolean showNearbyHistory) {
        super(activity, showNearbyHistory);
    }

    @Override
    protected int getClientLayout() {
        return R.layout.item_contact_client;
    }

    @Override
    protected int getGroupLayout() {
        return R.layout.item_contact_client;
    }

    @Override
    protected int getSeparatorLayout() {
        return R.layout.item_contact_separator;
    }

    @Override
    protected int getTokenLayout() {
        return R.layout.item_contact_sms_invite;
    }

    @Override
    protected int getNearbyHistoryLayout() {
        return R.layout.item_contact_client;
    }

    @Override
    protected void updateNearbyHistoryLayout(View v) {
        TextView titleText = (TextView) v.findViewById(R.id.contact_name);
        TextView timestampText = (TextView) v.findViewById(R.id.contact_time);
        TextView lastMessageText = (TextView) v.findViewById(R.id.contact_last_message);
        AvatarView avatarView = (AvatarView) v.findViewById(R.id.contact_icon);

        try {
            long offset = mDatabase.getNearbyMessageCount() - 1;
            TalkClientMessage lastNearbyMessage = mDatabase.findNearbyMessages(1, offset).get(0);
            timestampText.setText(getTimeString(lastNearbyMessage.getTimestamp()));
            lastMessageText.setText(lastNearbyMessage.getText());
        } catch (SQLException e) {
            LOG.error(e);
        }

        titleText.setText(R.string.nearby_saved);
        avatarView.setAvatarImage(R.drawable.avatar_default_location);
        avatarView.setClickable(false);
    }

    @Override
    protected void updateToken(View view, TalkClientSmsToken token) {
        LOG.debug("updateToken(" + token.getSmsTokenId() + ")");
        ContentResolver resolver = mActivity.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(token.getSender()));

        String name = token.getSender();
        String photo = "drawable://" + R.drawable.avatar_default_contact;

        Cursor cursor = resolver.query(uri,
                new String[]{
                        ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.PhoneLookup.PHOTO_URI,
                },
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
            int photoIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI);
            cursor.moveToFirst();
            name = cursor.getString(nameIndex);
            String contactPhoto = cursor.getString(photoIndex);
            if (contactPhoto != null) {
                photo = contactPhoto;
            }
        }

        TextView nameText = (TextView) view.findViewById(R.id.sms_invite_name);
        nameText.setText(name);
        AvatarView avatarView = (AvatarView) view.findViewById(R.id.sms_invite_icon);
        avatarView.setAvatarImage(photo);
    }

    protected void updateContact(final View view, final TalkClientContact contact) {
        LOG.debug("updateContact(" + contact.getClientContactId() + ")");
        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        AvatarView avatarView = (AvatarView) view.findViewById(R.id.contact_icon);
        nameView.setText(contact.getNickname());
        TextView typeView = (TextView) view.findViewById(R.id.contact_type);

        avatarView.setContact(contact);
        if (contact.isGroup()) {
            if (contact.isGroupInvited()) {
                typeView.setText(R.string.common_group_invite);
            } else {
                typeView.setText(R.string.common_group);
            }
        }
        String lastMessageTime = "";
        try {
            TalkClientMessage message = mDatabase
                    .findLatestMessageByContactId(contact.getClientContactId());
            if (message != null) {
                lastMessageTime = getTimeString(message.getTimestamp());
            }
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }

        TextView lastMessageTimeView = (TextView) view.findViewById(R.id.contact_time);
        lastMessageTimeView.setText(lastMessageTime);

        long unseenMessages = 0;
        try {
            unseenMessages = mDatabase.findUnseenMessageCountByContactId(contact.getClientContactId());
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
        TextView unseenView = (TextView) view.findViewById(R.id.contact_unseen_messages);
        if (unseenMessages > 0) {
            unseenView.setText(Long.toString(unseenMessages));
            unseenView.setVisibility(View.VISIBLE);
        } else {
            unseenView.setVisibility(View.GONE);
        }

        TextView lastMessageText = (TextView) view.findViewById(R.id.contact_last_message);
        try {
            TalkClientMessage lastMessage = mDatabase
                    .findLatestMessageByContactId(contact.getClientContactId());
            if (lastMessage != null) {
                lastMessageText.setVisibility(View.VISIBLE);
                if (lastMessage.getAttachmentDownload() != null) {
                    TalkClientDownload attachment = lastMessage.getAttachmentDownload();
                    lastMessageText.setText(chooseAttachmentType(view.getContext(),
                            attachment.getMediaType()));
                } else {
                    lastMessageText.setText(lastMessage.getText());
                }
            } else {
                lastMessageText.setVisibility(View.GONE);
            }
        } catch (SQLException e) {
            LOG.error("sql error", e);
            lastMessageText.setVisibility(View.GONE);
        }

        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showContactProfile(contact);
            }
        });
    }

    private String getTimeString(Date messageTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm");
        return sdf.format(messageTime);
    }

    private String chooseAttachmentType(Context context, String attachmentType) {
        String text = context.getResources().getString(R.string.contact_item_receive_attachment);
        return String.format(text, attachmentType);
    }

}
