package com.hoccer.xo.android.view;

import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A view holding an AspectImageView and a presence indicator.
 */
public class AvatarView extends LinearLayout implements IXoContactListener {

    private Context mContext;
    private String mDefaultAvatarImageUrl;
    private DisplayImageOptions mDefaultOptions;
    private float mCornerRadius = 0.0f;
    private AspectImageView mAvatarImage;
    private View mPresenceIndicator;

    private TalkClientContact mContact;

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
        if (isInEditMode()) {
            mDefaultOptions = new DisplayImageOptions.Builder()
                    .displayer(new RoundedBitmapDisplayer(pixel)).build();
        } else {
            mDefaultOptions = new DisplayImageOptions.Builder()
                    .cloneFrom(XoApplication.getContentImageOptions())
                    .displayer(new RoundedBitmapDisplayer(pixel)).build();
        }
        setAvatarImage(mDefaultAvatarImageUrl);
    }

    private void applyAttributes(Context context, AttributeSet attributes) {
        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attributes, R.styleable.AvatarView, 0, 0);
        try {
            mDefaultAvatarImageUrl = "drawable://" + a.getResourceId(R.styleable.AvatarView_defaultAvatarImageUrl, R.drawable.avatar_default_contact);
            mCornerRadius = a.getFloat(R.styleable.AvatarView_cornerRadius, 0.0f);
        } finally {
            a.recycle();
        }
    }


    public void setContact(TalkClientContact contact) {
        mContact = contact;
        updateAvatar();
        updatePresence();
        XoApplication.getXoClient().registerContactListener(this);
    }

    private void updateAvatar() {
        if(mContact == null) {
            resetAvatar();
            return;
        }
        IContentObject avatar = mContact.getAvatar();
        String avatarUri = avatar == null ? null : avatar.getContentDataUrl();

        if (avatarUri == null) {
            if (mContact.isGroup()) {
                if(mContact.getGroupPresence().isTypeNearby()) {
                    avatarUri = "drawable://" + R.drawable.avatar_default_location;
                } else {
                    avatarUri = "drawable://" + R.drawable.avatar_default_group;
                }
            } else {
                avatarUri = "drawable://" + R.drawable.avatar_default_contact;
            }
        }
        setAvatarImage(avatarUri);
    }

    private void resetAvatar() {
        setAvatarImage(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAvatar();
        updatePresence();
    }

    /**
     * Sets the avatar image. Value can be null. Uses default avatar image url instead (if
     * specified).
     *
     * @param avatarImageUrl Url of the given image resource  to load.
     */
    public void setAvatarImage(final String avatarImageUrl) {
        post(new Runnable() {
            @Override
            public void run() {
                if (isInEditMode()) {
                    ImageView avatar = (ImageView) findViewById(R.id.avatar_image);
                    avatar.setImageResource(R.drawable.avatar_default_contact);
                } else {
                    mAvatarImage.setVisibility(View.VISIBLE);
                    if (avatarImageUrl != null) {
                        ImageLoader.getInstance()
                                .displayImage(avatarImageUrl, mAvatarImage, mDefaultOptions, null);
                    } else if (mDefaultAvatarImageUrl != null) {
                        ImageLoader.getInstance()
                                .displayImage(mDefaultAvatarImageUrl, mAvatarImage, mDefaultOptions,
                                        null);
                    } else {
                        mAvatarImage.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    /**
     * Sets the url for the default avatar image. Value can be null.
     *
     * @param defaultAvatarImageUrl Url of the given image resource  to load.
     */
    private void setDefaultAvatarImageUrl(String defaultAvatarImageUrl) {
        mDefaultAvatarImageUrl = defaultAvatarImageUrl;
    }

    private void updatePresence() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mContact == null) {
                    return;
                }
                TalkPresence presence = null;
                if (mContact.isClient()) {
                    presence = mContact.getClientPresence();
                } else {
                    mPresenceIndicator.setVisibility(View.INVISIBLE);
                    return;
                }
                if (presence != null) {
                    if (presence.isPresent()) {
                        mPresenceIndicator.setVisibility(View.VISIBLE);
                    } else {
                        mPresenceIndicator.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        updatePresence();
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
    }
}
