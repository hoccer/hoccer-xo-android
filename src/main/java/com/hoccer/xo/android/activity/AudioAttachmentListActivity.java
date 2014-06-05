package com.hoccer.xo.android.activity;

import android.os.Bundle;
import android.view.Menu;
import com.hoccer.xo.android.base.XoActionbarActivity;
import com.hoccer.xo.android.fragment.AudioAttachmentListFragment;
import com.hoccer.xo.test.R;

public class AudioAttachmentListActivity extends XoActionbarActivity {

    public static final String EXTRA_CLIENT_CONTACT_ID = "com.hoccer.xo.android.activity.EXTRA_CLIENT_CONTACT_ID";
    private AudioAttachmentListFragment mAudioAttachmentListFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_audio_attachment_list;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableUpNavigation();
        mAudioAttachmentListFragment = (AudioAttachmentListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.audio_attachment_list_fragment);


    }
}