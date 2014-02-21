package com.hoccer.xo.android.content;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.hoccer.talk.client.XoTransferAgent;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentDisposition;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.view.AttachmentTransferControlView;
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
public class ContentView extends LinearLayout implements View.OnClickListener {

    private static final Logger LOG = Logger.getLogger(ContentView.class);

    enum TransferAction {
        NONE,
        REQUEST_DOWNLOAD,
        CANCEL_DOWNLOAD,
        REQUEST_UPLOAD,
        CANCEL_UPLOAD,
    }

    ContentRegistry mRegistry;

    IContentObject mObject;
    ContentViewer<?> mViewer;

    ContentState mPreviousContentState;

    LinearLayout mContentWrapper;

    View mContentChild;

    LinearLayout mContentFooter;

    TextView mContentDescription;
    TextView mContentStatus;

    AttachmentTransferControlView mTransferProgress;

    TransferAction mTransferAction = TransferAction.NONE;

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
        if(!isInEditMode()) {
            mContentWrapper.removeAllViews();
        }
        mContentFooter = (LinearLayout)findViewById(R.id.content_footer);
        mContentDescription = (TextView)findViewById(R.id.content_description_text);
        mContentStatus = (TextView)findViewById(R.id.content_status_text);
        mTransferProgress = (AttachmentTransferControlView)findViewById(R.id.content_progress);
        mTransferProgress.setOnClickListener(this);
    }

    public int getMaxContentHeight() {
        return mMaxContentHeight;
    }

    public void setMaxContentHeight(int maxContentHeight) {
        this.mMaxContentHeight = maxContentHeight;
    }

    @Override
    public void onClick(View v) {
        if(v == mTransferProgress) {
            switch(mTransferAction) {
            case REQUEST_DOWNLOAD:
                mTransferProgress.setEnabled(false);
                if(mObject instanceof TalkClientDownload) {
                    TalkClientDownload download = (TalkClientDownload)mObject;
                    XoApplication.getXoClient().requestDownload(download);
                }
                break;
            case CANCEL_DOWNLOAD:
                mTransferProgress.setEnabled(false);
                if(mObject instanceof TalkClientDownload) {
                    TalkClientDownload download = (TalkClientDownload)mObject;
                    XoApplication.getXoClient().cancelDownload(download);
                }
                break;
            case REQUEST_UPLOAD:
                mTransferProgress.setEnabled(false);
                if(mObject instanceof TalkClientUpload) {
                    TalkClientUpload upload = (TalkClientUpload)mObject;
                    XoApplication.getXoClient().getTransferAgent().requestUpload(upload);
                }
                break;
            case CANCEL_UPLOAD:
                mTransferProgress.setEnabled(false);
                if(mObject instanceof TalkClientUpload) {
                    TalkClientUpload upload = (TalkClientUpload)mObject;
                    XoApplication.getXoClient().getTransferAgent().cancelUpload(upload);
                }
            }
        }
    }

    private ContentState getTrueContentState(IContentObject object) {
        XoTransferAgent agent = XoApplication.getXoClient().getTransferAgent();
        ContentState state = object.getContentState();
        if(object instanceof TalkClientDownload) {
            TalkClientDownload download = (TalkClientDownload)object;
            switch (state) {
            case DOWNLOAD_DOWNLOADING:
            case DOWNLOAD_DECRYPTING:
            case DOWNLOAD_DETECTING:
                if(agent.isDownloadActive(download)) {
                    return state;
                } else {
                    return ContentState.DOWNLOAD_PAUSED;
                }
            }
        }
        if (object instanceof TalkClientUpload) {
            TalkClientUpload upload = (TalkClientUpload)object;
            switch (state) {
            case UPLOAD_REGISTERING:
            case UPLOAD_ENCRYPTING:
            case UPLOAD_UPLOADING:
                if(agent.isUploadActive(upload)) {
                    return state;
                } else {
                    return ContentState.UPLOAD_PAUSED;
                }
            }
        }
        return state;
    }

    public void displayContent(Activity activity, IContentObject object) {
        if(object.getContentDataUrl() != null) {
            LOG.debug("displayContent(" + object.getContentDataUrl() + ")");
        }

        boolean available = object.isContentAvailable();
        ContentState state = getTrueContentState(object);
        ContentDisposition disposition = object.getContentDisposition();
        ContentViewer<?> viewer = mRegistry.selectViewerForContent(object);

        // determine if the content url has changed so
        // we know if we need to re-instantiate child views
        boolean contentChanged = hasContentChanged(object);
        boolean stateChanged = hasStateChanged(contentChanged, state);
        boolean viewerChanged = hasViewerChanged(viewer);
        ContentViewer<?> oldViewer = mViewer;

        // remember the new object
        mObject = object;
        mViewer = viewer;
        mPreviousContentState = state;

        boolean footerVisible = updateFooter(state);

        // description
        mContentDescription.setText(mRegistry.getContentDescription(object));
        String stateText = getStateText(state);
        doDownAndUploadActions(state);
        updateProgressBar(state, object);
        removeChildViewIfContentHasChanged(contentChanged, stateChanged);
        try {
            updateContentView(viewerChanged, oldViewer, activity, object);
        } catch (NullPointerException exception) {
            LOG.error("probably received an unkown media-type", exception);
            return;
        }
        if(viewerChanged || contentChanged || stateChanged) {
            mViewer.updateView(mContentChild, this, object);
        }
        int visibility = isInEditMode() ? GONE : VISIBLE;
        mContentWrapper.setVisibility(visibility);

        // disable content child when we are showing the footer
        if(mContentChild != null) {
            mContentChild.setEnabled(!footerVisible);
        }
    }

    private void updateContentView(boolean viewerChanged, ContentViewer<?> oldViewer, Activity activity,
                                   IContentObject object) {
        if(viewerChanged || mContentChild == null) {
            // remove old
            if(mContentChild != null) {
                View v = mContentChild;
                mContentChild = null;
                mContentWrapper.removeView(v);
                oldViewer.returnView(activity, v);
            }
            // add new
            mContentChild = mViewer.getViewForObject(activity, this, object);
            mContentWrapper.addView(mContentChild,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private void removeChildViewIfContentHasChanged(boolean contentChanged, boolean stateChanged) {
        if(contentChanged || stateChanged) {
            if(mContentChild != null) {
                mContentWrapper.removeView(mContentChild);
                mContentChild = null;
            }
        }
    }

    private boolean hasViewerChanged(ContentViewer<?> viewer) {
        if(mViewer != null && viewer != null && viewer == mViewer) {
            return false;
        }
        return true;
    }

    private boolean hasStateChanged(boolean contentChanged, ContentState state) {
        if(!contentChanged) {
            if(mPreviousContentState == state) {
                return false;
            }
        }
        return true;
    }

    private boolean hasContentChanged(IContentObject object) {
        if(mObject != null) {
            String oldUrl = mObject.getContentDataUrl();
            String newUrl = object.getContentDataUrl();
            if(oldUrl != null && newUrl != null) {
                if(oldUrl.equals(newUrl)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean updateFooter(ContentState state) {
        if(state == ContentState.SELECTED
                || state == ContentState.UPLOAD_COMPLETE
                || state == ContentState.DOWNLOAD_COMPLETE) {
            mContentFooter.setVisibility(GONE);
        } else {
            mContentFooter.setVisibility(VISIBLE);
            return true;
        }
        return false;
    }

    private void updateProgressBar(ContentState state, IContentObject object) {
        int length = 0;
        int progress = 0;
        switch (state) {
            case DOWNLOAD_DETECTING:
                break;
            case DOWNLOAD_NEW:
                mTransferProgress.clean();
                mTransferProgress.pause();
                mTransferProgress.setVisibility(VISIBLE);
                break;
            case DOWNLOAD_PAUSED:
                mTransferProgress.pause();
                break;
            case DOWNLOAD_DOWNLOADING:
                length = object.getTransferLength();
                progress = object.getTransferProgress();
                mTransferProgress.play();
                mTransferProgress.setMax(length);
                mTransferProgress.setProgress(progress);
                break;
            case DOWNLOAD_DECRYPTING:
                length = object.getTransferLength();
                mTransferProgress.setProgress(length);
                break;
            case UPLOAD_REGISTERING:
                break;
            case UPLOAD_NEW:
                mTransferProgress.clean();
                break;
            case UPLOAD_ENCRYPTING:
                mTransferProgress.setVisibility(VISIBLE);
                break;
            case UPLOAD_PAUSED:
                mTransferProgress.pause();
                break;
            case UPLOAD_UPLOADING:
                length = object.getTransferLength();
                progress = object.getTransferProgress();
                mTransferProgress.play();
                mTransferProgress.setVisibility(VISIBLE);
                mTransferProgress.setMax(length);
                mTransferProgress.setProgress(progress);
                break;
            case UPLOAD_COMPLETE:
                mTransferProgress.setCompleteAndGone();
                break;
            default:
                mTransferProgress.setVisibility(GONE);
                break;
        }
    }

    private void doDownAndUploadActions(ContentState state) {
        mTransferProgress.setEnabled(true);
        mTransferAction = TransferAction.NONE;
        switch(state) {
            case DOWNLOAD_NEW:
                mTransferAction = TransferAction.REQUEST_DOWNLOAD;
                break;
            case DOWNLOAD_PAUSED:
                mTransferAction = TransferAction.REQUEST_DOWNLOAD;
                break;
            case DOWNLOAD_DECRYPTING:
                mTransferAction = TransferAction.CANCEL_DOWNLOAD;
                break;
            case DOWNLOAD_DETECTING:
                mTransferProgress.setEnabled(false);
                break;
            case DOWNLOAD_DOWNLOADING:
                mTransferAction = TransferAction.CANCEL_DOWNLOAD;
                break;
            case UPLOAD_NEW:
                mTransferAction = TransferAction.REQUEST_UPLOAD;
                break;
            case UPLOAD_PAUSED:
                mTransferAction = TransferAction.REQUEST_UPLOAD;
                break;
            case UPLOAD_REGISTERING:
                mTransferProgress.setEnabled(false);
                break;
            case UPLOAD_ENCRYPTING:
                mTransferAction = TransferAction.CANCEL_UPLOAD;
                break;
            case UPLOAD_UPLOADING:
                mTransferAction = TransferAction.CANCEL_UPLOAD;
                break;
            case SELECTED:
                break;
            case UPLOAD_FAILED:
                break;
            case UPLOAD_COMPLETE:
                break;
            case DOWNLOAD_FAILED:
                break;
            case DOWNLOAD_COMPLETE:
                break;
            default:
                break;
        }
    }

    private String getStateText(ContentState state) {
        String stateText = null;
        switch(state) {
            case DOWNLOAD_NEW:
                stateText = "Available for download";
                break;
            case DOWNLOAD_DOWNLOADING:
                stateText = "Downloading...";
                break;
            case DOWNLOAD_DECRYPTING:
                stateText = "Decrypting...";
                break;
            case DOWNLOAD_DETECTING:
                stateText = "Analyzing...";
                break;
            case DOWNLOAD_COMPLETE:
                stateText = "Download complete";
                break;
            case DOWNLOAD_FAILED:
                stateText = "Download failed";
                break;
            case DOWNLOAD_PAUSED:
                stateText = "Download paused";
                break;
            case UPLOAD_NEW:
                stateText = "New upload";
                break;
            case UPLOAD_REGISTERING:
                stateText = "Registering...";
                break;
            case UPLOAD_ENCRYPTING:
                stateText = "Encrypting...";
                break;
            case UPLOAD_UPLOADING:
                stateText = "Uploading...";
                break;
            case UPLOAD_COMPLETE:
                stateText = "Upload complete";
                break;
            case UPLOAD_PAUSED:
                stateText = "Upload paused";
                break;
            case UPLOAD_FAILED:
                stateText = "Upload failed";
                break;
        }
        if(stateText == null) {
            mContentStatus.setVisibility(GONE);
        } else {
            mContentStatus.setVisibility(VISIBLE);
            mContentStatus.setText(stateText);
        }
        return stateText;
    }

    public void clear() {
        mContentWrapper.removeAllViews();
        mContentChild = null;
        mObject = null;
    }

}
