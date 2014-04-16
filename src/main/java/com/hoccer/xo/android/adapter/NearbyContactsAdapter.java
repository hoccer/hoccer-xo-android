package com.hoccer.xo.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NearbyContactsAdapter extends BaseAdapter implements IXoContactListener, IXoMessageListener, IXoTransferListener {
    private XoClientDatabase mDatabase;
    private XoActivity mXoActivity;
    private Logger LOG = null;

    private List<TalkClientContact> mNearbyContacts = new ArrayList<TalkClientContact>();

    public NearbyContactsAdapter(XoClientDatabase db, XoActivity xoActivity) {
        super();
        mDatabase = db;
        mXoActivity = xoActivity;
        LOG = Logger.getLogger(getClass());
        getNearbyContactsFromDb();
    }

    @Override
    public int getCount() {
        return mNearbyContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return mNearbyContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_client, null);
        }
        updateContact(convertView, (TalkClientContact) getItem(position));
        return convertView;
    }

    public void registerListeners() {
        mXoActivity.getXoClient().registerContactListener(this);
        mXoActivity.getXoClient().registerTransferListener(this);
        mXoActivity.getXoClient().registerMessageListener(this);
    }

    public void unregisterListeners() {
        mXoActivity.getXoClient().unregisterContactListener(this);
        mXoActivity.getXoClient().unregisterTransferListener(this);
        mXoActivity.getXoClient().unregisterMessageListener(this);
    }

    private void getNearbyContactsFromDb() {
        try {
            mNearbyContacts = mDatabase.findAllGroupContacts();//TODO: get nerby group
            List<TalkClientContact> contacts = mDatabase.findAllGroupContacts();//TODO: get contacts from nearby group (sorted)
//            for(TalkClientContact contact: contacts) {
//                TalkClientDownload avatarDownload = contact.getAvatarDownload();
//                if(avatarDownload != null) {
//                    mDatabase.refreshClientDownload(avatarDownload);
//                }
//            }

            for (TalkClientContact t : contacts) {
                mNearbyContacts.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void updateContact(final View view, final TalkClientContact contact) {
        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        AvatarView avatarView = (AvatarView) view.findViewById(R.id.contact_icon);
        nameView.setText(contact.getName());
        TextView typeView = (TextView) view.findViewById(R.id.contact_type);
        avatarView.setContact(contact);
        // TODO: do we have only one type for nearby group?
        if (contact.isGroup()) {
            if (contact.isGroupInvited()) {
                typeView.setText(R.string.common_group_invite);
            } else {
                typeView.setText(R.string.common_group);
            }
        }
        TalkClientMessage message = null;
        long unseenMessages = 0;
        try {
            message = mDatabase.findLatestMessageByContactId(contact.getClientContactId());
            unseenMessages = mDatabase.findUnseenMessageCountByContactId(contact.getClientContactId());
        } catch (SQLException e) {
            LOG.error("NearbyContactsAdapter: SQL error", e);
        }
        if (message != null) {
            Date messageTime = message.getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm");
            String lastMessageTime = sdf.format(messageTime);
            TextView lastMessageTimeView = (TextView) view.findViewById(R.id.contact_time);
            TextView lastMessageText = (TextView) view.findViewById(R.id.contact_last_message);
            lastMessageTimeView.setText(lastMessageTime);
            if (message.getAttachmentDownload() != null) {
                TalkClientDownload attachment = message.getAttachmentDownload();
                lastMessageText.setText(chooseAttachmentType(view.getContext(),
                        attachment.getMediaType()));
            } else {
                lastMessageText.setText(message.getText());
            }
        }
        TextView unseenView = (TextView) view.findViewById(R.id.contact_unseen_messages);
        if (unseenMessages > 0) {
            unseenView.setText(Long.toString(unseenMessages));
            unseenView.setVisibility(View.VISIBLE);
        } else {
            unseenView.setVisibility(View.GONE);
        }
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mXoActivity.showContactProfile(contact);
            }
        });
    }

    private String chooseAttachmentType(Context context, String attachmentType) {
        String text = context.getResources().getString(R.string.contact_item_receive_attachment);
        return String.format(text, attachmentType);
    }



    public void reloadAdapter() {
        synchronized (this) {
            getNearbyContactsFromDb();
        }
        mXoActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetInvalidated();
            }
        });
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        reloadAdapter();
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        reloadAdapter();
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        reloadAdapter();
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        reloadAdapter();
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        reloadAdapter();
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        reloadAdapter();
    }

    @Override
    public void onMessageAdded(TalkClientMessage message) {
        reloadAdapter();
    }

    @Override
    public void onMessageRemoved(TalkClientMessage message) {

    }

    @Override
    public void onMessageStateChanged(TalkClientMessage message) {

    }

    @Override
    public void onDownloadRegistered(TalkClientDownload download) {

    }

    @Override
    public void onDownloadStarted(TalkClientDownload download) {

    }

    @Override
    public void onDownloadProgress(TalkClientDownload download) {

    }

    @Override
    public void onDownloadFinished(TalkClientDownload download) {
        if(download.isAvatar()) {
            reloadAdapter();
        }
    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {

    }

    @Override
    public void onUploadStarted(TalkClientUpload upload) {
        if(upload.isAvatar()) {
            reloadAdapter();
        }
    }

    @Override
    public void onUploadProgress(TalkClientUpload upload) {

    }

    @Override
    public void onUploadFinished(TalkClientUpload upload) {

    }

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {

    }
}
