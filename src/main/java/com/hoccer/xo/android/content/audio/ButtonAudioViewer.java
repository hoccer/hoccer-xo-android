package com.hoccer.xo.android.content.audio;

import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.release.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

public class ButtonAudioViewer extends ContentViewer<View> {

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("audio");
    }

    @Override
    protected View makeView(Activity activity) {
        View view = View.inflate(activity, R.layout.content_audio, null);
        return view;
    }

    @Override
    protected void updateViewInternal(final View view, final ContentView contentView,
            final IContentObject contentObject, boolean isLightTheme) {
        if (contentObject.isContentAvailable()) {
            view.setVisibility(View.VISIBLE);
            updateFilenameText(view, contentView, contentObject, isLightTheme);

            ImageButton playButton = (ImageButton) view.findViewById(R.id.audio_play_pause);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentObject.isContentAvailable()) {
                        String url = contentObject.getContentUrl();
                        if (url == null) {
                            url = contentObject.getContentDataUrl();
                        }
                        if (url != null) {
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

    private void updateFilenameText(View view, ContentView contentView,
            IContentObject contentObject, boolean isLightTheme) {
        TextView filenameText = (TextView) view.findViewById(R.id.tv_content_audio_name);

        String dataUrl = contentObject.getContentDataUrl();
        String filename = dataUrl.substring(dataUrl.lastIndexOf(File.separator) + 1);

        filenameText.setText(filename);
        if(isLightTheme) {
            filenameText.setTextColor(Color.BLACK);
        } else {
            filenameText.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void clearViewInternal(View view) {
    }

}
