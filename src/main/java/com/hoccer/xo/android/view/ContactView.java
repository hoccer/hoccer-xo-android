package com.hoccer.xo.android.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.hoccer.talk.content.ContentDisposition;
import com.hoccer.talk.content.IContentObject;
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

public class ContactView extends RelativeLayout implements View.OnClickListener {

    private static final Logger LOG = Logger.getLogger(ContactView.class);

    Activity mActivity;

    RelativeLayout mRoot;
    ImageView mAvatarImage;
    TextView mNameText;
    ImageButton mShowButton;
    ImageButton mImportButton;

    IContentObject mContent;

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
        mRoot = (RelativeLayout)inflate(activity, R.layout.content_vcard, null);
        mRoot.setVisibility(INVISIBLE);
        addView(mRoot);
        mAvatarImage = (ImageView)findViewById(R.id.vcard_avatar);
        mNameText = (TextView)findViewById(R.id.vcard_name);
        mShowButton = (ImageButton)findViewById(R.id.vcard_show_button);
        mShowButton.setOnClickListener(this);
        mImportButton = (ImageButton)findViewById(R.id.vcard_import_button);
        mImportButton.setOnClickListener(this);
        update();
    }

    private boolean isContentImported() {
        return mContent != null
                && mContent.getContentUrl() != null
                && !mContent.getContentUrl().startsWith("file://")
                && !mContent.getContentUrl().startsWith("content://media/external/file");
    }

    private boolean isContentImportable() {
        return mContent != null
                && mContent.getContentDisposition() == ContentDisposition.DOWNLOAD
                && !isContentImported();
    }

    private boolean isContentShowable() {
        return mContent != null
                && mContent.getContentDisposition() != ContentDisposition.SELECTED
                && isContentImported();
    }

    private boolean isContentChanged(IContentObject newContent) {
        return mContent == null
                || (newContent.getContentDataUrl() != null
                    && !newContent.getContentDataUrl().equals(mContent.getContentDataUrl()));
    }

    private void update() {
        LOG.debug("update()");
        if(mActivity != null) {
            if(isContentImportable()) {
                mImportButton.setVisibility(VISIBLE);
            } else {
                mImportButton.setVisibility(GONE);
            }
            if(isContentShowable()) {
                mShowButton.setVisibility(VISIBLE);
            } else {
                mShowButton.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mImportButton) {
            LOG.debug("onClick(importButton)");
            if(isContentImportable()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mContent.getContentDataUrl()), mContent.getContentType());
                mActivity.startActivity(intent);
            }
        }
        if(v == mShowButton) {
            LOG.debug("onClick(showButton)");
            if(isContentShowable()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContent.getContentUrl()));
                mActivity.startActivity(intent);
            }
        }
    }

    public void showContent(final IContentObject contentObject) {
        String contentUri = contentObject.getContentDataUrl();
        LOG.debug("showContent(" + contentUri + ")");

        // cancel running refresh
        if(mRefreshFuture != null) {
            mRefreshFuture.cancel(true);
            mRefreshFuture = null;
        }

        // schedule new refresh if needed
        if(isContentChanged(contentObject)) {
            mRoot.setVisibility(INVISIBLE);

            mContent = contentObject;

            // schedule a new refresh
            ScheduledExecutorService executor = XoApplication.getExecutor();
            mRefreshFuture = executor.schedule(new Runnable() {
                @Override
                public void run() {
                    update();
                    refresh(mContent.getContentDataUrl());
                }
            }, 0, TimeUnit.MILLISECONDS);
        }

        update();
    }

    private void checkInterrupt() throws InterruptedException {
        if(Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private void refresh(final String contentUri) {
        LOG.debug("refresh(" + contentUri + ")");
        try {
            // open input stream for given content
            InputStream is = openStreamForContent(contentUri);
            if(is == null) {
                LOG.error("could not open content");
                return;
            }

            checkInterrupt();

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

            checkInterrupt();

            // get the name of the contact
            final String name = card.getFormattedName().getValue();

            // get and load the first photo of the contact
            final List<PhotoType> photos = card.getPhotos();
            PhotoType photo = null;
            if(!photos.isEmpty()) {
                photo = photos.get(0);
            }

            final byte[] photoData = photo != null ? photo.getData() : null;

            checkInterrupt();

            // refresh ui
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRoot.setVisibility(VISIBLE);
                    mNameText.setText(name);
                    Bitmap photoBitmap = null;
                    if(photoData != null) {
                        photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                    }
                    if(photoBitmap != null) {
                        mAvatarImage.setImageDrawable(new BitmapDrawable(photoBitmap));
                    } else {
                        mAvatarImage.setImageResource(R.drawable.avatar_default_contact);
                    }
                    update();
                }
            });

            mRefreshFuture = null;

        } catch (InterruptedException e) {
            LOG.debug("refresh interrupted");
            Thread.currentThread().interrupt();
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
