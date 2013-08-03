package com.hoccer.talk.android.adapter;

import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.TalkAdapter;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends TalkAdapter {

    private static final int VIEW_TYPE_INCOMING = 0;
    private static final int VIEW_TYPE_OUTGOING = 1;

    private static final int VIEW_TYPE_COUNT = 2;

    private static final Logger LOG = Logger.getLogger(ConversationAdapter.class);

    TalkClientContact mContact;

    List<TalkClientMessage> mMessages;

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
        if(mContact == null) {
            mMessages = new ArrayList<TalkClientMessage>();
        } else {
            try {
                mMessages = mDatabase.findMessagesByContactId(mContact.getClientContactId());
            } catch (SQLException e) {
                LOG.error("sql error", e);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetInvalidated();
            }
        });
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

        try {
            mDatabase.refreshClientContact(sendingContact);
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }

        TextView text = (TextView)view.findViewById(R.id.message_text);
        String textString = message.getText();
        if(textString == null) {
            text.setText("Unreadable"); // XXX
        } else {
            text.setText(textString);
        }

        ImageView avatar = (ImageView)view.findViewById(R.id.message_avatar);
        if(sendingContact != null) {
            TalkClientDownload avatarDownload = sendingContact.getAvatarDownload();
            if(avatarDownload != null) {
                try {
                    mDatabase.refreshClientDownload(avatarDownload);
                } catch (SQLException e) {
                    LOG.error("SQL error", e);
                }
                if(avatarDownload.getState().equals(TalkClientDownload.State.COMPLETE)) {
                    File avatarFile = avatarDownload.getAvatarFile(getAvatarDirectory());
                    Drawable drawable = Drawable.createFromPath(avatarFile.toString());
                    avatar.setImageDrawable(drawable);
                } else {
                    avatar.setImageResource(R.drawable.ic_launcher);
                }
            } else {
                avatar.setImageResource(R.drawable.ic_launcher);
            }
        } else {
            avatar.setImageResource(R.drawable.ic_launcher);
        }

        ContentView content = (ContentView)view.findViewById(R.id.message_content);

        ContentObject contentObject = null;
        TalkClientUpload attachmentUpload = message.getAttachmentUpload();
        if(attachmentUpload != null) {
        } else {
            TalkClientDownload attachmentDownload = message.getAttachmentDownload();
            if(attachmentDownload != null) {

            }
        }
        if(contentObject == null) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }
    }
}
