package com.hoccer.xo.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.hoccer.xo.android.base.XoActionbarActivity;
import com.hoccer.xo.android.fragment.AudioAttachmentListFragment;
import com.hoccer.xo.android.fragment.MediaCollectionListFragment;
import com.hoccer.xo.release.R;

public class MediaBrowserActivity extends XoActionbarActivity {

    private AudioAttachmentListFragment mAudioAttachmentListFragment;
    private MediaCollectionListFragment mCollectionListFragment;

    private Integer mContactId;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_media_browser;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableUpNavigation();

        mContactId = -1;
        if (getIntent() != null) {
            mContactId = getIntent().getIntExtra(MessagingActivity.EXTRA_CLIENT_CONTACT_ID, mContactId);
        }

        showAudioAttachmentListFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actitvity_media_browser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_collections:
                showCollectionListFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAudioAttachmentListFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt(AudioAttachmentListFragment.ARG_CLIENT_CONTACT_ID, mContactId);

        mAudioAttachmentListFragment = new AudioAttachmentListFragment();
        mAudioAttachmentListFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_fragment_container, mAudioAttachmentListFragment);
        ft.commit();
    }

    private void showCollectionListFragment() {
        mCollectionListFragment = new MediaCollectionListFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_fragment_container, mCollectionListFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}