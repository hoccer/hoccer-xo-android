package com.hoccer.xo.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 13.02.14.
 */
public class OverscrollListView extends ListView {

    private List<OnOverscrollListener> mOnOverscrollListeners = new ArrayList<OnOverscrollListener>();

    private int mOverScrollByDeltaX;

    private int mOverScrollByDeltaY;

    public OverscrollListView(Context context) {
        super(context);
    }

    public OverscrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverscrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addOverScrollListener(OnOverscrollListener overScrollListenerOn) {
        mOnOverscrollListeners.add(overScrollListenerOn);
    }

    public void removeOverscrollListener(OnOverscrollListener onOverscrollListener) {
        mOnOverscrollListeners.remove(onOverscrollListener);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
            int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY,
            boolean isTouchEvent) {
        this.mOverScrollByDeltaX = deltaX;
        this.mOverScrollByDeltaY = deltaY;
        final boolean result = super
                .overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                        maxOverScrollX, maxOverScrollY, isTouchEvent);
        return result;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (!mOnOverscrollListeners.isEmpty() && (clampedX || clampedY)) {
            for (OnOverscrollListener listener : mOnOverscrollListeners) {
                listener.onOverscroll(mOverScrollByDeltaX, mOverScrollByDeltaY, clampedX,
                        clampedY);
            }
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }
}
