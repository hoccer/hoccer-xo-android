package com.hoccer.xo.android.adapter;

import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.ContentMediaTypes;

import android.view.View;
import android.view.ViewGroup;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adapter for messages in a conversation
 */
public class ConversationAdapter extends XoAdapter
        implements IXoMessageListener, IXoTransferListener {

    /**
     * Enumerates all types of list items used by this adapter.
     */
    private static final class ListItemTypes {
        public static final int ListItemWithText = 0;
        public static final int ListItemWithImage = 1;
        public static final int ListItemWithVideo = 2;
        public static final int ListItemWithAudio = 3;
        public static final int ListItemWithData = 4;
        public static final int ListItemWithContact = 5;
        public static final int ListItemWithLocation = 6;
        public static final int length = 7;
    }

    private static final long LOAD_MESSAGES = 10L;

    private final AtomicInteger mVersion = new AtomicInteger();

    private boolean mReloadHappened = false;

    private TalkClientContact mContact;

    private List<TalkClientMessage> mMessages = new Vector<TalkClientMessage>();

    private ScheduledFuture<?> mReloadFuture;

    private int mHistoryCount = 0;


    public ConversationAdapter(XoActivity activity) {
        super(activity);
    }

    public void converseWithContact(TalkClientContact contact) {
        LOG.debug("converseWithContact(" + contact.getClientContactId() + ")");
        if (mContact != contact) {
            mVersion.incrementAndGet();
            mContact = contact;
            mReloadHappened = false;
            mMessages.clear();
            notifyDataSetChanged();
            requestReload();
        }
    }

    @Override
    public void onCreate() {
        LOG.debug("onCreate()");
        super.onCreate();
        getXoClient().registerMessageListener(this);
        getXoClient().registerTransferListener(this);
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
        cancelReload();
        getXoClient().unregisterMessageListener(this);
        getXoClient().unregisterTransferListener(this);
    }

    @Override
    public void onReloadRequest() {
        LOG.debug("onReloadRequest()");
        super.onReloadRequest();
        startReload();
    }

    /** Triggers change notification on the ui thread */
    private void update() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /** Used internally to fault in related objects we need */
    private void reloadRelated(TalkClientMessage message) throws SQLException {
        TalkClientContact sender = message.getSenderContact();
        if (sender != null) {
            mDatabase.refreshClientContact(sender);
        }
        TalkClientContact conversation = message.getConversationContact();
        if (conversation != null) {
            mDatabase.refreshClientContact(conversation);
        }
    }

    private void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            LOG.debug("reload interrupted");
            throw new InterruptedException();
        }
    }

    private void performReload(final int version) {
        LOG.debug("performReload(" + version + ")");

        if (mContact != null) {

            try {
                // refresh the conversation contact
                mDatabase.refreshClientContact(mContact);
                checkInterrupt();

                long messagesToLoad = LOAD_MESSAGES;
                if (mMessages != null && !mMessages.isEmpty()) {
                    messagesToLoad = mMessages.size();
                }

                final List<TalkClientMessage> messages = mDatabase.findMessagesByContactId(mContact.getClientContactId(), messagesToLoad, 0);
                checkInterrupt();

                // update related objects
                for (TalkClientMessage message : messages) {
                    reloadRelated(message);
                    checkInterrupt();
                }

                // log about it
                LOG.debug("reload found " + messages.size() + " messages");

                // update the ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mVersion.compareAndSet(version, version + 1)) {
                            LOG.debug("reload updates ui");
                            mReloadHappened = true;
                            mMessages = messages;
                            reloadFinished();
                            notifyDataSetChanged();
                        } else {
                            LOG.debug("reload has been superseded");
                        }
                    }
                });
            } catch (SQLException e) {
                LOG.error("sql error", e);
            } catch (InterruptedException e) {
                LOG.trace("reload interrupted");
            } catch (Throwable e) {
                LOG.error("error reloading", e);
            }
        }
//        synchronized (ConversationAdapter.this) {
//            mReloadFuture = null;
//        }
    }

    /** Performs a full onReloadRequest */
    void startReload() {
        LOG.debug("startReload()");
        ScheduledExecutorService executor = XoApplication.getExecutor();
        final int startVersion = mVersion.get();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                performReload(startVersion);
            }
        };
        synchronized (this) {
            if (mReloadFuture != null) {
                mReloadFuture.cancel(true);
            }
            if (isActive()) {
                mReloadFuture = executor.schedule(runnable, 0, TimeUnit.MILLISECONDS);
            } else {
                requestReload();
            }
        }
    }

    private boolean cancelReload() {
        LOG.trace("cancelReload()");
        boolean cancelled = false;
//        synchronized (ConversationAdapter.this) {
//            if (mReloadFuture != null) {
//                cancelled = mReloadFuture.cancel(true);
//                mReloadFuture = null;
//            }
//        }
        return cancelled;
    }

//    @Override
//    public void onContentViewLongClick(OldContentView contentView) {
//        mActivity.showPopupForContentView(contentView);
//    }


    public synchronized void loadNextMessages() {
        try {
            mHistoryCount++;
            long offset = mHistoryCount * LOAD_MESSAGES;
            final List<TalkClientMessage> messages = mDatabase.findMessagesByContactId(mContact.getClientContactId(), LOAD_MESSAGES, offset);
            for (TalkClientMessage message : messages) {
                reloadRelated(message);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.addAll(0, messages);
                    notifyDataSetChanged();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageAdded(final TalkClientMessage message) {
        LOG.debug("onMessageAdded()");
        if (mContact != null && message.getConversationContact() == mContact) {
            final boolean forceReload = !mReloadHappened;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean reloadAgain = cancelReload();
                    mVersion.incrementAndGet();
                    mMessages.add(message);
                    notifyDataSetChanged();
                    if (reloadAgain || forceReload) {
                        requestReload();
                    }
                }
            });
        }
    }

    @Override
    public void onMessageRemoved(TalkClientMessage message) {
        requestReload();
    }

    @Override
    public void onMessageStateChanged(TalkClientMessage message) {
        update();
    }

    // TODO: do not update the whole conversation just because the transfer state of a single file has changed by some percent.
    // TODO: Either update the cell selectively or have the cell observe the state of the transfer object by itself.

    @Override
    public void onDownloadRegistered(TalkClientDownload download) {
    }

    @Override
    public void onDownloadStarted(TalkClientDownload download) {
        update();
    }

    @Override
    public void onDownloadProgress(TalkClientDownload download) {
        update();
    }

    @Override
    public void onDownloadFinished(TalkClientDownload download) {
        update();
    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {
        update();
    }

    @Override
    public void onUploadStarted(TalkClientUpload upload) {
        update();
    }

    @Override
    public void onUploadProgress(TalkClientUpload upload) {
        update();
    }

    @Override
    public void onUploadFinished(TalkClientUpload upload) {
        update();
    }

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {
        update();
    }







    @Override
    public boolean hasStableIds() {
        return true;
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
        return getItem(position).getClientMessageId();
    }

    @Override
    public int getViewTypeCount() {
        return ListItemTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        TalkClientMessage message = getItem(position);

        return getListItemTypeForMessage(message);
    }

    private int getListItemTypeForMessage(TalkClientMessage message) {
        int listItemType = ListItemTypes.ListItemWithText;
        String contentType = null;

        if (message.getAttachmentDownload() != null) {
            contentType = message.getAttachmentDownload().getContentMediaType();
        } else if (message.getAttachmentUpload() != null) {
            contentType = message.getAttachmentUpload().getContentMediaType();
        }

        if (contentType != null) {
            if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeImage)) {
                listItemType = ListItemTypes.ListItemWithImage;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVideo)) {
                listItemType = ListItemTypes.ListItemWithVideo;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeAudio)) {
                listItemType = ListItemTypes.ListItemWithAudio;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeData)) {
                listItemType = ListItemTypes.ListItemWithData;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVCard)) {
                listItemType = ListItemTypes.ListItemWithContact;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeGeolocation)) {
                listItemType = ListItemTypes.ListItemWithLocation;
            }
        }
        return listItemType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TalkClientMessage message = getItem(position);

        if (convertView == null) {
            //convertView = new ChatMessageItem.get(parent.getContext());
        }
        //ChatMessageItem contentItem = (ChatMessageItem)convertView;
        //contentItem.configureWithMessage(message);
        return convertView;
    }

    /*
    private void updateViewOutgoing(View view, TalkClientMessage message) {
        LOG.trace("updateViewOutgoing(" + message.getClientMessageId() + ")");
        updateViewCommon(view, message);
        if(mContact.isGroup()) {
            View avatar = view.findViewById(R.id.message_avatar);
            avatar.setVisibility(View.GONE);
        }
    }

    private void updateViewIncoming(View view, TalkClientMessage message) {
        LOG.trace("updateViewIncoming(" + message.getClientMessageId() + ")");
        if(mContact.isGroup()) {
            TextView contactName = (TextView) view.findViewById(R.id.tv_message_name);
            String name = message.getSenderContact().getName();
            contactName.setText(name);
            contactName.setVisibility(View.VISIBLE);
        }
        updateViewCommon(view, message);
    }

    private void updateViewCommon(View view, TalkClientMessage message) {
        final TalkClientContact sendingContact = message.getSenderContact();
        if (!message.isSeen()) {
            markMessageAsSeen(message);
        }
        setMessageText(view, message);
        setTimestamp(view, message);
        if(mContact.isGroup()) {
            setAvatar(view, sendingContact);
        } else {
            View avatar = view.findViewById(R.id.message_avatar);
            avatar.setVisibility(View.GONE);
        }
        setAttachment(view, message);
    }

    private void setAttachment(View view, TalkClientMessage message) {
        final OldContentView contentView = (OldContentView) view.findViewById(R.id.message_content);
        int displayHeight = mResources.getDisplayMetrics().heightPixels;
        contentView.setMaxContentHeight(Math.round(displayHeight * 0.8f));

        IContentObject contentObject = null;
        TalkClientUpload attachmentUpload = message.getAttachmentUpload();
        if (attachmentUpload != null) {
            contentObject = attachmentUpload;
        } else {
            TalkClientDownload attachmentDownload = message.getAttachmentDownload();
            if (attachmentDownload != null) {
                contentObject = attachmentDownload;
            }
        }
        if (contentObject == null) {
            contentView.setVisibility(View.GONE);
            contentView.clear();
        } else {
            contentView.setVisibility(View.VISIBLE);
            contentView.displayContent(mActivity, contentObject, message);
            contentView.setContentViewListener(this);
        }
    }

    private void setAvatar(View view, final TalkClientContact sendingContact) {
        final AvatarView avatarView = (AvatarView) view.findViewById(R.id.message_avatar);
        avatarView.setContact(sendingContact);
        avatarView.setVisibility(View.VISIBLE);
        if (sendingContact != null) {
            avatarView.setOnClickListener(new View.OnClickListener() {
                final TalkClientContact contact = sendingContact;

                @Override
                public void onClick(View v) {
                    if (!contact.isSelf()) {
                        mActivity.showContactProfile(contact);
                    }
                }
            });
        }
    }

    private void setMessageText(View view, TalkClientMessage message) {
        TextView text = (TextView) view.findViewById(R.id.message_text);
        String textString = message.getText();
        if (textString == null) {
            text.setText(""); // XXX
        } else {
            text.setText(textString);
            if (textString.length() > 0) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
        }
    }

    private void setTimestamp(View view, TalkClientMessage message) {
        TextView timestamp = (TextView) view.findViewById(R.id.message_time);
        Date time = message.getTimestamp();
        if (time != null) {
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
    }

    private void markMessageAsSeen(final TalkClientMessage message) {
        mActivity.getBackgroundExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getXoClient().markAsSeen(message);
            }
        });
    }
    */



}
