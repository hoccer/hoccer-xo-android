package com.hoccer.xo.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.hoccer.xo.android.fragment.FullscreenPlayerFragment;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.service.MediaPlayerServiceConnector;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends FragmentActivity {

    private final static Logger LOG = Logger.getLogger(FullscreenPlayerActivity.class);

    private MediaPlayerServiceConnector mMediaPlayerServiceConnector;
    private FullscreenPlayerFragment mPlayerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_player);
        mMediaPlayerServiceConnector = new MediaPlayerServiceConnector();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlayerFragment = (FullscreenPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_fullscreen_player);
        mMediaPlayerServiceConnector.connect(this,
                MediaPlayerService.PLAYSTATE_CHANGED_ACTION,
                new MediaPlayerServiceConnector.Listener() {
                    @Override
                    public void onConnected(MediaPlayerService service) {
                        mPlayerFragment.initView();
                    }
                    @Override
                    public void onDisconnected() {
                    }
                    @Override
                    public void onAction(String action, MediaPlayerService service) {
                        if (service.isStopped()) {
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayerServiceConnector.disconnect();
    }

    public MediaPlayerService getMediaPlayerService() {
        return mMediaPlayerServiceConnector.getService();
    }
}
