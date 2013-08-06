package com.hoccer.talk.android.content;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.hoccer.talk.android.R;
import org.apache.log4j.Logger;

public class ContentView extends LinearLayout {

    private static final Logger LOG = Logger.getLogger(ContentView.class);

    ContentRegistry mRegistry;

    LinearLayout mContent;

    LinearLayout mContentDownloading;
    LinearLayout mContentUnavailable;
    LinearLayout mContentDownload;

    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRegistry = ContentRegistry.get(context.getApplicationContext());
        initView(context);
    }

    private void initView(Context context) {
        addView(inflate(context, R.layout.view_content, null));
        mContent = (LinearLayout)findViewById(R.id.content_content);
        mContentDownloading = (LinearLayout)findViewById(R.id.content_downloading);
        mContentDownload = (LinearLayout)findViewById(R.id.content_download);
        mContentUnavailable = (LinearLayout)findViewById(R.id.content_unavailable);
    }

    public void displayContent(Activity activity, ContentObject object) {
        if(object.getContentUrl() != null) {
            LOG.info("displayContent(" + object.getContentUrl() + ")");
        }

        ContentObject.State state = object.getState();

        LOG.info("content state " + state.toString());

        if(state.equals(ContentObject.State.DOWNLOAD_NEW)
                || state.equals(ContentObject.State.UPLOAD_SELECTED)) {
            mContentDownload.setVisibility(VISIBLE);
        } else {
            mContentDownload.setVisibility(GONE);
        }

        if(state.equals(ContentObject.State.DOWNLOAD_COMPLETE)) {
            mContent.setVisibility(VISIBLE);
            mContent.removeAllViews();
            View view = mRegistry.createViewForContent(activity, object);
            if(view != null) {
                LOG.info("adding view");
                view.setVisibility(VISIBLE);
                mContent.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        } else {
            mContent.setVisibility(GONE);
        }

        if(state.equals(ContentObject.State.DOWNLOAD_STARTED)) {
            mContentDownloading.setVisibility(VISIBLE);
        } else {
            mContentDownloading.setVisibility(GONE);
        }

        if(state.equals(ContentObject.State.DOWNLOAD_FAILED)) {
            mContentUnavailable.setVisibility(VISIBLE);
        } else {
            mContentUnavailable.setVisibility(GONE);
        }
    }

    public void clear() {
        mContent.removeAllViews();
    }

}
