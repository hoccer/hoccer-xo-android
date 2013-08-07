package com.hoccer.talk.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.hoccer.talk.android.R;
import org.apache.log4j.Logger;

public class AspectLinearLayout extends LinearLayout {

    private static final Logger LOG = Logger.getLogger(AspectLinearLayout.class);

    int mMaxHeight = -1;

    double mAspectRatio = 0.0;

    public AspectLinearLayout(Context context) {
        super(context);
    }

    public AspectLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    public AspectLinearLayout(Context context, AttributeSet attrs, int defStyle) {
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
            mMaxHeight = a.getDimensionPixelSize(R.styleable.AspectLimits_maxHeight, -1);
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

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
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
            if(mMaxHeight != -1) {
                int limitedHeight = Math.min(height, mMaxHeight);
                if(limitedHeight != height) {
                    width = Math.round((int)(height * mAspectRatio));
                    height = limitedHeight;
                }
            }
            setMeasuredDimension(width, height);
        }
    }

}
