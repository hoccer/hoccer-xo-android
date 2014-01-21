package com.hoccer.xo.android.content.data;

import android.app.Activity;
import android.widget.Button;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import org.apache.log4j.Logger;

public class DataViewer extends ContentViewer<Button> {

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
    protected Button makeView(Activity activity) {
        Button button = new Button(activity);
        button.setText("open file");
        return button;
    }

    @Override
    protected void updateViewInternal(Button view, ContentView contentView, IContentObject contentObject) {

    }

    @Override
    protected void clearViewInternal(Button view) {

    }
}
