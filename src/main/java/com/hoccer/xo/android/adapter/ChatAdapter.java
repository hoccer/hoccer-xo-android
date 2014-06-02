package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.ContentMediaTypes;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.android.view.chat.attachements.ChatImageItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * This class represents an adapter which loads data from a given conversation and configures the chat view.
 * <p/>
 * When loading the all messages from the data base this adaptor performs batching.
 * The size of a batch is defined by the constant LOAD_MESSAGES.
 * <p/>
 * To configure list items it uses instances of ChatMessageItem and its subtypes.
 */
public class ChatAdapter extends XoAdapter implements IXoMessageListener, IXoTransferListener {

    /**
     * Enumerates all types of list items used by this adapter.
     */
    private enum ListItemType {
        ListItemWithText,
        ListItemWithImage,
        ListItemWithVideo,
        ListItemWithAudio,
        ListItemWithData,
        ListItemWithContact,
        ListItemWithLocation
    }

    private static final long LOAD_MESSAGES = 10L;

    private TalkClientContact mContact;
    private List<TalkClientMessage> mMessages = new Vector<TalkClientMessage>();
    private int mHistoryCount = -1;

    private ChatMessageItem mChatMessageItem;
    private ChatImageItem mChatImageItem;


    public ChatAdapter(XoActivity activity, TalkClientContact contact) {
        super(activity);
        mContact = contact;

        initialize();
    }

    private void initialize() {
        mChatMessageItem = new ChatMessageItem(mActivity);
        mChatImageItem = new ChatImageItem(mActivity);
        loadNextMessages();
    }

    public synchronized void loadNextMessages() {
        try {
            mHistoryCount++;
            //long offset = mHistoryCount * LOAD_MESSAGES;
            //final List<TalkClientMessage> messages = mDatabase.findMessagesByContactId(mContact.getClientContactId(), LOAD_MESSAGES, offset);
            final List<TalkClientMessage> messages = mDatabase.findMessagesByContactId(mContact.getClientContactId());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages = messages;
                    notifyDataSetChanged();
                }
            });
        } catch (SQLException e) {
            LOG.error("SQLException while retrieving messages for contact: " + mContact.getClientId(), e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        getXoClient().registerMessageListener(this);
        getXoClient().registerTransferListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getXoClient().unregisterMessageListener(this);
        getXoClient().unregisterTransferListener(this);
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TalkClientMessage message = (TalkClientMessage) getItem(position);

        if (!message.isSeen()) {
            markMessageAsSeen(message);
        }

        ChatMessageItem chatItem = getItemForMessage(message);
        if (convertView == null) {
            convertView = chatItem.getViewForMessage(message); // mChatMessageItem.getViewForMessage(message);
        } else {
            convertView = chatItem.recycleViewForMessage(convertView, message); // mChatMessageItem.recycleViewForMessage(convertView, message);
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return ListItemType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        TalkClientMessage message = (TalkClientMessage) getItem(position);
        return getListItemTypeForMessage(message).ordinal();
    }

    /**
     * Return the ListItemType which is appropriate for the given message / attachment.
     *
     * @param message The message to display
     * @return The corresponding ListItemType
     */
    private ListItemType getListItemTypeForMessage(TalkClientMessage message) {
        ListItemType listItemType = ListItemType.ListItemWithText;
        String contentType = null;

        if (message.getAttachmentDownload() != null) {
            contentType = message.getAttachmentDownload().getContentMediaType();
        } else if (message.getAttachmentUpload() != null) {
            contentType = message.getAttachmentUpload().getContentMediaType();
        }

        if (contentType != null) {
            if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeImage)) {
                listItemType = ListItemType.ListItemWithImage;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVideo)) {
                listItemType = ListItemType.ListItemWithVideo;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeAudio)) {
                listItemType = ListItemType.ListItemWithAudio;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeData)) {
                listItemType = ListItemType.ListItemWithData;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVCard)) {
                listItemType = ListItemType.ListItemWithContact;
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeGeolocation)) {
                listItemType = ListItemType.ListItemWithLocation;
            }
        }
        return listItemType;
    }

    private ChatMessageItem getItemForMessage(TalkClientMessage message) {
        ListItemType itemType = getListItemTypeForMessage(message);
        if (itemType == ListItemType.ListItemWithImage) {
            return mChatImageItem;
        }
        return mChatMessageItem;
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
    public void onMessageAdded(final TalkClientMessage message) {
        LOG.debug("onMessageAdded()");
        if (message.getConversationContact() == mContact) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.add(message);
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
                    mMessages.remove(message);
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
                    if (mMessages.contains(message)) {
                        int position = mMessages.indexOf(message);
                        mMessages.set(position, message);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    // TODO: do not update the whole conversation just because the transfer state of a single file has changed by some percent.

    @Override
    public void onDownloadRegistered(TalkClientDownload download) {
    }

    @Override
    public void onDownloadStarted(TalkClientDownload download) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDownloadProgress(TalkClientDownload download) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDownloadFinished(TalkClientDownload download) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onUploadStarted(TalkClientUpload upload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onUploadProgress(TalkClientUpload upload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onUploadFinished(TalkClientUpload upload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
