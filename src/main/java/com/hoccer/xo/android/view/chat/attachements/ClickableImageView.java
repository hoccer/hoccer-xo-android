package com.hoccer.xo.android.view.chat.attachements;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import org.apache.log4j.Logger;

public class ClickableImageView extends ImageView implements View.OnLongClickListener, View.OnClickListener {

    private static final Logger LOG = Logger.getLogger(ClickableImageView.class);

    private IClickableImageViewListener mClickListener;

    public ClickableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnLongClickListener(this);
        setOnClickListener(this);
    }

    public void setClickableImageViewListener(IClickableImageViewListener clickableImageViewListener) {
        mClickListener = clickableImageViewListener;
    }

    @Override
    public void onClick(View view) {
        LOG.debug("handling click.");

        if (mClickListener != null) {
            mClickListener.onImageViewClick(this);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        LOG.debug("handling long click.");

        if (mClickListener != null) {
            mClickListener.onImageViewLongClick(this);
        }
        return true;
    }
}
