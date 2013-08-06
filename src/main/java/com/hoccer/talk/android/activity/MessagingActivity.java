package com.hoccer.talk.android.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Window;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.fragment.MessagingFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;

public class MessagingActivity extends TalkActivity {

    ActionBar mActionBar;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_messaging;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("clientContactId")) {
            int contactId = intent.getIntExtra("clientContactId", -1);
            if(contactId == -1) {
                LOG.error("Invalid ccid");
                return;
            }
            try {
                TalkClientContact contact = getTalkClientDatabase().findClientContactById(contactId);
                converseWithContact(contact);
            } catch (SQLException e) {
                LOG.error("NO EXTRA", e);
            }

        } else {
            LOG.info("NO EXTRA");
        }
    }

    public void converseWithContact(TalkClientContact contact) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        MessagingFragment fragment = (MessagingFragment)fragmentManager.findFragmentById(R.id.activity_messaging_fragment);
        fragment.converseWithContact(contact);

        mActionBar.setTitle(contact.getName());

        /*
        TalkClientDownload download = contact.getAvatarDownload();
        if(download == null || !download.getState().equals(TalkClientDownload.State.COMPLETE)) {
            mActionBar.setLogo(R.drawable.ic_launcher);
        } else {
            File avatarFile = download.getAvatarFile(getAvatarDirectory());
            ImageLoader.getInstance().loadImage("file://" + avatarFile,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            mActionBar.setLogo(new BitmapDrawable(loadedImage)); // XXX apply resources
                        }
                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            mActionBar.setLogo(R.drawable.ic_launcher);
                        }
                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            mActionBar.setLogo(R.drawable.ic_launcher);
                        }
                    });
        }
        */
    }

}
