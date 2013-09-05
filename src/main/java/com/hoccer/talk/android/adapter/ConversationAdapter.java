package com.hoccer.talk.android.adapter;

import android.os.RemoteException;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.xo.R;
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
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
    public void onDownloadProgress(int contactId, int downloadId) throws RemoteException {
        reload();
    }

    @Override
    public void onDownloadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
        reload();
    }

    @Override
    public void onUploadProgress(int contactId, int downloadId) throws RemoteException {
        reload();
    }

    @Override
    public void onUploadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
        reload();
    }

    @Override
    public void reload() {
        ScheduledExecutorService executor = TalkApplication.getExecutor();
        long now = System.currentTimeMillis();
        long since = now - mLastReload;
        if(since < 200) {
            long delay = Math.max(0, (mLastReload + 200) - now);
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

        if(mContact != null) {
            synchronized (this) {
                try {
                    mDatabase.refreshClientContact(mContact);
                    final Map<Integer, TalkClientContact> newContacts = new HashMap<Integer, TalkClientContact>();
                    final Map<Integer, TalkClientDownload> newDownloads = new HashMap<Integer, TalkClientDownload>();
                    final List<TalkClientMessage> newMessages = mDatabase.findMessagesByContactId(mContact.getClientContactId());
                    for(TalkClientMessage message: newMessages) {
                        reloadRelated(message, newContacts, newDownloads);
                    }
                    mContacts = newContacts;
                    mDownloads = newDownloads;
                    mMessages = newMessages;
                } catch (SQLException e) {
                    LOG.error("sql error", e);
                } catch (Throwable e) {
                    LOG.error("error reloading", e);
                }
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private void reloadRelated(TalkClientMessage message, Map<Integer, TalkClientContact> newContacts, Map<Integer, TalkClientDownload> newDownloads) throws SQLException {
        TalkClientContact sender = message.getSenderContact();
        if(sender != null) {
            int contactId = sender.getClientContactId();
            if(newContacts.containsKey(contactId)) {
                sender = newContacts.get(contactId);
                message.setSenderContact(sender);
            } else {
                mDatabase.refreshClientContact(sender);
                newContacts.put(contactId, sender);
            }
            if(sender.isClient() || sender.isGroup()) {
                TalkClientDownload avatarDownload = sender.getAvatarDownload();
                if(avatarDownload != null) {
                    int avatarDownloadId = avatarDownload.getClientDownloadId();
                    if(newDownloads.containsKey(avatarDownloadId)) {
                        avatarDownload = newDownloads.get(avatarDownloadId);
                        sender.setAvatarDownload(avatarDownload);
                    } else {
                        mDatabase.refreshClientDownload(avatarDownload);
                        newDownloads.put(avatarDownloadId, avatarDownload);
                    }
                }
            }
            if(sender.isSelf()) {
                TalkClientUpload avatarUpload = sender.getAvatarUpload();
                if(avatarUpload != null) {
                    mDatabase.refreshClientUpload(avatarUpload);
                }
            }
        }
        TalkClientContact conversation = message.getConversationContact();
        if(conversation != null) {
            int contactId = conversation.getClientContactId();
            if(newContacts.containsKey(contactId)) {
                conversation = newContacts.get(contactId);
                message.setConversationContact(conversation);
            } else {
                mDatabase.refreshClientContact(conversation);
                newContacts.put(contactId, conversation);
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
            text.setText("<Unreadable>"); // XXX
        } else {
            text.setText(textString);
            if(textString.length() > 0) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
        }

        TextView timestamp = (TextView)view.findViewById(R.id.message_time);
        Date time = message.getTimestamp();
        if(time != null) {
            timestamp.setVisibility(View.VISIBLE);
            timestamp.setText(DateUtils.getRelativeDateTimeString(
                    mActivity,
                    message.getTimestamp().getTime(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0
            ));
        } else {
            timestamp.setVisibility(View.GONE);
        }

        final ImageView avatar = (ImageView)view.findViewById(R.id.message_avatar);
        String avatarUrl = "content://" + R.drawable.avatar_default_contact;
        if(sendingContact != null) {
            if(sendingContact.isGroup()) {
                avatarUrl = "content://" + R.drawable.avatar_default_group;
            } else {
                avatarUrl = "content://" + R.drawable.avatar_default_contact;
            }
            if(sendingContact.isGroup() || sendingContact.isClient()) {
                TalkClientDownload avatarDownload = sendingContact.getAvatarDownload();
                if(avatarDownload != null) {
                    if(avatarDownload.getState().equals(TalkClientDownload.State.COMPLETE)) {
                        File avatarFile = TalkApplication.getAvatarLocation(avatarDownload);
                        if(avatarFile != null) {
                            avatarUrl = "file://" + avatarFile.toString();
                        }
                    }
                }
            }
            if(sendingContact.isSelf()) {
                TalkClientUpload avatarUpload = sendingContact.getAvatarUpload();
                if(avatarUpload != null) {
                    if(avatarUpload.getState().equals(TalkClientUpload.State.COMPLETE)) {
                        File avatarFile = TalkApplication.getAvatarLocation(avatarUpload);
                        if(avatarFile != null) {
                            avatarUrl = "file://" + avatarFile.toString();
                        }
                    }
                }
            }
        }
        loadAvatar(avatar, avatarUrl);

        ContentView contentView = (ContentView)view.findViewById(R.id.message_content);

        int displayHeight = mResources.getDisplayMetrics().heightPixels;
        // XXX better place for this? also we might want to use the measured height of our list view
        contentView.setMaxContentHeight(Math.round(displayHeight * 0.8f));

        ContentObject contentObject = null;
        TalkClientUpload attachmentUpload = message.getAttachmentUpload();
        if(attachmentUpload != null) {
            contentObject = ContentObject.forUpload(attachmentUpload);
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
