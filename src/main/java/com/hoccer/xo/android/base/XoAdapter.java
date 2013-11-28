package com.hoccer.xo.android.base;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.xo.android.XoApplication;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base class for list adapters
 *
 * This enforces a registration/deregistration lifecycle
 * for use with client listeners and provides some
 * convenience methods.
 *
 */
public abstract class XoAdapter extends BaseAdapter {

    protected Logger LOG = null;

    static final long RATE_LIMIT_MSECS = 100;

    protected XoActivity mActivity;
    protected XoClientDatabase mDatabase;

    protected Resources mResources;
    protected LayoutInflater mInflater;

    ScheduledExecutorService mExecutor;

    ScheduledFuture<?> mNotifyFuture;
    long mNotifyTimestamp;

    public XoAdapter(XoActivity activity) {
        LOG = Logger.getLogger(getClass());
        mActivity = activity;
        mDatabase = mActivity.getXoDatabase();
        mInflater = mActivity.getLayoutInflater();
        mResources = mActivity.getResources();
        mExecutor = mActivity.getBackgroundExecutor();
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

    @Override
    public void notifyDataSetChanged() {
        long now = System.currentTimeMillis();
        long delta = now - mNotifyTimestamp;
        if(mNotifyFuture != null) {
            mNotifyFuture.cancel(false);
            mNotifyFuture = null;
        }
        if(delta < RATE_LIMIT_MSECS) {
            long delay = RATE_LIMIT_MSECS - delta;
            mNotifyFuture = mExecutor.schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            mNotifyTimestamp = System.currentTimeMillis();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    XoAdapter.super.notifyDataSetChanged();
                                }
                            });
                        }
                    }
            , delay, TimeUnit.MILLISECONDS);
        } else {
            mNotifyTimestamp = System.currentTimeMillis();
            super.notifyDataSetChanged();
        }
    }

}
