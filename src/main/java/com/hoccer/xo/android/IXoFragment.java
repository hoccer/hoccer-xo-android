package com.hoccer.xo.android;

import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.xo.android.content.ContentObject;
import com.hoccer.xo.android.service.IXoClientService;

public interface IXoFragment {

    public XoActivity getXoActivity();

    public TalkClientDatabase getXoDatabase();

    public IXoClientService getXoService();

    public void onServiceConnected();
    public void onServiceDisconnected();

    public void onAvatarSelected(ContentObject co);
    public void onAttachmentSelected(ContentObject co);

}
