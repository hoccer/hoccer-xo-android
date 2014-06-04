package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.ContentMediaTypes;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.android.view.chat.attachments.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an adapter which loads data from a given conversation and configures the chat view.
 * <p/>
 * When loading the all messages from the data base this adaptor performs batching.
 * The size of a batch is defined by the constant LOAD_MESSAGES.
 * <p/>
 * To configure list items it uses instances of ChatMessageItem and its subtypes.
 */
public class ChatAdapter extends XoAdapter implements IXoMessageListener {

    /**
     * Number of TalkClientMessage objects in a batch
     */
    private static final long LOAD_MESSAGES = 10L;

    /**
     * Defines the distance from the bottom-most item in the chat view - in number of items.
     * If you scroll up beyond this limit the chat view will not automatically scroll to the bottom when a new message is displayed.
     */
    private static final int AUTO_SCROLL_LIMIT = 5;

    private TalkClientContact mContact;

    private List<ChatMessageItem> mChatMessageItems;

    private ListView mListView;


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
        loadNextMessages(mChatMessageItems.size() - (int) LOAD_MESSAGES);
    }

    public synchronized void loadNextMessages(int offset) {
        try {
            if (offset < 0) {
                offset = 0;
            }
            final List<TalkClientMessage> messagesBatch = mDatabase.findMessagesByContactId(mContact.getClientContactId(), LOAD_MESSAGES, offset);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getXoClient().unregisterMessageListener(this);
    }

    @Override
    public int getCount() {
        return mChatMessageItems.size();
    }

    @Override
    public ChatMessageItem getItem(int position) {
        if (mChatMessageItems.get(position) == null) {
            int offset = (position / (int) LOAD_MESSAGES) * (int) LOAD_MESSAGES;
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        if (mListView.getLastVisiblePosition() >= getCount() - AUTO_SCROLL_LIMIT) {
            mListView.smoothScrollToPosition(getCount() - 1);
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
}
