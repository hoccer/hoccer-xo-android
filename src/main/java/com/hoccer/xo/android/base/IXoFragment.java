package com.hoccer.xo.android.base;

import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.service.IXoClientService;

/**
 * Base interface for our fragments
 *
 * We need to use several base classes for our fragments,
 * but since we want a common interface we have this.
 */
public interface IXoFragment {

    public XoActivity getXoActivity();

    public XoClientDatabase getXoDatabase();

    public IXoClientService getXoService();

    public void onServiceConnected();
    public void onServiceDisconnected();

    public void onAvatarSelected(IContentObject co);
    public void onAttachmentSelected(IContentObject co);

}
