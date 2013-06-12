package com.hoccer.talk.android;

import android.widget.BaseAdapter;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.client.model.TalkClientContact;

public interface ITalkActivity {

    public ITalkClientService getTalkClientService();

	public BaseAdapter makeMessageListAdapter();
	public BaseAdapter makeContactListAdapter();

    public void selectContact(TalkClientContact contact);

    public void showPairing();
	
}
