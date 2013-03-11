package com.hoccer.talk.android;

import android.widget.BaseAdapter;
import com.hoccer.talk.android.service.ITalkClientService;

public interface ITalkActivity {

    public ITalkClientService getTalkClientService();

	public BaseAdapter makeMessageListAdapter();
	public BaseAdapter makeContactListAdapter();

	
}
