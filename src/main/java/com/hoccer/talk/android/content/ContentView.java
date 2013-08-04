package com.hoccer.talk.android.content;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.hoccer.talk.android.R;
import org.apache.log4j.Logger;

public class ContentView extends LinearLayout {

    private static final Logger LOG = Logger.getLogger(ContentView.class);

    ContentRegistry mRegistry;

    LinearLayout mContent;

    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRegistry = ContentRegistry.get(context.getApplicationContext());
        initView(context);
    }

    private void initView(Context context) {
        addView(inflate(context, R.layout.view_content, null));
        mContent = (LinearLayout)findViewById(R.id.content_content);
    }

    public void displayContent(Activity activity, ContentObject object) {
        if(object.getContentUrl() != null) {
            LOG.info("displayContent(" + object.getContentUrl() + ")");
        }
        mContent.removeAllViews();
        View view = mRegistry.createViewForContent(activity, object);
        mContent.addView(view);
    }

    public void clear() {
        LOG.info("clear()");
        mContent.removeAllViews();
    }

}
