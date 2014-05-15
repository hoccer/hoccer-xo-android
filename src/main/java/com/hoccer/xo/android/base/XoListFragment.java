package com.hoccer.xo.android.base;

import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.service.IXoClientService;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.support.v4.app.ListFragment;

import java.io.File;

public abstract class XoListFragment extends ListFragment implements IXoFragment {

    protected Logger LOG = null;

    private XoActivity mActivity;

    public XoListFragment() {
        LOG = Logger.getLogger(getClass());
    }

    public File getAvatarDirectory() {
        return new File(mActivity.getFilesDir(), "avatars");
    }

    public XoActivity getXoActivity() {
        return mActivity;
    }

    public XoClientDatabase getXoDatabase() {
        return mActivity.getXoDatabase();
    }

    public IXoClientService getXoService() {
        return mActivity.getXoService();
    }

    public void runOnUiThread(Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }

    @Override
    public void onAttach(Activity activity) {
        LOG.debug("onAttach()");
        super.onAttach(activity);

        if(activity instanceof XoActivity) {
            mActivity = (XoActivity)activity;
        } else {
            throw new RuntimeException("talk fragments need to be in a talk activity");
        }

        mActivity.registerXoFragment(this);
    }

    @Override
    public void onDetach() {
        LOG.debug("onDetach()");
        super.onDetach();

        if(mActivity != null) {
            mActivity.unregisterXoFragment(this);
            mActivity = null;
        }
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    @Override
    public void onAvatarSelected(IContentObject co) {
    }

    @Override
    public void onAttachmentSelected(IContentObject co) {
    }

}
