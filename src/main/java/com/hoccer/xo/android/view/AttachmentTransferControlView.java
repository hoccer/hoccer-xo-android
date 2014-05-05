package com.hoccer.xo.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.whitelabel.gw.release.R;

public class AttachmentTransferControlView extends View {
    private final float SPINNER_LENGTH = 50;
    private final float SPINNER_STEP = 1.5f;

    private RectF mInnerWheel = new RectF();
    private RectF mOuterWheel = new RectF();

    private Paint mInnerWheelPaint = new Paint();
    private Paint mOuterWheelPaint = new Paint();
    private Paint mTextPaint = new Paint();

    private float mProgressCompleted = 360;
    private float mSpinnerLength = 0;
    private float mSpinningProgress = 0;
    private float mShownProgress = 0;
    private int mLayoutWidth;
    private int mLayoutHeight;
    private int mFileSize;

    private int mOuterWheelSize = 0;
    private int mInnerWheelSize = 0;
    private int mWheelDiameter = 0;
    private int mWheelColor = 0;

    private float[] mArrowPause;
    private float[] mArrowDownload;
    private float[] mArrowUpload;

    private boolean mIsInited = false;
    private boolean mPause = false;
    private boolean mGone = false;
    private boolean mIsDownloadingProcess;
    private boolean mStopSpinning;
    private float mStepInDegrees = 0.5f;
    private boolean mGoneAfterFinished = false;
    private boolean mIsSpinning = false;
    private boolean mIsStatesEnable = true;
    private boolean mIsEncrypting = true;

    private Handler progressHandler;


    {
        progressHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                invalidate();
                if (mPause) {
                    return;
                }
                if (mShownProgress < mProgressCompleted) {
                    mShownProgress += mStepInDegrees;
                    progressHandler.sendEmptyMessageDelayed(0, 1);
                    return;
                } else if (mGoneAfterFinished) {
                    mGone = true;
                    return;
                }
                //spinning
                if (mIsSpinning) {
                    if (mIsEncrypting) {
                        spinEncrypting();
                    } else {
                        spinDecrypting();
                    }
                }
            }

            private void spinEncrypting() {
                if (mSpinnerLength < SPINNER_LENGTH) {
                    mSpinnerLength += SPINNER_STEP;
                } else {
                    mSpinningProgress += SPINNER_STEP;
                }
                if (mSpinningProgress > 360) {
                    mSpinningProgress = 0;
                }
                if (mStopSpinning && mSpinningProgress == 0) {
                    mIsSpinning = false;
                    mShownProgress = SPINNER_LENGTH;
                    return;
                }
                progressHandler.sendEmptyMessageDelayed(0, 1);
            }

            private void spinDecrypting() {
                if (mSpinnerLength > SPINNER_LENGTH) {
                    mSpinnerLength -= SPINNER_STEP;
                    mSpinningProgress += SPINNER_STEP;
                } else {
                    mSpinnerLength += SPINNER_STEP;
                }
                if (mSpinningProgress > 360) {
                    mSpinningProgress = 0;
                }
                if (mStopSpinning && mSpinningProgress == 0) {
                    clean();
                    mGone = true;
                    mIsInited = false;
                    return;
                }
                progressHandler.sendEmptyMessageDelayed(0, 1);

            }
        };
    }

    private int mTextMargin;
    private int mTextSize;
    private String mText;

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
        mOuterWheelSize = (int) a.getDimension(R.styleable.TransferWheel_outerWheelSize, mOuterWheelSize);
        mInnerWheelSize = (int) a.getDimension(R.styleable.TransferWheel_innerWheelSize, mInnerWheelSize);
        mWheelDiameter = (int) a.getDimension(R.styleable.TransferWheel_wheelDiameter, mWheelDiameter);
        mTextMargin = (int) a.getDimension(R.styleable.TransferWheel_textMargin, mTextMargin);
        mTextSize = a.getInt(R.styleable.TransferWheel_textSize, mTextSize);
        mText = a.getString(R.styleable.TransferWheel_text);
        mWheelColor = a.getColor(R.styleable.TransferWheel_wheelColor, mWheelColor);
        mIsStatesEnable = a.getBoolean(R.styleable.TransferWheel_enableStates, mIsStatesEnable);
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

        mTextPaint.setColor(mWheelColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void setupWheels() {
        int circleCenterX = mLayoutWidth/2;
        int circleCenterY = mLayoutHeight/2;
        float outerRadius = mWheelDiameter/2.0f;
        mOuterWheel = new RectF(circleCenterX - outerRadius, circleCenterY - outerRadius,
                circleCenterX + outerRadius, circleCenterY + outerRadius);
        float innerRadius = outerRadius - mInnerWheelSize/2.0f;
        mInnerWheel = new RectF(circleCenterX - innerRadius, circleCenterY - innerRadius,
                circleCenterX + innerRadius, circleCenterY + innerRadius);

        //arrows
        initDownloadArrow(circleCenterX, circleCenterY, innerRadius, mWheelDiameter);
        initUploadArrow(circleCenterX, circleCenterY, innerRadius, mWheelDiameter);
        initPauseArrow(circleCenterX, circleCenterY, innerRadius, mWheelDiameter);
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
        if (mIsSpinning) {
            canvas.drawArc(mInnerWheel, mSpinningProgress - 90, mSpinnerLength, false, mInnerWheelPaint);
        } else {
            canvas.drawArc(mInnerWheel, -90, mShownProgress, false, mInnerWheelPaint);
        }
        if (mIsStatesEnable) {
            if (!mPause) {
                canvas.drawLines(mArrowPause, mOuterWheelPaint);
            } else if (mIsDownloadingProcess) {
                canvas.drawLines(mArrowDownload, mOuterWheelPaint);
            } else {
                canvas.drawLines(mArrowUpload, mOuterWheelPaint);
            }
        }
        canvas.drawText(mText, mLayoutWidth/2, mLayoutHeight/2 + mWheelDiameter/2.0f + mTextMargin, mTextPaint);
    }

    public void setMax(int length) {
        mFileSize = length;
    }

    public void setProgress(int progress) {
        mPause = false;
        float percentage = progress / (float)mFileSize;
        mProgressCompleted = 360 * percentage;
        progressHandler.sendEmptyMessage(0);
    }

    public void setProgressImmediately(int progress) {
        float percentage = progress / (float)mFileSize;
        mShownProgress = 360 * percentage;
        invalidate();
    }

    public void prepareToUpload() {
        if (!mIsInited) {
            mIsInited = true;
            mIsDownloadingProcess = false;
            prepareToEncrypt();
            clean();
        }
    }

    public void prepareToDownload() {
        if (!mIsInited) {
            mIsInited = true;
            mIsDownloadingProcess = true;
            prepareToDecrypt();
//            clean();
        }
    }

    private void clean() {
        mGoneAfterFinished = false;
        mGone = false;
        mIsSpinning = false;
        mStepInDegrees = 0.5f;
        mProgressCompleted = 0;
        mShownProgress = 0;
        invalidate();
    }

    public boolean completeAndGone() {
        mProgressCompleted = 360;
        mGoneAfterFinished = true;
        mStepInDegrees = 1;
        progressHandler.sendEmptyMessage(0);
        mIsInited = false;
        return false;
    }

    public void finishSpinningAndProceed() {
        mStopSpinning = true;
        mIsInited = false;
    }

    private void prepareToEncrypt() {
        mIsEncrypting = true;
        mSpinningProgress = 0;
        mSpinnerLength = 0;
    }

    private void prepareToDecrypt() {
        mIsEncrypting = false;
        mSpinningProgress = 0;
        mSpinnerLength = 360;
    }

    public void spin() {
        if (!mIsSpinning || mPause) {
            mIsSpinning = true;
            mStopSpinning = false;
            mPause = false;
            progressHandler.sendEmptyMessage(0);
        }
    }

    public void pause() {
        mPause = true;
    }

    public boolean isGoneAfterFinished() {
        return mGone;
    }

    private void initDownloadArrow(float centerX, float centerY, float innerRadius, float wheelDiameter) {
        float offsetFromWheelEdges = wheelDiameter / 3;
        float rowOffset = wheelDiameter / 18;
        mArrowDownload =  new float[12];
        mArrowDownload[0] = centerX;
        mArrowDownload[1] = centerY - innerRadius + offsetFromWheelEdges;
        mArrowDownload[2] = centerX;
        mArrowDownload[3] = centerY + innerRadius - offsetFromWheelEdges;

        mArrowDownload[4] = centerX - (((2 * (mArrowDownload[3] - mArrowDownload[1])) / 3))/2; //It is a half of 2/3 of arrow length B-)
        mArrowDownload[5] = centerY + rowOffset;
        mArrowDownload[6] = centerX;
        mArrowDownload[7] = mArrowDownload[3];

        mArrowDownload[8] = centerX + (((2 * (mArrowDownload[3] - mArrowDownload[1])) / 3))/2;
        mArrowDownload[9] = centerY + rowOffset;
        mArrowDownload[10] = centerX;
        mArrowDownload[11] = mArrowDownload[3];
    }

    private void initUploadArrow(float centerX, float centerY, float innerRadius, float wheelDiameter) {
        float offsetFromWheelEdges = wheelDiameter / 3;
        float rowOffset = wheelDiameter / 18;
        mArrowUpload =  new float[12];
        mArrowUpload[0] = centerX;
        mArrowUpload[1] = centerY - innerRadius + offsetFromWheelEdges;
        mArrowUpload[2] = centerX;
        mArrowUpload[3] = centerY + innerRadius - offsetFromWheelEdges;

        mArrowUpload[4] = centerX - (((2 * (mArrowUpload[3] - mArrowUpload[1])) / 3))/2;
        mArrowUpload[5] = centerY - rowOffset;
        mArrowUpload[6] = centerX;
        mArrowUpload[7] = mArrowUpload[1];

        mArrowUpload[8] = centerX + (((2 * (mArrowUpload[3] - mArrowUpload[1])) / 3))/2;
        mArrowUpload[9] = centerY - rowOffset;
        mArrowUpload[10] = centerX;
        mArrowUpload[11] = mArrowUpload[1];
    }

    private void initPauseArrow(float centerX, float centerY, float innerRadius, float wheelDiameter) {
        float offsetFromWheelEdges = wheelDiameter / 3;
        float pauseOffset = wheelDiameter / 12;
        mArrowPause =  new float[8];
        mArrowPause[0] = centerX - pauseOffset;
        mArrowPause[1] = centerY - innerRadius + offsetFromWheelEdges;
        mArrowPause[2] = centerX - pauseOffset;
        mArrowPause[3] = centerY + innerRadius - offsetFromWheelEdges;

        mArrowPause[4] = centerX + pauseOffset;
        mArrowPause[5] = centerY - innerRadius + offsetFromWheelEdges;
        mArrowPause[6] = centerX + pauseOffset;
        mArrowPause[7] = centerY + innerRadius - offsetFromWheelEdges;
    }

    public void setText(String text) {
        mText = text;
    }
}
