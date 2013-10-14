package com.hoccer.xo.android;

import android.widget.BaseAdapter;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.service.IXoClientService;

public interface IXoActivity {

    public int getClientState();

    public IXoClientService getXoService();
    public TalkClientDatabase getXoDatabase();

	public BaseAdapter makeConversationAdapter();
	public BaseAdapter makeContactListAdapter();

    public void showContactProfile(TalkClientContact contact);
    public void showContactConversation(TalkClientContact contact);

    public void showPreferences();
    public void showPairing();
    public void showAbout();
	
}
