package com.hoccer.xo.android.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

@TargetApi(17)
public class AsyncBitmapBlurTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {

    private Context             mContext;
    private float               mRadius;
    private int                 mRenderCycles;
    private BlurProgressHandler mProgressHandler;

    public AsyncBitmapBlurTask(Context pContext, float pRadius, int pRenderCycles,
                               BlurProgressHandler pProgressHandler) {
        this(pContext, pRadius, pRenderCycles);
        mProgressHandler = pProgressHandler;
    }

    public AsyncBitmapBlurTask(Context pContext, float pRadius, int pRenderCycles) {
        mContext = pContext;
        mRadius = pRadius;
        mRenderCycles = pRenderCycles;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        Bitmap result = params[0];
        for (int i = 0; i < mRenderCycles; i++) {
            result = blur(result);
            publishProgress(result);
        }
        return blur(result);
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        if (mProgressHandler != null) {
            mProgressHandler.onBlurProgressUpdate(values);
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (mProgressHandler != null) {
            mProgressHandler.onBlurFinished(result);
        }
    }

    private Bitmap blur(Bitmap bitmapMaster) {
        RenderScript rs = RenderScript.create(mContext);
        Allocation input = Allocation.createFromBitmap(rs, bitmapMaster);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(mRadius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmapMaster);
        return bitmapMaster;
    }

    public interface BlurProgressHandler {
        public void onBlurProgressUpdate(Bitmap... pBitmaps);

        public void onBlurFinished(Bitmap pBlurredBitmap);

    }
}
