package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.ContentMediaTypes;
import com.hoccer.xo.android.view.chat.ChatMessageItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;


public class ChatAdapter extends XoAdapter implements IXoMessageListener {

    /**
     * Enumerates all types of list items used by this adapter.
     */
    private enum ListItemTypes {
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


    public ChatAdapter(XoActivity activity, TalkClientContact contact) {
        super(activity);
        mContact = contact;

        initialize();
    }

    private void initialize() {
        mChatMessageItem = new ChatMessageItem(mActivity);
        loadNextMessages();
    }

    public synchronized void loadNextMessages() {
        try {
            mHistoryCount++;
            long offset = mHistoryCount * LOAD_MESSAGES;
            final List<TalkClientMessage> messages = mDatabase.findMessagesByContactId(mContact.getClientContactId(), LOAD_MESSAGES, offset);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.addAll(0, messages);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getXoClient().unregisterMessageListener(this);
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

        if (convertView == null) {
            convertView = mChatMessageItem.getViewForMessage(message);
        } else {
            convertView = mChatMessageItem.recycleViewForMessage(convertView, message);
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return ListItemTypes.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        TalkClientMessage message = (TalkClientMessage) getItem(position);

        return getListItemTypeForMessage(message);
    }

    private int getListItemTypeForMessage(TalkClientMessage message) {
        int listItemType = ListItemTypes.ListItemWithText.ordinal();
        String contentType = null;

        if (message.getAttachmentDownload() != null) {
            contentType = message.getAttachmentDownload().getContentMediaType();
        } else if (message.getAttachmentUpload() != null) {
            contentType = message.getAttachmentUpload().getContentMediaType();
        }

        if (contentType != null) {
            if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeImage)) {
                listItemType = ListItemTypes.ListItemWithImage.ordinal();
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVideo)) {
                listItemType = ListItemTypes.ListItemWithVideo.ordinal();
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeAudio)) {
                listItemType = ListItemTypes.ListItemWithAudio.ordinal();
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeData)) {
                listItemType = ListItemTypes.ListItemWithData.ordinal();
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeVCard)) {
                listItemType = ListItemTypes.ListItemWithContact.ordinal();
            } else if (contentType.equalsIgnoreCase(ContentMediaTypes.MediaTypeGeolocation)) {
                listItemType = ListItemTypes.ListItemWithLocation.ordinal();
            }
        }
        return listItemType;
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
}
