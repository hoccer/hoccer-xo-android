package com.hoccer.xo.android.content.location;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;

public class LocationViewer extends ContentViewer<Button> {

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("location");
    }

    @Override
    protected Button makeView(Activity activity) {
        Button view = new Button(activity);
        view.setText("Show location");
        return view;
    }

    @Override
    protected void updateView(final Button view, final ContentView contentView, final IContentObject contentObject) {
        if(contentObject.isContentAvailable()) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(contentObject.isContentAvailable()) {
                        String url = contentObject.getContentUrl();
                        if(url == null) {
                            url = contentObject.getContentDataUrl();
                        }
                        if(url != null) {
                            // XXX
                        }
                    }
                }
            });
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

}
