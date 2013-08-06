package com.hoccer.talk.android.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.TalkAdapter;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConversationAdapter extends TalkAdapter {

    private static final int VIEW_TYPE_INCOMING = 0;
    private static final int VIEW_TYPE_OUTGOING = 1;

    private static final int VIEW_TYPE_COUNT = 2;

    private static final Logger LOG = Logger.getLogger(ConversationAdapter.class);

    TalkClientContact mContact;

    List<TalkClientMessage> mMessages = new ArrayList<TalkClientMessage>();

    Map<Integer, TalkClientContact> mContacts = new HashMap<Integer, TalkClientContact>();
    Map<Integer, TalkClientDownload> mDownloads = new HashMap<Integer, TalkClientDownload>();

    long mLastReload = 0;
    ScheduledFuture<?> mReloadFuture;

    public ConversationAdapter(TalkActivity activity) {
        super(activity);
    }

    public void converseWithContact(TalkClientContact contact) {
        mContact = contact;
        reload();
    }

    @Override
    public void onMessageAdded(int contactId, int messageId) throws RemoteException {
        reload();
    }

    @Override
    public void onMessageRemoved(int contactId, int messageId) throws RemoteException {
        reload();
    }

    @Override
    public void onMessageStateChanged(int contactId, int messageId) throws RemoteException {
        reload();
    }

    @Override
    public void onDownloadRemoved(int contactId, int downloadId) throws RemoteException {
        reload();
    }

    @Override
    public void reload() {
        ScheduledExecutorService executor = TalkApplication.getExecutor();
        long now = System.currentTimeMillis();
        long since = now - mLastReload;
        if(since < 1000) {
            long delay = Math.max(0, (mLastReload + 1000) - now);
            if(mReloadFuture != null) {
                mReloadFuture.cancel(false);
            }
            mReloadFuture = executor.schedule(new Runnable() {
                @Override
                public void run() {
                    mLastReload = System.currentTimeMillis();
                    performReload();
                }
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            if(mReloadFuture != null) {
                mReloadFuture.cancel(false);
            }
            mReloadFuture = executor.schedule(new Runnable() {
                @Override
                public void run() {
                    performReload();
                }
            }, 0, TimeUnit.MILLISECONDS);
            mLastReload = now;
        }
    }

    private void performReload() {
        mContacts = new HashMap<Integer, TalkClientContact>();
        mDownloads = new HashMap<Integer, TalkClientDownload>();
        if(mContact == null) {
            mMessages = new ArrayList<TalkClientMessage>();
        } else {
            try {
                mDatabase.refreshClientContact(mContact);
                mMessages = mDatabase.findMessagesByContactId(mContact.getClientContactId());
                LOG.debug("found " + mMessages.size() + " messages");
                for(TalkClientMessage message: mMessages) {
                    LOG.debug("loading related for " + message.getClientMessageId());
                    reloadRelated(message);
                }
            } catch (SQLException e) {
                LOG.error("sql error", e);
            } catch (Throwable e) {
                LOG.error("error reloading", e);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private void reloadRelated(TalkClientMessage message) throws SQLException {
        TalkClientContact sender = message.getSenderContact();
        if(sender != null) {
            int contactId = sender.getClientContactId();
            if(mContacts.containsKey(contactId)) {
                sender = mContacts.get(contactId);
                message.setSenderContact(sender);
            } else {
                mDatabase.refreshClientContact(sender);
                mContacts.put(contactId, sender);
            }
            TalkClientDownload avatarDownload = sender.getAvatarDownload();
            if(avatarDownload != null) {
                int avatarDownloadId = avatarDownload.getClientDownloadId();
                if(mDownloads.containsKey(avatarDownloadId)) {
                    avatarDownload = mDownloads.get(avatarDownloadId);
                    sender.setAvatarDownload(avatarDownload);
                } else {
                    mDatabase.refreshClientDownload(avatarDownload);
                    mDownloads.put(avatarDownloadId, avatarDownload);
                }
            }
        }
        TalkClientContact conversation = message.getConversationContact();
        if(conversation != null) {
            int contactId = conversation.getClientContactId();
            if(mContacts.containsKey(contactId)) {
                conversation = mContacts.get(contactId);
                message.setConversationContact(conversation);
            } else {
                mDatabase.refreshClientContact(conversation);
                mContacts.put(contactId, conversation);
            }
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public TalkClientMessage getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMessages.get(position).getClientMessageId();
    }

    @Override
    public int getItemViewType(int position) {
        TalkClientMessage msg = getItem(position);
        if(msg.isOutgoing()) {
            return VIEW_TYPE_OUTGOING;
        } else {
            return VIEW_TYPE_INCOMING;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        View v = convertView;

        switch (viewType) {
        case VIEW_TYPE_OUTGOING:
            if(v == null) {
                v = mInflater.inflate(R.layout.item_conversation_outgoing, null);
            }
            updateViewOutgoing(v, getItem(position));
            break;
        case VIEW_TYPE_INCOMING:
            if(v == null) {
                v = mInflater.inflate(R.layout.item_conversation_incoming, null);
            }
            updateViewIncoming(v, getItem(position));
            break;
        }

        return v;
    }

    private void updateViewOutgoing(View view, TalkClientMessage message) {
        updateViewCommon(view, message);
    }

    private void updateViewIncoming(View view, TalkClientMessage message) {
        updateViewCommon(view, message);
    }

    private void updateViewCommon(View view, TalkClientMessage message) {
        TalkClientContact sendingContact = message.getSenderContact();

        if(!message.isSeen()) {
            markMessageAsSeen(message);
        }

        TextView text = (TextView)view.findViewById(R.id.message_text);
        String textString = message.getText();
        if(textString == null) {
            text.setText("Unreadable"); // XXX
        } else {
            text.setText(textString);
        }

        final ImageView avatar = (ImageView)view.findViewById(R.id.message_avatar);
        if(sendingContact != null) {
            TalkClientDownload avatarDownload = sendingContact.getAvatarDownload();
            if(avatarDownload != null) {
                if(avatarDownload.getState().equals(TalkClientDownload.State.COMPLETE)) {
                    File avatarFile = avatarDownload.getAvatarFile(getAvatarDirectory());
                    loadAvatar(avatar, "file://" + avatarFile.toString());
                } else {
                    loadAvatar(avatar, "content://" + R.drawable.ic_launcher);
                }
            } else {
                loadAvatar(avatar, "content://" + R.drawable.ic_launcher);
            }
        } else {
            loadAvatar(avatar, "content://" + R.drawable.ic_launcher);
        }

        ContentView contentView = (ContentView)view.findViewById(R.id.message_content);

        ContentObject contentObject = null;
        TalkClientUpload attachmentUpload = message.getAttachmentUpload();
        if(attachmentUpload != null) {
        } else {
            TalkClientDownload attachmentDownload = message.getAttachmentDownload();
            if(attachmentDownload != null) {
                contentObject = ContentObject.forDownload(attachmentDownload);
            }
        }
        if(contentObject == null) {
            contentView.setVisibility(View.GONE);
            contentView.clear();
        } else {
            contentView.setVisibility(View.VISIBLE);
            contentView.displayContent(mActivity, contentObject);
        }
    }

    private void loadAvatar(ImageView view, String url) {
        ImageLoader.getInstance().displayImage(url, view);
    }

    private void markMessageAsSeen(final TalkClientMessage message) {
        mActivity.getBackgroundExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mActivity.getService().markAsSeen(message.getClientMessageId());
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
            }
        });
    }
}
