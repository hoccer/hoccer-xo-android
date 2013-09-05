package com.hoccer.talk.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.hoccer.xo.R;
import org.apache.log4j.Logger;

/**
 * Image view that can enforce an aspect ratio and a maximum height
 *
 * We use this for most user content to force nice layouts.
 *
 * This has to emulate getMaxHeight() on SDK < 16.
 */
public class AspectImageView extends ImageView {

    private static final Logger LOG = Logger.getLogger(AspectImageView.class);

    double mAspectRatio = 0.0;

    int mMaxHeight = Integer.MAX_VALUE;

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
        TypedArray a;

        a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.AspectView,
            0, 0);
       try {
           mAspectRatio = a.getFloat(R.styleable.AspectView_aspectRatio, 0.0f);
       } finally {
           a.recycle();
       }

       a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AspectLimits,
                0, 0);
       try {
           setMaxHeight(a.getInt(R.styleable.AspectLimits_maxHeight, Integer.MAX_VALUE));
       } finally {
           a.recycle();
       }
    }

    /** @return the enforced aspect ratio */
    public double getAspectRatio() {
        return mAspectRatio;
    }

    /** set the enforced aspect ratio */
    public void setAspectRatio(double aspectRatio) {
        this.mAspectRatio = aspectRatio;
    }

    /**
     * Override to emulate getMaxHeight() on SDK < 16
     * @return maximum height for this view
     */
    @Override
    public int getMaxHeight() {
        if(Build.VERSION.SDK_INT >= 16) {
            return super.getMaxHeight();
        } else {
            return mMaxHeight;
        }
    }

    /**
     * Override to emulate getMaxHeight() on SDK < 16.
     * @param maxHeight for this view
     */
    @Override
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        super.setMaxHeight(maxHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        // let the default measuring occur
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // get original dimensions
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // force aspect ratio
        if(mAspectRatio != 0.0) {
            height = Math.round((int)(width / mAspectRatio));
        }
        // force max height
        int maxHeight = getMaxHeight();
        if(maxHeight != Integer.MAX_VALUE) {
            height = Math.min(height, maxHeight);
        }
        // set new dimensions
        setMeasuredDimension(width, height);
    }

}
