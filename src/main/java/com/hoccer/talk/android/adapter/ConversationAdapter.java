package com.hoccer.talk.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.TalkAdapter;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;

import java.util.List;

public class ConversationAdapter extends TalkAdapter {

    TalkClientContact mContact;

    List<TalkClientMessage> mMessages;

    public ConversationAdapter(TalkActivity activity) {
        super(activity);
    }

    @Override
    public void reload() {
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

}
