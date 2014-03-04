package com.hoccer.xo.android.adapter;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Contacts adapter for the main contact list
 *
 * This displays detailed contact status with various nooks and crannies.
 *
 */
public class RichContactsAdapter extends ContactsAdapter {

    public RichContactsAdapter(XoActivity activity) {
        super(activity);
        setShowTokens(true);
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
        return R.layout.item_contact_smsinvite;
    }

    @Override
    protected void updateToken(View view, TalkClientSmsToken token) {
        LOG.debug("updateToken(" + token.getSmsTokenId() + ")");
        ContentResolver resolver = mActivity.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(token.getSender()));

        String name = token.getSender();
        String photo = "drawable://" + R.drawable.avatar_default_contact;

        Cursor cursor = resolver.query(uri,
                new String[] {
                   ContactsContract.PhoneLookup.DISPLAY_NAME,
                   ContactsContract.PhoneLookup.PHOTO_URI,
                },
                null, null, null);

        if(cursor != null && cursor.getCount() > 0) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
            int photoIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI);
            cursor.moveToFirst();
            name = cursor.getString(nameIndex);
            String contactPhoto = cursor.getString(photoIndex);
            if(contactPhoto != null) {
                photo = contactPhoto;
            }
        }

        TextView nameText = (TextView)view.findViewById(R.id.smsinvite_name);
        nameText.setText(name);
        ImageView photoImage = (ImageView)view.findViewById(R.id.smsinvite_icon);
        ImageLoader.getInstance().displayImage(photo, photoImage);
    }

    protected void updateContact(final View view, final TalkClientContact contact) {
        LOG.debug("updateContact(" + contact.getClientContactId() + ")");
        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        nameView.setText(contact.getName());
        TextView typeView = (TextView) view.findViewById(R.id.contact_type);

        if(contact.isClient()) {
            TalkPresence presence = contact.getClientPresence();
            AvatarView avatar = (AvatarView) view.findViewById(R.id.contact_icon);
            avatar.setPresence(presence);
        }
        if(contact.isGroup()) {
            if(contact.isGroupInvited()) {
                typeView.setText("Group Invite"); // XXX i18n
            } else {
                typeView.setText(R.string.common_group);
            }
        }
        String lastMessageTime = "";
        try {
            TalkClientMessage message = mDatabase.findLatestMessageByContactId(contact.getClientContactId());
            if(message != null) {
                Date now = new Date(System.currentTimeMillis());
                Date messageTime = message.getTimestamp();

                SimpleDateFormat sdf = new SimpleDateFormat("EEE. HH:mm");
                lastMessageTime = sdf.format(messageTime);
            }
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }

        TextView lastMessageTimeView = (TextView) view.findViewById(R.id.contact_last_message);
        lastMessageTimeView.setText(lastMessageTime);

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
                if (lastMessage.getAttachmentDownload() != null) {
                    TalkClientDownload attachment = lastMessage.getAttachmentDownload();
                    lastMessageText.setText(chooseAttachmentType(attachment.getMediaType()));
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

        AvatarView avatarView = (AvatarView) view.findViewById(R.id.contact_icon);
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showContactProfile(contact);
            }
        });
        String avatarUri = contact.getAvatarContentUrl();
        if(avatarUri == null) {
            if(contact.isGroup()) {
                avatarUri = "drawable://" + R.drawable.avatar_default_group; //"content://" + R.drawable.avatar_default_group;
            } else {
                avatarUri = "drawable://" + R.drawable.avatar_default_contact; //"content://" + R.drawable.avatar_default_contact;
            }
        }
        avatarView.setAvatarImage(avatarUri);
    }

    private String chooseAttachmentType(String attachmentType) {
        return "Received " + attachmentType + " attachment";
    }

}
