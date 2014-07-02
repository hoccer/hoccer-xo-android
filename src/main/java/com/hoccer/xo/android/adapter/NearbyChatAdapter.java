package com.hoccer.xo.android.adapter;

import android.widget.ListView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.chat.ChatMessageItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NearbyChatAdapter extends ChatAdapter {

    public NearbyChatAdapter(ListView listView, XoActivity activity) {
        super(listView, activity, null);
    }

    @Override
    protected void initialize() {
        int totalMessageCount = 0;
        try {
            totalMessageCount = (int) mDatabase.getNearbyMessageCount();
        } catch (SQLException e) {
            LOG.error("SQLException while loading message count in nearby ");
        }
        mChatMessageItems = new ArrayList<ChatMessageItem>(totalMessageCount);
        for (int i = 0; i < totalMessageCount; i++) {
            mChatMessageItems.add(null);
        }
        loadNextMessages(mChatMessageItems.size() - (int) BATCH_SIZE);
    }

    @Override
    public synchronized void loadNextMessages(int offset) {
        try {
            if (offset < 0) {
                offset = 0;
            }
//            final List<TalkClientMessage> messagesBatch = mDatabase.findNearbyMessages(BATCH_SIZE, offset);
            final List<TalkClientMessage> messagesBatch = mDatabase.findMessagesByContactId(mContact.getClientContactId(), BATCH_SIZE, offset);
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
            LOG.error("SQLException while batch retrieving messages for nearby", e);
        }
    }

    public void setConverseContact(TalkClientContact talkClientContact) {
        mContact = talkClientContact;
        initialize();
    }
}
