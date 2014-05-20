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
 * This base class implements our own lifecycle for adapters
 * so that they can attach client listeners and manage resources.
 *
 * Adapters get the following lifecycle calls, similar to activities:
 * onCreate, onResume, onPause, onDestroy
 *
 * Adapter reload is integrated into the lifecycle by implementing
 * an onRequestReload method. The method requestReload may be used
 * to request a reload whenever the adapter becomes active.
 *
 * Reloads are rate-limited to a minimum interval to prevent
 * hogging the CPU with superfluous view updates.
 */
public abstract class XoAdapter extends BaseAdapter {

    private static final long RATE_LIMIT_MSECS = 1000;

    protected final XoActivity mActivity;

    protected final XoClientDatabase mDatabase;

    protected final Resources mResources;

    protected final LayoutInflater mInflater;

    private final ScheduledExecutorService mExecutor;

    protected Logger LOG = null;

    private boolean mActive = false;

    private boolean mNeedsReload = false;

    private AdapterReloadListener mAdapterReloadListener;

    private ScheduledFuture<?> mNotifyFuture;

    private long mNotifyTimestamp;

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

    public File getAvatarDirectory() {
        return new File(mActivity.getFilesDir(), "avatars");
    }

    public AdapterReloadListener getAdapterReloadListener() {
        return mAdapterReloadListener;
    }

    public void setAdapterReloadListener(AdapterReloadListener adapterReloadListener) {
        mAdapterReloadListener = adapterReloadListener;
    }

    public boolean isActive() {
        return mActive;
    }

    public void onResume() {
        LOG.debug("onResume()");
        mActive = true;
        if (mNeedsReload) {
            mNeedsReload = false;
            performReload();
        }
    }

    public void onPause() {
        LOG.debug("onPause()");
        mActive = false;
    }

    public void requestReload() {
        LOG.debug("requestReload()");
        if (mActive) {
            performReload();
        } else {
            mNeedsReload = true;
        }
    }

    private void performReload() {
        LOG.debug("performReload()");
        if (mAdapterReloadListener != null) {
            mAdapterReloadListener.onAdapterReloadStarted(this);
        }
        onReloadRequest();
    }

    protected void reloadFinished() {
        LOG.debug("reloadFinished()");
        if (mAdapterReloadListener != null) {
            mAdapterReloadListener.onAdapterReloadFinished(this);
        }
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public void onReloadRequest() {
    }

    @Override
    public void notifyDataSetChanged() {
        LOG.trace("notifyDataSetChanged()");
        long now = System.currentTimeMillis();
        long delta = now - mNotifyTimestamp;
        if (mNotifyFuture != null) {
            mNotifyFuture.cancel(false);
            mNotifyFuture = null;
        }
        if (delta < RATE_LIMIT_MSECS) {
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

    public interface AdapterReloadListener {

        public void onAdapterReloadStarted(XoAdapter adapter);

        public void onAdapterReloadFinished(XoAdapter adapter);
    }

}
