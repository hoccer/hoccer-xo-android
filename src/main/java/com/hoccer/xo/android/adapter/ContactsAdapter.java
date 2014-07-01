package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.IXoTokenListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;

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
public abstract class ContactsAdapter extends XoAdapter
        implements IXoContactListener, IXoMessageListener, IXoTokenListener, IXoTransferListener {

    protected final static long ITEM_ID_UNKNOWN = -1000;
    protected final static long ITEM_ID_CLIENT_HEADER = -1;
    protected final static long ITEM_ID_GROUP_HEADER = -2;
    protected final static long ITEM_ID_TOKENS_BASE = -10000;

    protected final static int VIEW_TYPE_SEPARATOR = 0;
    protected final static int VIEW_TYPE_CLIENT    = 1;
    protected final static int VIEW_TYPE_GROUP     = 2;
    protected final static int VIEW_TYPE_TOKEN     = 3;
    protected static final int VIEW_TYPE_NEARBY_HISTORY = 4;

    protected final static int VIEW_TYPE_COUNT = 5;

    private OnItemCountChangedListener mOnItemCountChangedListener;

    private boolean showNearbyHistory = false;
    private long mArchivedNearbyMessagesCount = 0;

    public ContactsAdapter(XoActivity activity) {
        super(activity);
    }

    public ContactsAdapter(XoActivity activity, boolean showNearbyHistory) {
        super(activity);
        this.showNearbyHistory = showNearbyHistory;
    }

    Filter mFilter = null;

    protected boolean mShowTokens = false;

    List<TalkClientSmsToken> mSmsTokens = new ArrayList<TalkClientSmsToken>();
    List<TalkClientContact> mClientContacts = new ArrayList<TalkClientContact>();
    List<TalkClientMessage> mArchivedNearbyMessages = new ArrayList<TalkClientMessage>();

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
    public void onCreate() {
        LOG.debug("onCreate()");
        super.onCreate();
        getXoClient().registerContactListener(this);
        getXoClient().registerTokenListener(this);
        getXoClient().registerTransferListener(this);
        getXoClient().registerMessageListener(this);
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
        getXoClient().unregisterContactListener(this);
        getXoClient().unregisterTokenListener(this);
        getXoClient().unregisterTransferListener(this);
        getXoClient().unregisterMessageListener(this);
    }

    @Override
    public void onReloadRequest() {
        LOG.debug("onReloadRequest()");
        super.onReloadRequest();
        synchronized (this) {
            try {
                int oldItemCount = getCount();
                List<TalkClientSmsToken> newTokens = null;
                if(mShowTokens) {
                    newTokens = mDatabase.findAllSmsTokens();
                } else {
                    newTokens = new ArrayList<TalkClientSmsToken>();
                }

                List<TalkClientContact> newClients = mDatabase.findAllClientContacts();
                LOG.debug("found " + newClients.size() + " contacts");

                if(mFilter != null) {
                    newClients = filter(newClients, mFilter);
                }
                LOG.debug("filtered " + newClients.size() + " contacts");

                for(TalkClientContact contact: newClients) {
                    TalkClientDownload avatarDownload = contact.getAvatarDownload();
                    if(avatarDownload != null) {
                        mDatabase.refreshClientDownload(avatarDownload);
                    }
                }

                mClientContacts = newClients;
                mSmsTokens = newTokens;
                if(mOnItemCountChangedListener != null && oldItemCount != getCount()) {
                    mOnItemCountChangedListener.onItemCountChanged(getCount());
                }
            } catch (SQLException e) {
                LOG.error("SQL error", e);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reloadFinished();
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
        requestReload();
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        requestReload();
    }
    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        requestReload();
    }
    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        requestReload();
    }
    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        requestReload();
    }
    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        requestReload();
    }

    @Override
    public void onMessageAdded(TalkClientMessage message) {
        requestReload();
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
    }

    @Override
    public void onDownloadFailed(TalkClientDownload download) {
    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {
        if(download.isAvatar() && download.getState() == TalkClientDownload.State.COMPLETE) {
            requestReload();
        }
    }
    @Override
    public void onUploadStarted(TalkClientUpload upload) {
    }
    @Override
    public void onUploadProgress(TalkClientUpload upload) {
    }
    @Override
    public void onUploadFinished(TalkClientUpload upload) {
    }

    public void onUploadFailed(TalkClientUpload upload) {
    }

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {
        if(upload.isAvatar()) {
            requestReload();
        }
    }

    @Override
    public void onTokensChanged(List<TalkClientSmsToken> tokens, boolean newTokens) {
        if(mShowTokens) {
            requestReload();
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

        // add saved nearby messages
        if (showNearbyHistory) {
            // TODO: only if nearby history was found in db
            try {
                long mArchivedNearbyMessagesCount = mDatabase.getArchivedMessageCountNearby();
                if (mArchivedNearbyMessagesCount > 0) {
                    count++;
                }
            } catch (SQLException e) {
                LOG.error("SQL Error while retrieving archived nearby messages", e);
            }
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
            int clientPos = position - offset;
            if(clientPos >= 0 && clientPos < mClientContacts.size()) {
                return mClientContacts.get(clientPos);
            }
            offset += mClientContacts.size();
        }
        // TODO: only if nearby history was found in db
        if (position == getCount()-1 && mArchivedNearbyMessagesCount > 0) {
            return null;
        }
        return null;
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
            int clientPos = position - offset;
            if(clientPos >= 0 && clientPos < mClientContacts.size()) {
                return VIEW_TYPE_CLIENT;
            }
            offset += mClientContacts.size();
        }

        // TODO: only if nearby history was found in db
        if (position == getCount()-1 && mArchivedNearbyMessagesCount > 0) {
            return VIEW_TYPE_NEARBY_HISTORY;
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
            offset += mClientContacts.size();
        }
        return ITEM_ID_UNKNOWN;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);

        View v = convertView;

        switch (type) {
            case VIEW_TYPE_CLIENT:
                if (v == null) {
                    v = mInflater.inflate(getClientLayout(), null);
                }
                updateContact(v, (TalkClientContact) getItem(position));
                break;
            case VIEW_TYPE_GROUP:
                if (v == null) {
                    v = mInflater.inflate(getGroupLayout(), null);
                }
                updateContact(v, (TalkClientContact) getItem(position));
                break;
            case VIEW_TYPE_SEPARATOR:
                if (v == null) {
                    v = mInflater.inflate(getSeparatorLayout(), null);
                }
                updateSeparator(v, position);
                break;
            case VIEW_TYPE_TOKEN:
                if (v == null) {
                    v = mInflater.inflate(getTokenLayout(), null);
                }
                updateToken(v, (TalkClientSmsToken) getItem(position));
                break;
            case VIEW_TYPE_NEARBY_HISTORY:
                if (v == null) {
                    v = mInflater.inflate(getNearbyHistoryLayout(), null);
                }
                updateNearbyHistoryLayout(v);
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
    protected abstract int getNearbyHistoryLayout();

    protected abstract void updateNearbyHistoryLayout(View v);
    protected abstract void updateContact(View view, final TalkClientContact contact);
    protected abstract void updateToken(View view, final TalkClientSmsToken token);

    protected void updateSeparator(View view, int position) {
        LOG.debug("updateSeparator()");
        TextView separator = (TextView)view;
        separator.setText((String)getItem(position));
    }

    public void setOnItemCountChangedListener(OnItemCountChangedListener onItemCountChangedListener) {
        mOnItemCountChangedListener = onItemCountChangedListener;
    }

    public interface Filter {
        public boolean shouldShow(TalkClientContact contact);
    }

}
