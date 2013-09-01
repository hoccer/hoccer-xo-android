package com.hoccer.talk.android;

import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.client.TalkClientDatabase;

public interface ITalkFragment extends ITalkClientServiceListener {

    public TalkActivity getTalkActivity();

    public TalkClientDatabase getTalkDatabase();

    public ITalkClientService getTalkService();

    public void onServiceConnected();
    public void onServiceDisconnected();

    public void onAvatarSelected(ContentObject co);
    public void onAttachmentSelected(ContentObject co);

}
