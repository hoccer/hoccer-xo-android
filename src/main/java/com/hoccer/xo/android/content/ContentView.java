package com.hoccer.xo.android.content;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
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

    IContentObject mObject;

    LinearLayout mContentWrapper;

    LinearLayout mContentFooter;

    ImageView mContentType;

    TextView mContentDescription;
    TextView mContentStatus;

    Button mDownloadButton;
    Button mUploadButton;

    ProgressBar mTransferProgress;

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
        addView(inflate(context, R.layout.view_content_new, null));
        mContentWrapper = (LinearLayout)findViewById(R.id.content_wrapper);
        mContentFooter = (LinearLayout)findViewById(R.id.content_footer);
        mContentType = (ImageView)findViewById(R.id.content_type_image);
        mContentDescription = (TextView)findViewById(R.id.content_description_text);
        mContentStatus = (TextView)findViewById(R.id.content_status_text);
        mDownloadButton = (Button)findViewById(R.id.content_download_button);
        mUploadButton = (Button)findViewById(R.id.content_upload_button);
        mTransferProgress = (ProgressBar)findViewById(R.id.content_transfer_progress);
    }

    public int getMaxContentHeight() {
        return mMaxContentHeight;
    }

    public void setMaxContentHeight(int maxContentHeight) {
        this.mMaxContentHeight = maxContentHeight;
    }

    public void displayContent(Activity activity, IContentObject object) {
        if(object.getContentUrl() != null) {
            LOG.debug("displayContent(" + object.getContentUrl() + ")");
        }

        // determine if the content url has changed so
        // we know if we need to re-instantiate child views
        boolean contentChanged = true;
        if(mObject != null) {
            String oldUrl = mObject.getContentUrl();
            String newUrl = object.getContentUrl();
            if(oldUrl != null && newUrl != null) {
                if(oldUrl.equals(newUrl)) {
                    contentChanged = false;
                }
            }
        }

        // remember the new object
        mObject = object;

        // we examine the state of the object
        boolean available = object.isContentAvailable();
        ContentState state = object.getContentState();

        // footer
        if(state == ContentState.SELECTED || state == ContentState.UPLOAD_COMPLETE || state == ContentState.DOWNLOAD_COMPLETE) {
            mContentFooter.setVisibility(GONE);
        } else {
            mContentFooter.setVisibility(VISIBLE);
        }

        // description
        mContentDescription.setText(object.getContentMediaType());

        // status text
        if(state == ContentState.SELECTED) {
            mContentStatus.setVisibility(GONE);
        } else {
            mContentStatus.setVisibility(VISIBLE);
            int stateRes = 0;
            switch(state) {
            case DOWNLOAD_NEW:
            case DOWNLOAD_IN_PROGRESS:
            case DOWNLOAD_PAUSED:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_FAILED:
            case UPLOAD_NEW:
            case UPLOAD_IN_PROGRESS:
            case UPLOAD_PAUSED:
            case UPLOAD_COMPLETE:
            case UPLOAD_FAILED:
            }
        }

        // download/upload actions
        if(state == ContentState.DOWNLOAD_NEW || state == ContentState.DOWNLOAD_PAUSED) {
            mDownloadButton.setVisibility(VISIBLE);
        } else {
            mDownloadButton.setVisibility(GONE);
        }
        if(state == ContentState.UPLOAD_NEW || state == ContentState.UPLOAD_PAUSED) {
            mUploadButton.setVisibility(VISIBLE);
        } else {
            mUploadButton.setVisibility(GONE);
        }

        // progress bar
        if(state == ContentState.DOWNLOAD_IN_PROGRESS || state == ContentState.UPLOAD_IN_PROGRESS) {
            mTransferProgress.setVisibility(VISIBLE);
            int length = object.getTransferLength();
            int progress = object.getTransferProgress();
            if(length > 0 && progress > 0) {
                mTransferProgress.setIndeterminate(false);
                mTransferProgress.setMax(length);
                mTransferProgress.setProgress(progress);
            } else {
                mTransferProgress.setIndeterminate(true);
            }
        } else {
            mTransferProgress.setVisibility(GONE);
        }

        // content wrapper
        if(contentChanged || !object.isContentAvailable()) {
            mContentWrapper.removeAllViews();
        }
        if(available && !isInEditMode()) {
            mContentWrapper.setVisibility(VISIBLE);
            if(contentChanged || mContentWrapper.getChildCount() == 0) {
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

    public void clear() {
        mContentWrapper.removeAllViews();
        mObject = null;
    }

}
