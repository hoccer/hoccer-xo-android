package com.hoccer.xo.android.content.image;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.release.R;

public class VideoViewer extends ContentViewer<Button> {

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("video");
    }

    @Override
    protected Button makeView(Activity activity) {
        Button view = new Button(activity);
        view.setText("Play video");
        return view;
    }

    @Override
    protected void updateViewInternal(final Button view, final ContentView contentView, final IContentObject contentObject, boolean isLightTheme) {
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
                            try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(url), "video/*");
                            view.getContext().startActivity(intent);
                            } catch(ActivityNotFoundException exception) {
                                Toast.makeText(view.getContext(), R.string.error_no_videoplayer,
                                        Toast.LENGTH_LONG).show();
                                LOG.error(exception);
                            }
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
