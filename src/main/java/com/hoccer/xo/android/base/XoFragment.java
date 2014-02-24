package com.hoccer.xo.android.base;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.service.IXoClientService;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Base class for fragments working with the talk client
 *
 * This encapsulated commonalities:
 *  - access to activity for db and services
 */
public abstract class XoFragment extends Fragment implements IXoFragment {

    protected Logger LOG = null;

    private XoActivity mActivity;

    public XoFragment() {
        LOG = Logger.getLogger(getClass());
    }

    public XoClient getXoClient() {
        return XoApplication.getXoClient();
    }

    public File getAvatarDirectory() {
        return new File(mActivity.getFilesDir(), "avatars");
    }

    public ScheduledExecutorService getBackgroundExecutor() {
        return mActivity.getBackgroundExecutor();
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



    public void onServiceConnected() {
    }

    public void onServiceDisconnected() {
    }

    public void onAttachmentSelected(IContentObject contentObject) {
    }

    public void onAvatarSelected(IContentObject contentObject) {
    }

}
