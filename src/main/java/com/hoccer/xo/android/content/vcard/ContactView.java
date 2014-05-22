package com.hoccer.xo.android.content.vcard;

import com.hoccer.talk.content.ContentDisposition;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.whitelabel.gw.release.R;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class ContactView extends RelativeLayout implements View.OnClickListener {

    private static final Logger LOG = Logger.getLogger(ContactView.class);

    XoActivity mActivity;

    RelativeLayout mRoot;

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
        mActivity = (XoActivity)activity;
        mRoot = (RelativeLayout) inflate(activity, R.layout.content_vcard, null);
        mRoot.setVisibility(INVISIBLE);
        addView(mRoot);
        mNameText = (TextView) findViewById(R.id.tv_vcard_name);
        mShowButton = (ImageButton) findViewById(R.id.ib_vcard_show_button);
        mShowButton.setOnClickListener(this);
        mImportButton = (ImageButton) findViewById(R.id.ib_vcard_import_button);
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
        if (mActivity != null) {
            if (isContentImportable()) {
                mImportButton.setVisibility(VISIBLE);
            } else {
                mImportButton.setVisibility(GONE);
            }
            if (isContentShowable()) {
                mShowButton.setVisibility(VISIBLE);
            } else {
                mShowButton.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mImportButton) {
            LOG.debug("onClick(importButton)");
            if (isContentImportable()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mContent.getContentDataUrl()),
                        mContent.getContentType());
                mActivity.startExternalActivity(intent);
            }
        }
        if (v == mShowButton) {
            LOG.debug("onClick(showButton)");
            if (isContentShowable()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContent.getContentUrl()));
                mActivity.startExternalActivity(intent);
            }
        }
    }

    public void showContent(final IContentObject contentObject, boolean isLightTheme) {
        String contentUri = contentObject.getContentDataUrl();
        LOG.debug("showContent(" + contentUri + ")");

        initTextViews(isLightTheme);
        initImageButton(isLightTheme);

        // schedule new refresh if needed
        if (isContentChanged(contentObject)) {
            // cancel running refresh
            if (mRefreshFuture != null) {
                mRefreshFuture.cancel(true);
                mRefreshFuture = null;
            }

            mRoot.setVisibility(INVISIBLE);

            mContent = contentObject;

            // schedule a new refresh
            ScheduledExecutorService executor = XoApplication.getExecutor();
            mRefreshFuture = executor.schedule(new Runnable() {
                @Override
                public void run() {
                    refresh(mContent.getContentDataUrl());
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    private void initImageButton(boolean isLightTheme) {
        int imageResource = isLightTheme ? R.drawable.ic_dark_contact : R.drawable.ic_light_contact;

        ImageButton ib1 = (ImageButton) findViewById(R.id.ib_vcard_import_button);
        ImageButton ib2= (ImageButton) findViewById(R.id.ib_vcard_show_button);
        ib1.setImageResource(imageResource);
        ib2.setImageResource(imageResource);
    }

    private void initTextViews(boolean isLightTheme) {
        int textColor = isLightTheme ? Color.BLACK : Color.WHITE;

        TextView name = (TextView) findViewById(R.id.tv_vcard_name);
        TextView description = (TextView) findViewById(R.id.tv_vcard_description);
        name.setTextColor(textColor);
        description.setTextColor(textColor);
    }

    private void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private void refresh(final String contentUri) {
        LOG.debug("refresh(" + contentUri + ")");
        try {
            // open input stream for given content
            InputStream is = openStreamForContent(contentUri);
            if (is == null) {
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
            final String name;
            if(card.getFormattedName() != null) {
                name = card.getFormattedName().getValue();
            } else {
                name = "Contact";
            }

            checkInterrupt();

            // refresh ui
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LOG.debug("updating ui");
                    mRoot.setVisibility(VISIBLE);
                    mNameText.setText(name);
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
        if (is == null) {
            LOG.error("don't know how to open " + contentUri);
        }
        return is;
    }

}
