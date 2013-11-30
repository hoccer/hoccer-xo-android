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
import com.hoccer.talk.client.XoTransferAgent;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentDisposition;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
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

    ContentState mPreviousContentState;

    LinearLayout mContentWrapper;

    View mContentChild;

    LinearLayout mContentFooter;

    ImageView mContentType;

    TextView mContentDescription;
    TextView mContentStatus;

    Button mActionButton;

    ProgressBar mTransferProgress;

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
        mContentType = (ImageView)findViewById(R.id.content_type_image);
        mContentDescription = (TextView)findViewById(R.id.content_description_text);
        mContentStatus = (TextView)findViewById(R.id.content_status_text);
        mActionButton = (Button)findViewById(R.id.content_action_button);
        mActionButton.setOnClickListener(this);
        mTransferProgress = (ProgressBar)findViewById(R.id.content_progress);
    }

    public int getMaxContentHeight() {
        return mMaxContentHeight;
    }

    public void setMaxContentHeight(int maxContentHeight) {
        this.mMaxContentHeight = maxContentHeight;
    }

    @Override
    public void onClick(View v) {
        if(v == mActionButton) {
            switch(mTransferAction) {
            case REQUEST_DOWNLOAD:
                mActionButton.setEnabled(false);
                if(mObject instanceof TalkClientDownload) {
                    TalkClientDownload download = (TalkClientDownload)mObject;
                    XoApplication.getXoClient().requestDownload(download);
                }
                break;
            case CANCEL_DOWNLOAD:
                mActionButton.setEnabled(false);
                if(mObject instanceof TalkClientDownload) {
                    TalkClientDownload download = (TalkClientDownload)mObject;
                    XoApplication.getXoClient().cancelDownload(download);
                }
                break;
            case REQUEST_UPLOAD:
                mActionButton.setEnabled(false);
                if(mObject instanceof TalkClientUpload) {
                    TalkClientUpload upload = (TalkClientUpload)mObject;
                    XoApplication.getXoClient().getTransferAgent().requestUpload(upload);
                }
                break;
            case CANCEL_UPLOAD:
                mActionButton.setEnabled(false);
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

        // determine if the content url has changed so
        // we know if we need to re-instantiate child views
        boolean contentChanged = true;
        if(mObject != null) {
            String oldUrl = mObject.getContentDataUrl();
            String newUrl = object.getContentDataUrl();
            if(oldUrl != null && newUrl != null) {
                if(oldUrl.equals(newUrl)) {
                    contentChanged = false;
                }
            }
        }
        boolean stateChanged = true;
        if(!contentChanged) {;
            if(mPreviousContentState == state) {
                stateChanged = false;
            }
        }

        // remember the new object
        mObject = object;
        mPreviousContentState = state;

        // footer
        if(state == ContentState.SELECTED || state == ContentState.UPLOAD_COMPLETE || state == ContentState.DOWNLOAD_COMPLETE) {
            mContentFooter.setVisibility(GONE);
        } else {
            mContentFooter.setVisibility(VISIBLE);
        }

        // description
        mContentDescription.setText(mRegistry.getContentDescription(object));

        // status text
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

        // download/upload actions
        mActionButton.setEnabled(true);
        mActionButton.setVisibility(VISIBLE);
        mTransferAction = TransferAction.NONE;
        switch(state) {
            case DOWNLOAD_NEW:
                mActionButton.setText("Download");
                mTransferAction = TransferAction.REQUEST_DOWNLOAD;
                break;
            case DOWNLOAD_PAUSED:
                mActionButton.setText("Continue");
                mTransferAction = TransferAction.REQUEST_DOWNLOAD;
                break;
            case DOWNLOAD_DECRYPTING:
            case DOWNLOAD_DETECTING:
                mActionButton.setEnabled(false);
                mActionButton.setText("Cancel");
                break;
            case DOWNLOAD_DOWNLOADING:
                mActionButton.setText("Cancel");
                mTransferAction = TransferAction.CANCEL_DOWNLOAD;
                break;
            case UPLOAD_NEW:
                mActionButton.setText("Upload");
                mTransferAction = TransferAction.REQUEST_UPLOAD;
                break;
            case UPLOAD_PAUSED:
                mActionButton.setText("Continue");
                mTransferAction = TransferAction.REQUEST_UPLOAD;
                break;
            case UPLOAD_REGISTERING:
            case UPLOAD_ENCRYPTING:
                mActionButton.setEnabled(false);
                mActionButton.setText("Cancel");
                break;
            case UPLOAD_UPLOADING:
                mActionButton.setText("Cancel");
                mTransferAction = TransferAction.CANCEL_UPLOAD;
                break;
            case SELECTED:
            case UPLOAD_FAILED:
            case UPLOAD_COMPLETE:
            case DOWNLOAD_FAILED:
            case DOWNLOAD_COMPLETE:
            default:
                mActionButton.setVisibility(GONE);
                break;
        }

        // progress bar
        switch (state) {
            case DOWNLOAD_DECRYPTING:
            case DOWNLOAD_DETECTING:
            case UPLOAD_REGISTERING:
            case UPLOAD_ENCRYPTING:
                mTransferProgress.setVisibility(VISIBLE);
                mTransferProgress.setIndeterminate(true);
                break;
            case DOWNLOAD_PAUSED:
            case DOWNLOAD_DOWNLOADING:
            case UPLOAD_PAUSED:
            case UPLOAD_UPLOADING:
                int length = object.getTransferLength();
                int progress = object.getTransferProgress();
                mTransferProgress.setVisibility(VISIBLE);
                mTransferProgress.setIndeterminate(false);
                mTransferProgress.setMax(length);
                mTransferProgress.setProgress(progress);
                break;
            default:
                mTransferProgress.setVisibility(GONE);
                break;
        }

        // content wrapper
        if(contentChanged || stateChanged) {
            if(mContentChild != null) {
                mContentWrapper.removeView(mContentChild);
                mContentChild = null;
            }
        }
        if(isInEditMode()) {
            mContentWrapper.setVisibility(GONE);
        } else {
            mContentWrapper.setVisibility(VISIBLE);
            if(mContentChild == null) {
                mContentChild = mRegistry.createViewForContent(activity, object, this);
                if(mContentChild != null) {
                    mContentChild.setVisibility(VISIBLE);
                    mContentWrapper.addView(mContentChild,
                            new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
        }
        if(mContentChild != null) {
            if(state == ContentState.SELECTED || state == ContentState.UPLOAD_COMPLETE || state == ContentState.DOWNLOAD_COMPLETE) {
                mContentChild.setEnabled(true);
            } else {
                mContentChild.setEnabled(false);
            }
        }
    }

    public void clear() {
        mContentWrapper.removeAllViews();
        mObject = null;
    }

}
