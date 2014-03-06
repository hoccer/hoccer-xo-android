package com.hoccer.xo.android.content.image;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.release.R;

import java.util.ArrayList;

public class VideoViewer extends ContentViewer<View> {

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("video");
    }

    @Override
    protected View makeView(Activity activity) {
        View view = View.inflate(activity, R.layout.content_video, null);
        return view;
    }


    @Override
    protected void updateViewInternal(final View view, final ContentView contentView,
                                      final IContentObject contentObject, boolean isLightTheme) {
        if(contentObject.isContentAvailable()) {
            initImageButton(view, contentObject, isLightTheme);
            initTextViews(view, isLightTheme);
        }
    }

    private void initImageButton(final View view, final IContentObject contentObject, boolean isLightTheme) {
        ImageButton openMapButton = (ImageButton) view.findViewById(R.id.ib_content_open);
        int imageResource = isLightTheme ? R.drawable.ic_dark_video
                : R.drawable.ic_light_video;
        openMapButton.setImageResource(imageResource);

        openMapButton.setOnClickListener(new View.OnClickListener() {
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
    }

    private void initTextViews(View view, boolean isLightTheme) {
        int textColor = isLightTheme ? Color.BLACK : Color.WHITE;
        TextView title = (TextView) view.findViewById(R.id.tv_video_title);
        TextView description = (TextView) view.findViewById(R.id.tv_video_description);

        title.setTextColor(textColor);
        description.setTextColor(textColor);
    }

//    private Bitmap getFrames(Uri uri) {
//        try {
//            ArrayList<Bitmap> bArray = new ArrayList<Bitmap>();
//            bArray.clear();
//            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
//            mRetriever.setDataSource(mContext, uri); //need context
//            mRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//            return mRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//        } catch (Exception e) { return null; }
//    }

    @Override
    protected void clearViewInternal(View view) {
    }

}
