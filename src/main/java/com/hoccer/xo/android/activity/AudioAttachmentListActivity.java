package com.hoccer.xo.android.activity;

import android.os.Bundle;
import android.view.Menu;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.AudioAttachmentListFragment;
import com.hoccer.xo.release.R;

public class AudioAttachmentListActivity extends XoActivity {

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
        mAudioAttachmentListFragment = (AudioAttachmentListFragment) getSupportFragmentManager().findFragmentById(R.id.audio_attachment_list_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        menu.findItem(R.id.menu_audio_attachment_list).setVisible(false);

        return result;
    }
}
