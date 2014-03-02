package com.hoccer.xo.android.content.image;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.release.R;

import java.util.ArrayList;

public class VideoViewer extends ContentViewer<ImageView> {
    private Context mContext;

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("video");
    }

    @Override
    protected ImageView makeView(Activity activity) {
        mContext = activity.getBaseContext();
        ImageView view = new ImageView(activity);
//        view.setText("Play video");
        return view;
    }

    @Override
    protected void updateViewInternal(final ImageView view, final ContentView contentView, final IContentObject contentObject, boolean isLightTheme) {
        if(contentObject.isContentAvailable()) {
            view.setVisibility(View.VISIBLE);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));
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
            if(contentObject.isContentAvailable()) {
                String url = contentObject.getContentUrl();
                if(url == null) {
                    url = contentObject.getContentDataUrl();
                }
                if(url != null) {
                    Uri uri = Uri.parse(url);
//                    Bitmap bmp = getFrames(uri);
//                    Drawable d = new BitmapDrawable(mContext.getResources(),bmp);
                    ColorDrawable d = new ColorDrawable();
                    d.setColor(Color.GRAY);
                    view.setImageDrawable(d);
                }

            }
//            view.setImageDrawable();
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private Bitmap getFrames(Uri uri) {
        try {
            ArrayList<Bitmap> bArray = new ArrayList<Bitmap>();
            bArray.clear();
            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
            mRetriever.setDataSource(mContext, uri);

//            for (int i = 0; i < 30; i++) {
//                bArray.add(mRetriever.getFrameAtTime(1000*i,
//                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC));
//            }
            mRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//            return bArray;
            return mRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) { return null; }
    }

    @Override
    protected void clearViewInternal(ImageView view) {
    }

}
