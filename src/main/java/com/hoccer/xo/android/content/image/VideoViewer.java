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
    private final int mTextBoxSize = 500;

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("video");
    }

    @Override
    protected ImageView makeView(Activity activity) {
        mContext = activity.getBaseContext();
        ImageView view = new ImageView(activity);
        return view;
    }

    @Override
    protected void updateViewInternal(final ImageView view, final ContentView contentView, final IContentObject contentObject, boolean isLightTheme) {
        if(contentObject.isContentAvailable()) {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTextBoxSize));
            ColorDrawable d = new ColorDrawable();
            d.setColor(Color.TRANSPARENT);
            view.setImageDrawable(writeOnDrawable(d, "Video is here"));
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
        }
    }

    private LayerDrawable writeOnDrawable(Drawable background, String text){

        Bitmap canvasBitmap = Bitmap.createBitmap(mTextBoxSize, mTextBoxSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBitmap);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(0xff339BB9);
        paint.setTextSize(50f);
        background.draw(canvas);
        canvas.drawText(text, canvasBitmap.getWidth()/2, canvasBitmap.getHeight()/2, paint);
        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{background, new BitmapDrawable(canvasBitmap)});
        return layerDrawable;
    }

    private Bitmap getFrames(Uri uri) {
        try {
            ArrayList<Bitmap> bArray = new ArrayList<Bitmap>();
            bArray.clear();
            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
            mRetriever.setDataSource(mContext, uri);
            mRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            return mRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) { return null; }
    }

    @Override
    protected void clearViewInternal(ImageView view) {
    }

}
