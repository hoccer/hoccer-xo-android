package com.hoccer.xo.android.view;

import android.content.Context;
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
    private AspectImageView mAvatarImage;
    private View mPresenceIndicator;

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        initializeView();
    }

    private void initializeView() {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_avatar, null);
        this.addView(layout);
        mAvatarImage = (AspectImageView) this.findViewById(R.id.avatar_image);
        mPresenceIndicator = this.findViewById(R.id.presence_indicator_view);
    }

    public ImageView getAvatarImage() {
        return mAvatarImage;
    }

    public void setAvatarImage(String avatarImageUrl) {
        ImageLoader.getInstance().displayImage(avatarImageUrl, mAvatarImage);
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
