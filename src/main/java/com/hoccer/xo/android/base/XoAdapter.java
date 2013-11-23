package com.hoccer.xo.android.base;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.xo.android.XoApplication;

import java.io.File;

/**
 * Base class for list adapters
 *
 * This enforces a registration/deregistration lifecycle
 * for use with client listeners and provides some
 * convenience methods.
 *
 */
public abstract class XoAdapter extends BaseAdapter {

    protected XoActivity mActivity;
    protected XoClientDatabase mDatabase;

    protected Resources mResources;
    protected LayoutInflater mInflater;


    public XoAdapter(XoActivity activity) {
        mActivity = activity;
        mDatabase = mActivity.getXoDatabase();
        mInflater = mActivity.getLayoutInflater();
        mResources = mActivity.getResources();
    }

    public void runOnUiThread(Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }

    public XoClient getXoClient() {
        return XoApplication.getXoClient();
    }

    abstract public void register();
    abstract public void unregister();
    abstract public void reload();

    public File getAvatarDirectory() {
        return new File(mActivity.getFilesDir(), "avatars");
    }

}