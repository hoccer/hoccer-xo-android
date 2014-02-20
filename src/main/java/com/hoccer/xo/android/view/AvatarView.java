package com.hoccer.xo.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by juliank on 20.02.14.
 */
public class AvatarView extends LinearLayout {

    private Context mContext;
    private String mDefaultAvatarImageUrl;
    private AspectImageView mAvatarImage;
    private View mPresenceIndicator;

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mDefaultAvatarImageUrl = null;

        this.initializeView();
        this.applyAttributes(context, attrs);
        this.setAvatarImage(null);
    }

    private void initializeView() {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_avatar, null);
        this.addView(layout);
        mAvatarImage = (AspectImageView) this.findViewById(R.id.avatar_image);
        mPresenceIndicator = this.findViewById(R.id.presence_indicator_view);
    }

    private void applyAttributes(Context context, AttributeSet attributes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attributes, R.styleable.AvatarView, 0, 0);
        try {
            mDefaultAvatarImageUrl = a.getString(R.styleable.AvatarView_defaultAvatarImageUrl);
        } finally {
            a.recycle();
        }
    }

    public ImageView getAvatarImage() {
        return mAvatarImage;
    }

    /**
     * Sets the avatar image. Value can be null. Uses default avatar image url instead (if specified).
     *
     * @param avatarImageUrl Url of the given image resource  to load.
     */
    public void setAvatarImage(String avatarImageUrl) {
        mAvatarImage.setVisibility(View.VISIBLE);
        if (avatarImageUrl != null) {
            ImageLoader.getInstance().displayImage(avatarImageUrl, mAvatarImage);
        } else if (mDefaultAvatarImageUrl != null) {
            ImageLoader.getInstance().displayImage(null, mAvatarImage);
        } else {
            mAvatarImage.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets the url for the default avatar image. Value can be null.
     *
     * @param defaultAvatarImageUrl Url of the given image resource  to load.
     */
    public void setDefaultAvatarImageUrl(String defaultAvatarImageUrl) {
        this.mDefaultAvatarImageUrl = defaultAvatarImageUrl;
    }

    public void setPresence(boolean isPresent) {
        if (isPresent) {
            mPresenceIndicator.setVisibility(View.VISIBLE);
        } else {
            mPresenceIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private AspectImageView maskImage(ImageView imageView) {
        AspectImageView maskedImage = null;
        return maskedImage;
    }

}
