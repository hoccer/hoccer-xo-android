package com.hoccer.xo.android.view;

import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * A view holding an AspectImageView and a presence indicator.
 */
public class AvatarView extends LinearLayout {

    private Context mContext;
    private String mDefaultAvatarImageUrl;
    private AspectImageView mAvatarImage;
    private View mPresenceIndicator;
    private DisplayImageOptions mDefaultOptions;
    private float mCornerRadius = 0.0f;

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        applyAttributes(context, attrs);
        initializeView();
    }

    private void initializeView() {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_avatar, null);
        addView(layout);

        mAvatarImage = (AspectImageView) this.findViewById(R.id.avatar_image);
        mPresenceIndicator = this.findViewById(R.id.presence_indicator_view);

        float scale = getResources().getDisplayMetrics().density;
        int pixel = (int) (mCornerRadius * scale + 0.5f);
        mDefaultOptions = new DisplayImageOptions.Builder().cloneFrom(XoApplication.getContentImageOptions()).displayer(new RoundedBitmapDisplayer(pixel)).build();
        setAvatarImage(mDefaultAvatarImageUrl);
    }

    private void applyAttributes(Context context, AttributeSet attributes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attributes, R.styleable.AvatarView, 0, 0);
        try {
            mDefaultAvatarImageUrl = a.getString(R.styleable.AvatarView_defaultAvatarImageUrl);
            mCornerRadius = a.getFloat(R.styleable.AvatarView_cornerRadius, 0.0f);
        } finally {
            a.recycle();
        }
    }

    /**
     * Sets the avatar image. Value can be null. Uses default avatar image url instead (if specified).
     *
     * @param avatarImageUrl Url of the given image resource  to load.
     */
    public void setAvatarImage(String avatarImageUrl) {
        if(isInEditMode()) {
            ImageView avatar = (ImageView) this.findViewById(R.id.avatar_image);
            avatar.setImageResource(R.drawable.avatar_default_contact);
        } else {
            mAvatarImage.setVisibility(View.VISIBLE);

            if (avatarImageUrl != null) {
                ImageLoader.getInstance().displayImage(avatarImageUrl, mAvatarImage, mDefaultOptions, null);
            } else if (mDefaultAvatarImageUrl != null) {
                ImageLoader.getInstance().displayImage(mDefaultAvatarImageUrl, mAvatarImage, mDefaultOptions, null);
            } else {
                mAvatarImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Sets the url for the default avatar image. Value can be null.
     *
     * @param defaultAvatarImageUrl Url of the given image resource  to load.
     */
    public void setDefaultAvatarImageUrl(String defaultAvatarImageUrl) {
        mDefaultAvatarImageUrl = defaultAvatarImageUrl;
    }

    public void setPresence(boolean isPresent) {
        if (isPresent) {
            mPresenceIndicator.setVisibility(View.VISIBLE);
        } else {
            mPresenceIndicator.setVisibility(View.INVISIBLE);
        }
    }

}
