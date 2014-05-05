package com.hoccer.xo.android.adapter;

import android.content.Context;
import android.util.SparseArray;
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
    Filter mFilter = null;
    private Logger LOG = null;

    private List<TalkClientContact> mNearbyContacts = new ArrayList<TalkClientContact>();

    public NearbyContactsAdapter(XoClientDatabase db, XoActivity xoActivity) {
        super();
        mDatabase = db;
        mXoActivity = xoActivity;
        LOG = Logger.getLogger(getClass());

        this.setFilter(new NearbyContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
                if (contact.isGroup()) {
                    if (contact.isGroupInvolved() && contact.isGroupExisting() && contact.getGroupPresence().isTypeNearby()) {
                        return true;
                    }
                } else if (!contact.isDeleted() && contact.isNearby()) {
                    return true;
                }
                return false;
            }
        });
    }

    public Filter getFilter() {
        return mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
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

    public void retrieveDataFromDb() {
        try {
            mNearbyContacts = mDatabase.findAllContacts();
            if(mFilter != null) {
                mNearbyContacts = filter(mNearbyContacts, mFilter);
            }
            for(TalkClientContact contact: mNearbyContacts) {
                if (contact.isGroup()) {
                    mNearbyContacts.set(0, contact);
                }
                TalkClientDownload avatarDownload = contact.getAvatarDownload();
                if(avatarDownload != null) {
                    mDatabase.refreshClientDownload(avatarDownload);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void updateContact(final View view, final TalkClientContact contact) {
        TextView nameView = ViewHolderForAdapters.get(view, R.id.contact_name);
        AvatarView avatarView = ViewHolderForAdapters.get(view, R.id.contact_icon);
        TextView typeView = ViewHolderForAdapters.get(view, R.id.contact_type);


        nameView.setText(contact.getName());
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

    private void updateAdapter() {
        mXoActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    retrieveDataFromDb();
                }
                notifyDataSetChanged();
            }
        });
    }

    public interface Filter {
        public boolean shouldShow(TalkClientContact contact);
    }

    private List<TalkClientContact> filter(List<TalkClientContact> in, Filter filter) {
        ArrayList<TalkClientContact> res = new ArrayList<TalkClientContact>();
        for(TalkClientContact contact: in) {
            if(filter.shouldShow(contact)) {
                res.add(contact);
            }
        }
        return res;
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        updateAdapter();
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        updateAdapter();
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        updateAdapter();
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        updateAdapter();
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        updateAdapter();
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        updateAdapter();
    }

    @Override
    public void onMessageAdded(TalkClientMessage message) {
        updateAdapter();
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
            updateAdapter();
        }
    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {

    }

    @Override
    public void onUploadStarted(TalkClientUpload upload) {
        if(upload.isAvatar()) {
            updateAdapter();
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