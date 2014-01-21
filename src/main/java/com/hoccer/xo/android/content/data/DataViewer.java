package com.hoccer.xo.android.content.data;

import android.app.Activity;
import android.graphics.Typeface;
import android.widget.TextView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class DataViewer extends ContentViewer<TextView> {

    private static final Logger LOG = Logger.getLogger(DataViewer.class);

    ObjectMapper mJsonMapper;

    public DataViewer() {
        mJsonMapper = new ObjectMapper();
    }

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("data");
    }

    @Override
    protected TextView makeView(Activity activity) {
        TextView textView = new TextView(activity);
        textView.setText(R.string.feature_not_available);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        return textView;
    }

    @Override
    protected void updateViewInternal(TextView view, ContentView contentView, IContentObject contentObject) {

    }

    @Override
    protected void clearViewInternal(TextView view) {

    }
}
