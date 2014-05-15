package com.hoccer.xo.android.content;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.*;
import com.hoccer.talk.client.XoTransferAgent;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.image.ClickableImageView;
import com.hoccer.xo.android.content.image.IClickableImageViewListener;
import com.hoccer.xo.android.view.AttachmentTransferControlView;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * Content view
 *
 * This is a wrapper for the various types of content object views.
 *
 * It needs to handle content in all states, displayable or not.
 *
 */
public class ContentView extends LinearLayout implements View.OnClickListener, View.OnLongClickListener, IClickableImageViewListener {

    private static final Logger LOG = Logger.getLogger(ContentView.class);

    enum TransferAction {
        NONE,
        REQUEST_DOWNLOAD,
        CANCEL_DOWNLOAD,
        REQUEST_UPLOAD,
        CANCEL_UPLOAD,
    }

    ContentRegistry mRegistry;

    IContentObject mContent;
    ContentViewCache<?> mViewCache;

    ContentState mPreviousContentState;

    LinearLayout mContentWrapper;

    View mContentChild;

    RelativeLayout mContentFooter;

    TextView mContentDescription;

    AttachmentTransferControlView mTransferProgress;

    TransferAction mTransferAction = TransferAction.NONE;

    private IContentViewListener mContentViewListener;

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

    private boolean mWaitUntilOperationIsFinished = false;
    private  Handler mUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mTransferProgress.isGoneAfterFinished()) {
                mUploadHandler.sendEmptyMessageDelayed(0, 500);
            } else {
                updateFooter(ContentState.SELECTED);
                mContentWrapper.setVisibility(View.VISIBLE);
                if (mContentChild != null) {
                    mContentChild.setEnabled(true);
                    mContentChild.invalidate();
                }
                mWaitUntilOperationIsFinished = false;
            }
        }
    };

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
        applyAttributes(context, attrs);
    }

    public IContentObject getContent() {
        return mContent;
    }

    private void applyAttributes(Context context, AttributeSet attributes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attributes, R.styleable.AspectLimits, 0, 0);
        try {
            float maxHeightDp = a.getDimension(R.styleable.AspectLimits_maxHeight, -1f);

            mMaxContentHeight = a.getDimensionPixelSize(R.styleable.AspectLimits_maxHeight, Integer.MAX_VALUE);

        } finally {
            a.recycle();
        }
    }

    private void initView(Context context) {
        addView(inflate(context, R.layout.view_content_new, null));
        mContentWrapper = (LinearLayout)findViewById(R.id.content_wrapper);
        if(!isInEditMode()) {
            mContentWrapper.removeAllViews();
        }
        mContentFooter = (RelativeLayout)findViewById(R.id.content_footer);
        mContentDescription = (TextView)findViewById(R.id.content_description_text);
        mTransferProgress = (AttachmentTransferControlView)findViewById(R.id.content_progress);
        mTransferProgress.setOnClickListener(this);
    }

    public int getMaxContentHeight() {
        return mMaxContentHeight;
    }

    public void setMaxContentHeight(int maxContentHeight) {
        this.mMaxContentHeight = maxContentHeight;
    }

    public void setContentViewListener(IContentViewListener contentViewListener) {
        mContentViewListener = contentViewListener;
    }

    @Override
    public void onImageViewClick(ClickableImageView view) {
        LOG.debug("handling click");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mContent.getContentDataUrl()), "image/*");
        try {
            Activity activity = (Activity) view.getContext();
            activity.startActivity(intent);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageViewLongClick(ClickableImageView view) {
        mContentViewListener.onContentViewLongClick(this);
    }

    @Override
    public boolean onLongClick(View view) {
        mContentViewListener.onContentViewLongClick(this);
        return true;
    }

    @Override
    public void onClick(View v) {

        if(v == mTransferProgress) {
            switch(mTransferAction) {
            case REQUEST_DOWNLOAD:
                mTransferProgress.setEnabled(false);
                if(mContent instanceof TalkClientDownload) {
                    TalkClientDownload download = (TalkClientDownload)mContent;
                    XoApplication.getXoClient().requestDownload(download);
                }
                break;
            case CANCEL_DOWNLOAD:
                mTransferProgress.setEnabled(false);
                if(mContent instanceof TalkClientDownload) {
                    TalkClientDownload download = (TalkClientDownload)mContent;
                    XoApplication.getXoClient().cancelDownload(download);
                }
                break;
            case REQUEST_UPLOAD:
                mTransferProgress.setEnabled(false);
                if(mContent instanceof TalkClientUpload) {
                    TalkClientUpload upload = (TalkClientUpload)mContent;
                    XoApplication.getXoClient().getTransferAgent().requestUpload(upload);
                }
                break;
            case CANCEL_UPLOAD:
                mTransferProgress.setEnabled(false);
                if(mContent instanceof TalkClientUpload) {
                    TalkClientUpload upload = (TalkClientUpload)mContent;
                    XoApplication.getXoClient().getTransferAgent().cancelUpload(upload);
                }
            }
        }
        mTransferProgress.invalidate();
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

    public void displayContent(Activity activity, IContentObject content, TalkClientMessage message) {
        if(content.getContentDataUrl() != null) {
            LOG.debug("displayContent(" + content.getContentDataUrl() + ")");
        }

        ContentState state = getTrueContentState(content);
        ContentViewCache<?> contentViewCache = mRegistry.selectViewCacheForContent(content);

        // determine if the content url has changed so
        // we know if we need to re-instantiate child views
        boolean contentChanged = hasContentChanged(content);
        boolean stateChanged = hasStateChanged(contentChanged, state);
        boolean cacheChanged = hasViewCacheChanged(contentViewCache);
        ContentViewCache<?> oldViewCache = mViewCache;


        boolean contentAvailable = true;
        try {
            isValidContent(content);
        } catch (FileNotFoundException e) {
            LOG.warn(e.getMessage());
            contentAvailable = false;
        }

        // remember the new object
        mContent = content;
        mViewCache = contentViewCache;
        mPreviousContentState = state;


        // description
        mContentDescription.setText(mRegistry.getContentDescription(content));
        doDownAndUploadActions(state);
        updateProgressBar(state, content);
        boolean footerVisible = true;
        if (mWaitUntilOperationIsFinished) {
            mUploadHandler.sendEmptyMessage(0);
        } else {
            footerVisible = updateFooter(state);
        }

        removeChildViewIfContentHasChanged(contentChanged, stateChanged);
        if(contentAvailable) {
            try {
                updateContentView(cacheChanged, oldViewCache, activity, content, message);
            } catch (NullPointerException exception) {
                LOG.error("probably received an unkown media-type", exception);
                return;
            }
            if (cacheChanged || contentChanged || stateChanged) {
                boolean isLightTheme = message != null ? message.isIncoming() : true;
                mViewCache.updateView(mContentChild, this, content, isLightTheme);
            }

            if(mContentChild != null) {
                mContentChild.setEnabled(!footerVisible);
                if (footerVisible) {
                    mContentWrapper.setVisibility(View.INVISIBLE);
                } else {
                    mContentWrapper.setVisibility(View.VISIBLE);
                }
            this.setOnLongClickListener(this);
            }
        } else {
            mContentWrapper.setVisibility(View.INVISIBLE);
            this.setOnLongClickListener(null);
        }
    }

    private void isValidContent(IContentObject content) throws FileNotFoundException {
        String dataUrl = content.getContentDataUrl();
        if(dataUrl == null || dataUrl.length() == 0) {
            return;
        }
        if(dataUrl.startsWith("file://")) {
            dataUrl = dataUrl.replaceFirst("file://", "");
        }
        File file = new File(dataUrl);
        if(!file.exists()) {
            throw new FileNotFoundException("attachment file not found: " + dataUrl);
        }
    }

    private void updateContentView(boolean viewCacheChanged, ContentViewCache<?> oldViewCache,
            Activity activity,
            IContentObject object, TalkClientMessage message) {
        if(viewCacheChanged || mContentChild == null) {
            // remove old
            if(mContentChild != null) {
                View v = mContentChild;
                mContentChild = null;
                mContentWrapper.removeView(v);
                oldViewCache.returnView(activity, v);
            }
            // add new
            boolean isIncomingMessage = false;
            if (message != null) {
                isIncomingMessage = message.isIncoming();
            }
            mContentChild = mViewCache.getViewForObject(activity, this, object, isIncomingMessage);
            mContentWrapper.addView(mContentChild,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
            mContentWrapper.setVisibility(View.INVISIBLE);
        }
    }

    private void removeChildViewIfContentHasChanged(boolean contentChanged, boolean stateChanged) {
        if(contentChanged) {
            if(mContentChild != null) {
                mContentWrapper.removeView(mContentChild);
                mContentChild = null;
            }
        }
        if (stateChanged) {
            if(mContentChild != null) {
                mContentWrapper.requestLayout();
                mContentWrapper.invalidate();
            }
        }
    }

    private boolean hasViewCacheChanged(ContentViewCache<?> contentViewCache) {
        if(mViewCache != null && contentViewCache != null && contentViewCache == mViewCache) {
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
        if(mContent != null) {
            String oldUrl = mContent.getContentDataUrl();
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
        Resources res = getResources();
        switch (state) {
            case DOWNLOAD_DETECTING:
                break;
            case DOWNLOAD_NEW:
                mTransferProgress.setVisibility(VISIBLE);
                mTransferProgress.prepareToDownload();
                mTransferProgress.setText(res.getString(R.string.transfer_state_pause));
                mTransferProgress.pause();
                break;
            case DOWNLOAD_PAUSED:
                length = object.getTransferLength();
                progress = object.getTransferProgress();
                mTransferProgress.setMax(length);
                mTransferProgress.setProgressImmediately(progress);
                mTransferProgress.setText(res.getString(R.string.transfer_state_pause));
                mTransferProgress.prepareToDownload();
                mTransferProgress.pause();
                break;
            case DOWNLOAD_DOWNLOADING:
                length = object.getTransferLength();
                progress = object.getTransferProgress();
                if (length == 0 || progress == 0) {
                    length = 360;
                    progress = 18;
                }
                mTransferProgress.prepareToDownload();
                mTransferProgress.setText(res.getString(R.string.transfer_state_downloading));
                mTransferProgress.setMax(length);
                mTransferProgress.setProgress(progress);
                break;
            case DOWNLOAD_DECRYPTING:
                length = object.getTransferLength();
                mTransferProgress.setText(res.getString(R.string.transfer_state_decrypting));
                mTransferProgress.setProgress(length);
                mTransferProgress.spin();
                mWaitUntilOperationIsFinished = true;
                break;
            case DOWNLOAD_COMPLETE:
                mTransferProgress.finishSpinningAndProceed();
            case UPLOAD_REGISTERING:
                break;
            case UPLOAD_NEW:
                mTransferProgress.prepareToUpload();
                mTransferProgress.setText(res.getString(R.string.transfer_state_encrypting));
                mTransferProgress.setVisibility(VISIBLE);
                break;
            case UPLOAD_ENCRYPTING:
                mTransferProgress.prepareToUpload();
                mTransferProgress.setText(res.getString(R.string.transfer_state_encrypting));
                mTransferProgress.setVisibility(VISIBLE);
                mTransferProgress.spin();
                break;
            case UPLOAD_PAUSED:
                length = object.getTransferLength();
                progress = object.getTransferProgress();
                mTransferProgress.setMax(length);
                mTransferProgress.setProgressImmediately(progress);
                mTransferProgress.setText(res.getString(R.string.transfer_state_pause));
                mTransferProgress.pause();
                break;
            case UPLOAD_UPLOADING:
                mTransferProgress.finishSpinningAndProceed();
                mTransferProgress.setText(res.getString(R.string.transfer_state_uploading));
                mWaitUntilOperationIsFinished = true;
                length = object.getTransferLength();
                progress = object.getTransferProgress();
                mTransferProgress.setMax(length);
                mTransferProgress.setProgress(progress);
                break;
            case UPLOAD_COMPLETE:
                mTransferProgress.completeAndGone();
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

    public void clear() {
        this.setOnLongClickListener(null);
        mContentWrapper.removeAllViews();
        mContentChild = null;
        mContent = null;
    }


    
}
