package com.hoccer.xo.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.MusicViewerFragment;
import com.hoccer.xo.release.R;

public class MusicViewerActivity extends XoActivity {

    private MusicViewerFragment mMusicViewerFragment;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_music_viewer;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableUpNavigation();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mMusicViewerFragment = (MusicViewerFragment) fragmentManager.findFragmentById(R.id.music_viewer_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        boolean result = super.onCreateOptionsMenu(menu);

        menu.findItem(R.id.menu_music_viewer).setVisible(false);

        return result;
    }
}