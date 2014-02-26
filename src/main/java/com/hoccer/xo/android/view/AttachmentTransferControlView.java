package com.hoccer.xo.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import com.hoccer.xo.release.R;

public class AttachmentTransferControlView extends View {

    private RectF mInnerWheel = new RectF();
    private RectF mOuterWheel = new RectF();

    private float[] mArrowPause;
    private float[] mArrowPlay;

    private Paint mInnerWheelPaint = new Paint();
    private Paint mOuterWheelPaint = new Paint();

    private float mProgressCompleted = 360;
    private float mShownProgress = 0;
    private int mLayoutWidth;
    private int mLayoutHeight;
    private int mFileSize;
    private boolean mPlay = true;

    private int mOuterWheelSize = 0;
    private int mInnerWheelSize = 0;
    private int mWheelDiameter = 0;
    private int mWheelColor = 0;

    private float mStepIndegrees = 0.5f;
    private boolean mGoneAfterFinished = false;

    private Handler spinHandler;

    {
        spinHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                invalidate();
                if (mShownProgress < mProgressCompleted && mPlay) {
                    mShownProgress += mStepIndegrees;
                    spinHandler.sendEmptyMessageDelayed(0, 1);
                } else if (mGoneAfterFinished) {
                    setVisibility(GONE);
                }
            }
        };
    }

    public AttachmentTransferControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttributes(context.obtainStyledAttributes(attrs,
                R.styleable.TransferWheel));
    }

    public AttachmentTransferControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context.obtainStyledAttributes(attrs,
                R.styleable.TransferWheel));
    }

    public AttachmentTransferControlView(Context context) {
        super(context);
    }

    private void parseAttributes(TypedArray a) {
        mOuterWheelSize = (int) a.getDimension(R.styleable.TransferWheel_outerWheelSize,
                mOuterWheelSize);
        mInnerWheelSize = (int) a.getDimension(R.styleable.TransferWheel_innerWheelSize,
                mInnerWheelSize);
        mWheelDiameter = (int) a.getDimension(R.styleable.TransferWheel_wheelDiameter,
                mWheelDiameter);
        mWheelColor = a.getColor(R.styleable.TransferWheel_wheelColor,
                mWheelColor);
        a.recycle();
    }

    private void setupPaint() {
        mInnerWheelPaint.setColor(mWheelColor);
        mInnerWheelPaint.setStyle(Paint.Style.STROKE);
        mInnerWheelPaint.setStrokeWidth(mInnerWheelSize);
        mInnerWheelPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mOuterWheelPaint.setColor(mWheelColor);
        mOuterWheelPaint.setStyle(Paint.Style.STROKE);
        mOuterWheelPaint.setStrokeWidth(mOuterWheelSize);
        mOuterWheelPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    private void setupWheels() {
        int mCircleCenterX = mLayoutWidth/2;
        int mCircleCenterY = mLayoutHeight/2;
        float outerRadius = mWheelDiameter/2.0f;
        mOuterWheel = new RectF(mCircleCenterX - outerRadius, mCircleCenterY - outerRadius,
                mCircleCenterX + outerRadius, mCircleCenterY + outerRadius);
        float innerRadius = outerRadius - mInnerWheelSize/2.0f;
        mInnerWheel = new RectF(mCircleCenterX - innerRadius, mCircleCenterY - innerRadius,
            mCircleCenterX + innerRadius, mCircleCenterY + innerRadius);
        //arrows
        final float lineOffset = mWheelDiameter / 3;
        final float rowOffset = mWheelDiameter / 18;
        final float pauseOffset = mWheelDiameter / 12;
        //play arrow
        mArrowPlay =  new float[12];
        mArrowPlay[0] = mCircleCenterX;
        mArrowPlay[1] = mCircleCenterY - innerRadius + lineOffset;
        mArrowPlay[2] = mCircleCenterX;
        mArrowPlay[3] = mCircleCenterY + innerRadius - lineOffset;

        mArrowPlay[4] = mCircleCenterX - (((2 * (mArrowPlay[3] - mArrowPlay[1])) / 3))/2;
        mArrowPlay[5] = mCircleCenterY + rowOffset;
        mArrowPlay[6] = mCircleCenterX;
        mArrowPlay[7] = mCircleCenterY + innerRadius - lineOffset;

        mArrowPlay[8] = mCircleCenterX + (((2 * (mArrowPlay[3] - mArrowPlay[1])) / 3))/2;
        mArrowPlay[9] = mCircleCenterY + rowOffset;
        mArrowPlay[10] = mCircleCenterX;
        mArrowPlay[11] = mCircleCenterY + innerRadius - lineOffset;
        //pause arrow
        mArrowPause =  new float[8];
        mArrowPause[0] = mCircleCenterX - pauseOffset;
        mArrowPause[1] = mCircleCenterY - innerRadius + lineOffset;
        mArrowPause[2] = mCircleCenterX - pauseOffset;
        mArrowPause[3] = mCircleCenterY + innerRadius - lineOffset;
        mArrowPause[4] = mCircleCenterX + pauseOffset;
        mArrowPause[5] = mCircleCenterY - innerRadius + lineOffset;
        mArrowPause[6] = mCircleCenterX + pauseOffset;
        mArrowPause[7] = mCircleCenterY + innerRadius - lineOffset;



    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mLayoutWidth = w;
        mLayoutHeight = h;

        setupPaint();
        setupWheels();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mOuterWheel, -90, 360, false, mOuterWheelPaint);
        canvas.drawArc(mInnerWheel, -90, mShownProgress, false, mInnerWheelPaint);
        if (mPlay) {
            canvas.drawLines(mArrowPlay, mOuterWheelPaint);
        } else {
            canvas.drawLines(mArrowPause, mOuterWheelPaint);
        }
    }

    public void setMax(int length) {
        mFileSize = length;
    }

    public void setProgress(int progress) {
        float percentage = progress / (float)mFileSize;
        mProgressCompleted = 360 * percentage;
        spinHandler.sendEmptyMessage(0);
    }

    public void clean() {
        mGoneAfterFinished = false;
        mPlay = true;
        mStepIndegrees = 0.5f;
        mProgressCompleted = 0;
        mShownProgress = 0;
        invalidate();
    }

    public boolean setCompletedAndGone() {
        mProgressCompleted = 360;
        mGoneAfterFinished = true;
        mStepIndegrees = 1;
        spinHandler.sendEmptyMessage(0);
        return false;
    }

    public void pause() {
        mPlay = false;
    }

    public void play() {
        mPlay = true;
    }
}
