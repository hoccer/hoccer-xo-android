package com.hoccer.xo.android.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.release.R;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.types.PhotoType;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ContactView extends RelativeLayout {

    private static final Logger LOG = Logger.getLogger(ContactView.class);

    Activity mActivity;

    ImageView mAvatarImage;
    TextView mNameText;
    ImageButton mShowButton;

    ScheduledFuture<?> mRefreshFuture;

    public ContactView(Activity activity) {
        super(activity);
        initialize(activity);
    }

    public ContactView(Activity activity, AttributeSet attrs) {
        super(activity, attrs);
        initialize(activity);
    }

    public ContactView(Activity activity, AttributeSet attrs, int defStyle) {
        super(activity, attrs, defStyle);
        initialize(activity);
    }

    private void initialize(Activity activity) {
        mActivity = activity;
        addView(inflate(activity, R.layout.content_vcard, null));
        mAvatarImage = (ImageView)findViewById(R.id.vcard_avatar);
        mNameText = (TextView)findViewById(R.id.vcard_name);
        mShowButton = (ImageButton)findViewById(R.id.vcard_view);
    }

    public void showContent(final String contentUri) {
        LOG.debug("showContent(" + contentUri + ")");

        // cancel running refresh
        if(mRefreshFuture != null) {
            mRefreshFuture.cancel(true);
            mRefreshFuture = null;
        }

        // schedule a new refresh
        ScheduledExecutorService executor = XoApplication.getExecutor();
        mRefreshFuture = executor.schedule(new Runnable() {
            @Override
            public void run() {
                refresh(contentUri);
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    private void refresh(final String contentUri) {
        LOG.debug("refresh(" + contentUri + ")");

        // open input stream for given content
        InputStream is = openStreamForContent(contentUri);
        if(is == null) {
            LOG.error("could not open content");
            return;
        }

        // parse the vcard behind the uri
        VCard card = null;
        try {
            card = Ezvcard.parse(is).first();
        } catch (IOException e) {
            LOG.error("could not parse vcard", e);
            return;
        } catch (Throwable t) {
            LOG.error("could not parse vcard", t);
            return;
        }

        // get the name of the contact
        final String name = card.getFormattedName().getValue();

        // get and load the first photo of the contact
        final List<PhotoType> photos = card.getPhotos();
        PhotoType photo = null;
        if(!photos.isEmpty()) {
            photo = photos.get(0);
        }

        // refresh the name text
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNameText.setText(name);
            }
        });

        // refresh the photo, if there is one
        if(photo != null) {
            final byte[] photoData = photo.getData();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                    if(photoBitmap != null) {
                        mAvatarImage.setImageDrawable(new BitmapDrawable(photoBitmap));
                    } else {
                        mAvatarImage.setImageResource(R.drawable.avatar_default_contact);
                    }
                }
            });
        }
    }

    private InputStream openStreamForContent(String contentUri) {
        LOG.trace("openStreamForContent(" + contentUri + ")");
        InputStream is = null;
        ContentResolver resolver = getContext().getContentResolver();
        try {
            is = resolver.openInputStream(Uri.parse(contentUri));
        } catch (FileNotFoundException e) {
            LOG.error("could not find vcard", e);
        }
        if(is == null) {
            LOG.error("don't know how to open " + contentUri);
        }
        return is;
    }

}
