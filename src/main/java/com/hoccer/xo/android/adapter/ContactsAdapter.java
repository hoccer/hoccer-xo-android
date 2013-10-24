package com.hoccer.xo.android.adapter;

import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for adapters dealing with contacts
 *
 * This allows base classes to override the UI for various needs.
 *
 * It also has a filter feature that allows restricting the displayed set of contacts.
 *
 */
public abstract class ContactsAdapter extends XoAdapter implements IXoContactListener {

    private static final Logger LOG = Logger.getLogger(ContactsAdapter.class);

    protected final static long ITEM_ID_UNKNOWN = -1000;
    protected final static long ITEM_ID_CLIENT_HEADER = -1;
    protected final static long ITEM_ID_GROUP_HEADER = -2;
    protected final static long ITEM_ID_TOKENS_BASE = -10000;

    protected final static int VIEW_TYPE_SEPARATOR = 0;
    protected final static int VIEW_TYPE_CLIENT    = 1;
    protected final static int VIEW_TYPE_GROUP     = 2;
    protected final static int VIEW_TYPE_TOKEN     = 3;

    protected final static int VIEW_TYPE_COUNT = 4;

    public ContactsAdapter(XoActivity activity) {
        super(activity);
    }

    Filter mFilter = null;

    protected boolean mShowTokens = false;

    List<TalkClientSmsToken> mSmsTokens = new ArrayList<TalkClientSmsToken>();
    List<TalkClientContact> mClientContacts = new ArrayList<TalkClientContact>();
    List<TalkClientContact> mGroupContacts = new ArrayList<TalkClientContact>();

    public Filter getFilter() {
        return mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
    }

    public boolean isShowTokens() {
        return mShowTokens;
    }

    public void setShowTokens(boolean mShowTokens) {
        this.mShowTokens = mShowTokens;
    }

    @Override
    public void register() {
        getXoClient().registerContactListener(this);
    }

    @Override
    public void unregister() {
        getXoClient().unregisterContactListener(this);
    }

    @Override
    public void reload() {
        synchronized (this) {
            try {
                List<TalkClientSmsToken> newTokens = null;
                if(mShowTokens) {
                    newTokens = mDatabase.findAllSmsTokens();
                } else {
                    newTokens = new ArrayList<TalkClientSmsToken>();
                }

                List<TalkClientContact> newClients = mDatabase.findAllClientContacts();
                List<TalkClientContact> newGroups = mDatabase.findAllGroupContacts();

                if(mFilter != null) {
                    newClients = filter(newClients, mFilter);
                    newGroups = filter(newGroups, mFilter);
                }

                for(TalkClientContact contact: newClients) {
                    TalkClientDownload avatarDownload = contact.getAvatarDownload();
                    if(avatarDownload != null) {
                        mDatabase.refreshClientDownload(avatarDownload);
                    }
                }
                for(TalkClientContact contact: newGroups) {
                    TalkClientDownload avatarDownload = contact.getAvatarDownload();
                    if(avatarDownload != null) {
                        mDatabase.refreshClientDownload(avatarDownload);
                    }
                }

                mClientContacts = newClients;
                mGroupContacts = newGroups;
                mSmsTokens = newTokens;
            } catch (SQLException e) {
                LOG.error("sql error", e);
            }
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
    public void onContactAdded(TalkClientContact c) {
        try {
            TalkClientContact contact = mDatabase.findClientContactById(c.getClientContactId());
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
    public void onContactRemoved(TalkClientContact contact) {
        reload();
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        reload();
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        reload();
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        reload();
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        reload();
    }

    // XXX @Override
    public void onSmsTokensChanged() throws RemoteException {
        LOG.info("onSmsTokensChanged()");
        if(mShowTokens) {
            reload();
        }
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

        count += mSmsTokens.size();
        count += mClientContacts.size();
        count += mGroupContacts.size();

        if(!mSmsTokens.isEmpty()) {
            //count += 1;
        }
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
        if(!mSmsTokens.isEmpty()) {
            int tokenPos = position - offset;
            if(tokenPos >= 0 && tokenPos < mSmsTokens.size()) {
                return mSmsTokens.get(tokenPos);
            }
            offset += mSmsTokens.size();
        }
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
        if(!mSmsTokens.isEmpty()) {
            int tokenPos = position - offset;
            if(tokenPos >= 0 && tokenPos < mSmsTokens.size()) {
                return VIEW_TYPE_TOKEN;
            }
            offset += mSmsTokens.size();
        }
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
        if(item instanceof TalkClientSmsToken) {
            return ITEM_ID_TOKENS_BASE + ((TalkClientSmsToken)item).getSmsTokenId();
        }

        int offset = 0;
        if(!mSmsTokens.isEmpty()) {
            offset += mSmsTokens.size();
        }
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
                v = mInflater.inflate(getClientLayout(), null);
            }
            updateContact(v, (TalkClientContact) getItem(position));
            break;
        case VIEW_TYPE_GROUP:
            if(v == null) {
                v = mInflater.inflate(getGroupLayout(), null);
            }
            updateContact(v, (TalkClientContact)getItem(position));
            break;
        case VIEW_TYPE_SEPARATOR:
            if(v == null) {
                v = mInflater.inflate(getSeparatorLayout(), null);
            }
            updateSeparator(v, position);
            break;
        case VIEW_TYPE_TOKEN:
            if(v == null) {
                v = mInflater.inflate(getTokenLayout(), null);
            }
            updateToken(v, (TalkClientSmsToken)getItem(position));
            break;
        default:
            v = mInflater.inflate(getSeparatorLayout(), null);
            break;
        }

        return v;
    }

    protected abstract int getClientLayout();
    protected abstract int getGroupLayout();
    protected abstract int getSeparatorLayout();
    protected abstract int getTokenLayout();

    protected abstract void updateContact(View view, final TalkClientContact contact);
    protected abstract void updateToken(View view, final TalkClientSmsToken token);


    protected void updateSeparator(View view, int position) {
        TextView separator = (TextView)view;
        separator.setText((String)getItem(position));
    }

    public interface Filter {
        public boolean shouldShow(TalkClientContact contact);
    }

}
