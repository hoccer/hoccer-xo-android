package com.hoccer.talk.android.adapter;

import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.TalkAdapter;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.model.TalkPresence;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends TalkAdapter {

    private static final Logger LOG = Logger.getLogger(ContactsAdapter.class);

    protected final static long ITEM_ID_UNKNOWN = -1000;
    protected final static long ITEM_ID_CLIENT_HEADER = -1;
    protected final static long ITEM_ID_GROUP_HEADER = -2;

    protected final static int VIEW_TYPE_SEPARATOR = 0;
    protected final static int VIEW_TYPE_CLIENT    = 1;
    protected final static int VIEW_TYPE_GROUP     = 2;

    protected final static int VIEW_TYPE_COUNT = 3;

    public ContactsAdapter(TalkActivity activity) {
        super(activity);
    }

    Filter mFilter = null;

    List<TalkClientContact> mClientContacts = new ArrayList<TalkClientContact>();
    List<TalkClientContact> mGroupContacts = new ArrayList<TalkClientContact>();

    public Filter getFilter() {
        return mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
    }

    @Override
    public void reload() {
        try {
            mClientContacts = mDatabase.findAllClientContacts();
            mGroupContacts = mDatabase.findAllGroupContacts();

            if(mFilter != null) {
                mClientContacts = filter(mClientContacts, mFilter);
                mGroupContacts = filter(mGroupContacts, mFilter);
            }

            for(TalkClientContact contact: mClientContacts) {
                TalkClientDownload avatarDownload = contact.getAvatarDownload();
                if(avatarDownload != null) {
                    mDatabase.refreshClientDownload(avatarDownload);
                }
            }
            for(TalkClientContact contact: mGroupContacts) {
                TalkClientDownload avatarDownload = contact.getAvatarDownload();
                if(avatarDownload != null) {
                    mDatabase.refreshClientDownload(avatarDownload);
                }
            }
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetInvalidated();
            }
        });
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
    public void onContactAdded(int contactId) throws RemoteException {
        try {
            TalkClientContact contact = mDatabase.findClientContactById(contactId);
            if(contact.isClient()) {
                mClientContacts.add(contact);
            }
            if(contact.isGroup()) {
                mGroupContacts.add(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onContactRemoved(int contactId) throws RemoteException {
        LOG.info("onContactRemoved(" + contactId + ")");
        reload();
    }

    @Override
    public void onClientPresenceChanged(int contactId) throws RemoteException {
        LOG.info("onClientPresenceChanged(" + contactId + ")");
        reload();
    }

    @Override
    public void onClientRelationshipChanged(int contactId) throws RemoteException {
        LOG.info("onClientRelationshipChanged(" + contactId + ")");
        reload();
    }

    @Override
    public void onGroupPresenceChanged(int contactId) throws RemoteException {
        LOG.info("onGroupPresenceChanged(" + contactId + ")");
        reload();
    }

    @Override
    public void onGroupMembershipChanged(int contactId) throws RemoteException {
        LOG.info("onGroupMembershipChanged(" + contactId + ")");
        reload();
    }

    @Override
    public void onDownloadRemoved(int contactId, int downloadId) throws RemoteException {
        LOG.info("onDownloadRemoved(" + contactId + "," + downloadId + ")");
        reload();
    }

    @Override
    public void onMessageAdded(int contactId, int messageId) throws RemoteException {
        LOG.info("onMessageAdded(" + contactId + "," + messageId + ")");
        reload();
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        int count = 0;

        count += mClientContacts.size();
        count += mGroupContacts.size();

        if(!mClientContacts.isEmpty()) {
            //count += 1;
        }
        if(!mGroupContacts.isEmpty()) {
            //count += 1;
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        int offset = 0;
        if(!mClientContacts.isEmpty()) {
//            if(position == offset) {
//                return mResources.getString(R.string.contacts_category_friends);
//            }
//            offset += 1;
            int clientPos = position - offset;
            if(clientPos >= 0 && clientPos < mClientContacts.size()) {
                return mClientContacts.get(clientPos);
            }
            offset += mClientContacts.size();
        }
        if(!mGroupContacts.isEmpty()) {
//            if(position == offset) {
//                return mResources.getString(R.string.contacts_category_groups);
//            }
//            offset += 1;
            int groupPos = position - offset;
            if(groupPos >= 0 && groupPos < mGroupContacts.size()) {
                return mGroupContacts.get(groupPos);
            }
            offset += mGroupContacts.size();
        }
        return "";
    }

    @Override
    public int getItemViewType(int position) {
        int offset = 0;
        if(!mClientContacts.isEmpty()) {
//            if(position == offset) {
//                return VIEW_TYPE_SEPARATOR;
//            }
//            offset += 1;
            int clientPos = position - offset;
            if(clientPos >= 0 && clientPos < mClientContacts.size()) {
                return VIEW_TYPE_CLIENT;
            }
            offset += mClientContacts.size();
        }
        if(!mGroupContacts.isEmpty()) {
//            if(position == offset) {
//                return VIEW_TYPE_SEPARATOR;
//            }
//            offset += 1;
            int groupPos = position - offset;
            if(groupPos >= 0 && groupPos < mGroupContacts.size()) {
                return VIEW_TYPE_GROUP;
            }
            offset += mGroupContacts.size();
        }
        return VIEW_TYPE_SEPARATOR;
    }

    @Override
    public long getItemId(int position) {
        Object item = getItem(position);
        if(item instanceof TalkClientContact) {
            return ((TalkClientContact)item).getClientContactId();
        }

        int offset = 0;
        if(!mClientContacts.isEmpty()) {
//            if(position == offset) {
//                return ITEM_ID_CLIENT_HEADER;
//            }
//            offset += 1;
            offset += mClientContacts.size();
        }
        if(!mGroupContacts.isEmpty()) {
//            if(position == offset) {
//                return ITEM_ID_GROUP_HEADER;
//            }
//            offset += 1;
            offset += mGroupContacts.size();
        }
        return ITEM_ID_UNKNOWN;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);

        View v = convertView;

        switch(type) {
        case VIEW_TYPE_CLIENT:
            if(v == null) {
                v = mInflater.inflate(R.layout.item_contact_client, null);
            }
            updateContact(v, (TalkClientContact)getItem(position));
            updateClientContact(v, (TalkClientContact)getItem(position));
            break;
        case VIEW_TYPE_GROUP:
            if(v == null) {
                v = mInflater.inflate(R.layout.item_contact_group, null);
            }
            updateContact(v, (TalkClientContact)getItem(position));
            break;
        case VIEW_TYPE_SEPARATOR:
            if(v == null) {
                v = mInflater.inflate(R.layout.item_contact_separator, null);
            }
            updateSeparator(v, position);
            break;
        default:
            v = mInflater.inflate(R.layout.item_contact_separator, null);
            break;
        }

        return v;
    }

    private void updateSeparator(View view, int position) {
        TextView separator = (TextView)view;
        separator.setText((String)getItem(position));
    }

    private void updateContact(final View view, final TalkClientContact contact) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showContactConversation(contact);
            }
        });

        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        nameView.setText(contact.getName());

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

//        ImageButton profileButton = (ImageButton) view.findViewById(R.id.contact_profile_button);
//        profileButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mActivity.showContactProfile(contact);
//            }
//        });

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

    private void updateClientContact(final View view, final TalkClientContact contact) {
        TalkPresence presence = contact.getClientPresence();
        TextView connectedView = (TextView) view.findViewById(R.id.contact_connected);
        if(presence != null && presence.getConnectionStatus().equals("online")) {
            connectedView.setVisibility(View.VISIBLE);
        } else {
            connectedView.setVisibility(View.GONE);
        }
    }

    public interface Filter {
        public boolean shouldShow(TalkClientContact contact);
    }

}
