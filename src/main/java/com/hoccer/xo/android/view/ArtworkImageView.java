package com.hoccer.xo.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ArtworkImageView extends ImageView {

    public ArtworkImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }
}
