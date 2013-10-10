package com.hoccer.talk.android.content;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * Content view
 *
 * This is a wrapper for the various types of content object views.
 *
 * It needs to handle content in all states, displayable or not.
 *
 */
public class ContentView extends LinearLayout {

    private static final Logger LOG = Logger.getLogger(ContentView.class);

    ContentRegistry mRegistry;

    ContentObject mObject;

    LinearLayout mContentWrapper;

    LinearLayout mContentDownloading;
    LinearLayout mContentUnavailable;
    LinearLayout mContentDownload;
    LinearLayout mContentUploading;

    ProgressBar mDownloadingProgress;
    ProgressBar mUploadingProgress;

    /**
     * Maximum height for content view in DP.
     *
     * This is only used for views that have variable size,
     * specifically images and videos.
     *
     * The responsibility for realising this lies with the content viewers,
     * which should set up the view they return appropriately.
     *
     * Any container may and should set this according to its layout requirements
     */
    int mMaxContentHeight = Integer.MAX_VALUE;

    /**
     * Standard view constructor
     *
     * @param context
     * @param attrs
     */
    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // only get the registry when on an actual device
        if(!this.isInEditMode()) {
            mRegistry = ContentRegistry.get(context.getApplicationContext());
        }
        // initialize views
        initView(context);
    }

    private void initView(Context context) {
        addView(inflate(context, R.layout.view_content, null));
        mContentWrapper = (LinearLayout)findViewById(R.id.content_content);
        mContentDownloading = (LinearLayout)findViewById(R.id.content_downloading);
        mContentDownload = (LinearLayout)findViewById(R.id.content_download);
        mContentUnavailable = (LinearLayout)findViewById(R.id.content_unavailable);
        mContentUploading = (LinearLayout)findViewById(R.id.content_uploading);
        mDownloadingProgress = (ProgressBar)findViewById(R.id.content_downloading_progress);
        mUploadingProgress = (ProgressBar)findViewById(R.id.content_uploading_progress);
    }

    public int getMaxContentHeight() {
        return mMaxContentHeight;
    }

    public void setMaxContentHeight(int maxContentHeight) {
        this.mMaxContentHeight = maxContentHeight;
    }

    public void displayContent(Activity activity, ContentObject object) {
        if(object.getContentUrl() != null) {
            LOG.debug("displayContent(" + object.getContentUrl() + ")");
        }

        boolean changed = true;
        if(mObject != null) {
            String oldUrl = mObject.getContentUrl();
            String newUrl = object.getContentUrl();
            if(oldUrl != null && newUrl != null) {
                if(oldUrl.equals(newUrl)) {
                    changed = false;
                }
            }
        }

        if(changed) {
            mContentWrapper.removeAllViews();
        }

        mObject = object;

        ContentObject.State state = object.getState();

        LOG.debug("CO state " + state);

        if(state.equals(ContentObject.State.DOWNLOAD_NEW)) {
            mContentDownload.setVisibility(VISIBLE);
        } else {
            mContentDownload.setVisibility(GONE);
        }

        if(this.isInEditMode()) {
            mContentWrapper.setVisibility(GONE);
        } else if(mRegistry != null) {
            if(object.isAvailable()) {
                mContentWrapper.setVisibility(VISIBLE);
                if(changed || mContentWrapper.getChildCount() == 0) {
                    View view = mRegistry.createViewForContent(activity, object, this);
                    if(view != null) {
                        view.setVisibility(VISIBLE);
                        mContentWrapper.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                }
            } else {
                mContentWrapper.setVisibility(GONE);
            }
        }

        if(state.equals(ContentObject.State.DOWNLOAD_STARTED)
                || state.equals(ContentObject.State.DOWNLOAD_REQUESTED)) {
            mContentDownloading.setVisibility(VISIBLE);
            int length = object.getTransferLength();
            if(length > 0 && state.equals(ContentObject.State.DOWNLOAD_STARTED)) {
                mDownloadingProgress.setIndeterminate(false);
                mDownloadingProgress.setMax(length);
                mDownloadingProgress.setProgress(object.getTransferProgress());
            } else {
                mDownloadingProgress.setIndeterminate(true);
            }
        } else {
            mContentDownloading.setVisibility(GONE);
        }

        if(state.equals(ContentObject.State.UPLOAD_STARTED)) {
            mContentUploading.setVisibility(VISIBLE);
            int length = object.getTransferLength();
            if(length > 0) {
                mUploadingProgress.setIndeterminate(false);
                mUploadingProgress.setMax(length);
                mUploadingProgress.setProgress(object.getTransferProgress());
            } else {
                mUploadingProgress.setIndeterminate(true);
            }
        } else {
            mContentUploading.setVisibility(GONE);
        }

        if(state.equals(ContentObject.State.DOWNLOAD_FAILED)) {
            mContentUnavailable.setVisibility(VISIBLE);
        } else {
            mContentUnavailable.setVisibility(GONE);
        }
    }

    public void clear() {
        mContentWrapper.removeAllViews();
    }

}
