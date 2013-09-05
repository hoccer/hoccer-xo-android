package com.hoccer.talk.android.views;

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
import com.hoccer.xo.R;
import com.hoccer.talk.android.TalkApplication;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.types.PhotoType;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class ContactView extends RelativeLayout {

    private static final Logger LOG = Logger.getLogger(ContactView.class);

    Activity mActivity;

    ImageView mAvatarImage;
    TextView mNameText;
    ImageButton mShowButton;

    ScheduledFuture<?> mLoadFuture;

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
        LOG.debug("show vcard " + contentUri);
        if(mLoadFuture != null) {
            mLoadFuture.cancel(true);
            mLoadFuture = null;
        }

        ScheduledExecutorService executor = TalkApplication.getExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                LOG.debug("refreshing vcard " + contentUri);

                InputStream is = null;

                if(contentUri.startsWith("content://")) {
                    ContentResolver resolver = getContext().getContentResolver();
                    try {
                        is = resolver.openInputStream(Uri.parse(contentUri));
                    } catch (FileNotFoundException e) {
                        LOG.error("could not find vcard", e);
                        return;
                    }
                }
                if(contentUri.startsWith("file://")) {
                    try {
                        URL url = new URL(contentUri);
                        is = url.openStream();
                    } catch (MalformedURLException e) {
                        LOG.error("invalid file uri", e);
                        return;
                    } catch (IOException e) {
                        LOG.error("could not open file", e);
                        return;
                    }
                }
                if(is == null) {
                    LOG.error("don't know how to open " + contentUri);
                    return;
                }

                VCard card = null;
                try {
                    card = Ezvcard.parse(is).first();
                } catch (IOException e) {
                    LOG.error("could not read vcard", e);
                    return;
                } catch (Throwable t) {
                    LOG.error("uh!?", t);
                    return;
                }

                final String name = card.getFormattedName().getValue();
                final List<PhotoType> photos = card.getPhotos();
                PhotoType photo = null;
                if(!photos.isEmpty()) {
                    photo = photos.get(0);
                }
                final PhotoType photoFinal = photo;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LOG.debug("setting up gui");
                        mNameText.setText(name);
                        if(photoFinal != null) {
                            byte[] photoData = photoFinal.getData();
                            Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                            mAvatarImage.setImageDrawable(new BitmapDrawable(photoBitmap));
                        } else {
                            mAvatarImage.setImageResource(R.drawable.avatar_default_contact);
                        }
                    }
                });
            }
        });

    }

}
