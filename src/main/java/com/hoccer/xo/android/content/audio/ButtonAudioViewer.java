package com.hoccer.xo.android.content.audio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;

public class ButtonAudioViewer extends ContentViewer<Button> {

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("audio");
    }

    @Override
    protected Button makeView(Activity activity) {
        Button view = new Button(activity);
        view.setText("Play audio");
        return view;
    }

    @Override
    protected void updateViewInternal(final Button view, final ContentView contentView, final IContentObject contentObject) {
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
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            view.getContext().startActivity(intent);
                        }
                    }
                }
            });
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void clearViewInternal(Button view) {
    }

}
