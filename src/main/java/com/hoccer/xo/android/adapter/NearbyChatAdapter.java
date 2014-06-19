package com.hoccer.xo.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.IXoMessageListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NearbyChatAdapter extends ChatAdapter {
    private Logger LOG = Logger.getLogger(NearbyChatAdapter.class);
    private XoActivity mActivity;

//    private List<TalkClientContact> mNearbyContacts = new ArrayList<TalkClientContact>();

//    private OnItemCountChangedListener mOnItemCountChangedListener;

    public NearbyChatAdapter(ListView listView, XoActivity activity, TalkClientContact contact) {
        super(listView, activity, contact);
        mActivity = activity;
    }

    @Override
    protected void initialize() {
        int totalMessageCount = 0;
        try {
            totalMessageCount = (int) mDatabase.getMessageCountNearby();
        } catch (SQLException e) {
            LOG.error("SQLException while loading message count in nearby ");
        }
        mChatMessageItems = new ArrayList<ChatMessageItem>(totalMessageCount);
        for (int i = 0; i < totalMessageCount; i++) {
            mChatMessageItems.add(null);
        }
        loadNextMessages(mChatMessageItems.size() - (int) LOAD_MESSAGES);
    }

//    public void setOnItemCountChangedListener(OnItemCountChangedListener listener) {
//        mOnItemCountChangedListener = listener;
//    }

    @Override
    public synchronized void loadNextMessages(int offset) {
        try {
            if (offset < 0) {
                offset = 0;
            }
            final List<TalkClientMessage> messagesBatch = mDatabase.findNearbyMessages(LOAD_MESSAGES, offset);
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
    }
}
