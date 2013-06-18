package com.hoccer.talk.android;

import android.widget.BaseAdapter;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;

public interface ITalkActivity {

    public ITalkClientService getTalkClientService();
    public TalkClientDatabase getTalkClientDatabase();

	public BaseAdapter makeMessageListAdapter();
	public BaseAdapter makeContactListAdapter();

    public void showContactProfile(TalkClientContact contact);
    public void showContactConversation(TalkClientContact contact);

    public void showPairing();
    public void showAbout();
	
}
