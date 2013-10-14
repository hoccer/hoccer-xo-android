package com.hoccer.talk.android;

import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.service.IXoClientService;
import com.hoccer.talk.android.service.IXoClientServiceListener;
import com.hoccer.talk.client.TalkClientDatabase;

public interface IXoFragment extends IXoClientServiceListener {

    public XoActivity getXoActivity();

    public TalkClientDatabase getXoDatabase();

    public IXoClientService getXoService();

    public void onServiceConnected();
    public void onServiceDisconnected();

    public void onAvatarSelected(ContentObject co);
    public void onAttachmentSelected(ContentObject co);

}
