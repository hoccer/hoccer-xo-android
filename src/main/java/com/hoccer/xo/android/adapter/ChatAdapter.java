package com.hoccer.xo.android.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.ContentMediaTypes;
import com.hoccer.xo.android.fragment.AudioAttachmentListFragment;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.android.view.chat.attachments.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an adapter which loads data from a given conversation and configures the
 * chat view.
 * <p/>
 * When loading the all messages from the data base this adaptor performs batching.
 * The size of a batch is defined by the constant BATCH_SIZE.
 * <p/>
 * To configure list items it uses instances of ChatMessageItem and its subtypes.
 */
public class ChatAdapter extends XoAdapter implements IXoMessageListener, IXoTransferListener {

    /**
     * Number of TalkClientMessage objects in a batch
     */
    private static final long BATCH_SIZE = 10L;

    /**
     * Defines the distance from the bottom-most item in the chat view - in number of items.
     * If you scroll up beyond this limit the chat view will not automatically scroll to the bottom
     * when a new message is displayed.
     */
    private static final int AUTO_SCROLL_LIMIT = 5;

    /**
     * Set to false to override auto scrolling behavior
     */
    private boolean shouldAutoScroll = true;

    private TalkClientContact mContact;

    private List<ChatMessageItem> mChatMessageItems;

    private ListView mListView;
    private BroadcastReceiver mReceiver;


    public ChatAdapter(ListView listView, XoActivity activity, TalkClientContact contact) {
        super(activity);
        mContact = contact;
        mListView = listView;

        initialize();
    }

    private void initialize() {
        int totalMessageCount = 0;
        try {
            totalMessageCount = (int) mDatabase.getMessageCountByContactId(mContact.getClientContactId());
        } catch (SQLException e) {
            LOG.error("SQLException while loading message count: " + mContact.getClientId(), e);
        }
        mChatMessageItems = new ArrayList<ChatMessageItem>(totalMessageCount);
        for (int i = 0; i < totalMessageCount; i++) {
            mChatMessageItems.add(null);
        }
        loadNextMessages(mChatMessageItems.size() - (int) BATCH_SIZE);
    }

    /**
     * Loads a range of TalkClientMessage objects from database starting at a given offset.
     * Range is defined by constant BATCH_SIZE.
     *
     * Creates the appropriate ChatMessageItem for each TalkClientMessage and adds it to mChatMessageItems.
     *
     * @param offset Index of the first TalkClientMessage object
     */
    public synchronized void loadNextMessages(int offset) {
        try {
            long batchSize = BATCH_SIZE;
            if(offset < 0) {
                batchSize = batchSize + offset;
                offset = 0;
            }
            final List<TalkClientMessage> messagesBatch = mDatabase.findMessagesByContactId(mContact.getClientContactId(), batchSize, offset);
            for (int i = 0; i < messagesBatch.size(); i++) {
                ChatMessageItem messageItem = getItemForMessage(messagesBatch.get(i));
                mChatMessageItems.set(offset + i, messageItem);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } catch (SQLException e) {
            LOG.error("SQLException while batch retrieving messages for contact: " + mContact.getClientId(), e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getXoClient().registerMessageListener(this);
        getXoClient().registerTransferListener(this);
        createBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getXoClient().unregisterMessageListener(this);
        getXoClient().unregisterTransferListener(this);
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public int getCount() {
        return mChatMessageItems.size();
    }

    @Override
    public ChatMessageItem getItem(int position) {
        if (mChatMessageItems.get(position) == null) {
            int offset = position - (int) BATCH_SIZE + 1;
            loadNextMessages(offset);
        }
        return mChatMessageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessageItem chatItem = getItem(position);
        if (!chatItem.getMessage().isSeen()) {
            markMessageAsSeen(chatItem.getMessage());
        }
        if (convertView == null) {
            convertView = chatItem.getViewForMessage();
        } else {
            convertView = chatItem.recycleViewForMessage(convertView);
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return ChatItemType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageItem item = getItem(position);
        return item.getType().ordinal();
    }

    /**
     * Return the ListItemType which is appropriate for the given message / attachment.
     *
     * @param message The message to display
     * @return The corresponding ListItemType
     */
    private ChatItemType getListItemTypeForMessage(TalkClientMessage message) {
        ChatItemType chatItemType = ChatItemType.ChatItemWithText;
        String contentType = null;

        if (message.getAttachmentDownload() != null) {
            contentType = message.getAttachmentDownload().getContentMediaType();
        } else if (message.getAttachmentUpload() != null) {
            contentType = message.getAttachmentUpload().getContentMediaType();
        }

        if (contentType != null) {
            if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeImage)) {
                chatItemType = ChatItemType.ChatItemWithImage;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVideo)) {
                chatItemType = ChatItemType.ChatItemWithVideo;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeAudio)) {
                chatItemType = ChatItemType.ChatItemWithAudio;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeData)) {
                chatItemType = ChatItemType.ChatItemWithData;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVCard)) {
                chatItemType = ChatItemType.ChatItemWithContact;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeGeolocation)) {
                chatItemType = ChatItemType.ChatItemWithLocation;
            }
        }
        return chatItemType;
    }

    private ChatMessageItem getItemForMessage(TalkClientMessage message) {
        ChatItemType itemType = getListItemTypeForMessage(message);
        if (itemType == ChatItemType.ChatItemWithImage) {
            return new ChatImageItem(mActivity, message);
        } else if (itemType == ChatItemType.ChatItemWithVideo) {
            return new ChatVideoItem(mActivity, message);
        } else if (itemType == ChatItemType.ChatItemWithAudio) {
            return new ChatAudioItem(mActivity, message);
        } else if (itemType == ChatItemType.ChatItemWithData) {
            return new ChatDataItem(mActivity, message);
        } else if (itemType == ChatItemType.ChatItemWithContact) {
            return new ChatContactItem(mActivity, message);
        } else if (itemType == ChatItemType.ChatItemWithLocation) {
            return new ChatLocationItem(mActivity, message);
        } else {
            return new ChatMessageItem(mActivity, message);
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


    private void createBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(AudioAttachmentListFragment.AUDIO_ATTACHMENT_REMOVED_ACTION)) {
                    int messageId = intent.getIntExtra(AudioAttachmentListFragment.TALK_CLIENT_MESSAGE_ID_EXTRA, -1);
                    if (messageId != -1) {
                        removeMessageById(messageId);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(AudioAttachmentListFragment.AUDIO_ATTACHMENT_REMOVED_ACTION);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mReceiver, filter);
    }

    private void removeMessageById(final int messageId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ChatMessageItem messageItem : mChatMessageItems) {
                    if (messageItem != null && messageItem.getMessage().getClientMessageId() == messageId) {
                        mChatMessageItems.remove(messageItem);
                        notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        if (shouldAutoScroll) {
            if (mListView.getLastVisiblePosition() >= getCount() - AUTO_SCROLL_LIMIT) {
                mListView.smoothScrollToPosition(getCount() - 1);
            }
        }
    }

    @Override
    public void onMessageAdded(final TalkClientMessage message) {
        LOG.debug("onMessageAdded()");
        if (message.getConversationContact() == mContact) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatMessageItem messageItem = getItemForMessage(message);
                    mChatMessageItems.add(messageItem);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onMessageRemoved(final TalkClientMessage message) {
        LOG.debug("onMessageRemoved()");
        if (message.getConversationContact() == mContact) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatMessageItem item = new ChatMessageItem(mActivity, message);
                    mChatMessageItems.remove(item);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onMessageStateChanged(final TalkClientMessage message) {
        LOG.debug("onMessageStateChanged()");
        if (message.getConversationContact() == mContact) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatMessageItem item = new ChatMessageItem(mActivity, message);
                    if (mChatMessageItems.contains(item)) {
                        int position = mChatMessageItems.indexOf(item);
                        ChatMessageItem originalItem = mChatMessageItems.get(position);
                        originalItem.setMessage(message);
                        notifyDataSetChanged();
                    }
                }
            });
        }
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
    public void onDownloadStateChanged(TalkClientDownload download) {
        if (download.isAvatar() && download.getState() == TalkClientDownload.State.COMPLETE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shouldAutoScroll = false;
                    notifyDataSetChanged();
                    shouldAutoScroll = true;
                }
            });
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

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {
        if (upload.isAvatar()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shouldAutoScroll = false;
                    notifyDataSetChanged();
                    shouldAutoScroll = true;
                }
            });
        }
    }
}
