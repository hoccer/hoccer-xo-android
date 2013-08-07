package com.hoccer.talk.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.hoccer.talk.android.R;
import org.apache.log4j.Logger;

public class AspectImageView extends ImageView {

    private static final Logger LOG = Logger.getLogger(AspectImageView.class);

    double mAspectRatio = 0.0;

    public AspectImageView(Context context) {
        super(context);
    }

    public AspectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    public AspectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttributes(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.AspectView,
            0, 0);

       try {
           mAspectRatio = a.getFloat(R.styleable.AspectView_aspectRatio, 0.0f);
       } finally {
           a.recycle();
       }
    }

    public double getAspectRatio() {
        return mAspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.mAspectRatio = aspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        // let the default measuring occur, then force the desired aspect ratio on the view (not the drawable).
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mAspectRatio != 0.0) {
            int width = getMeasuredWidth();
            // force aspect ratio
            int height = Math.round((int)(width / mAspectRatio));
            setMeasuredDimension(width, height);
        }
    }

}
