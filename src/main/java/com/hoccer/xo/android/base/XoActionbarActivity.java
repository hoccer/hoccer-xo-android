package com.hoccer.xo.android.base;

import android.content.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.hoccer.xo.android.activity.FullscreenPlayerActivity;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.service.MediaPlayerServiceConnector;
import com.hoccer.xo.release.R;

/**
 * Activity keeps track of synchronizing the mediaplay icon
 * according to the mediaplayer state.
 * It can also be used to pause the current media.
 */
public abstract class XoActionbarActivity extends XoActivity {

    private Menu mMenu;
    private MediaPlayerServiceConnector mMediaPlayerServiceConnector = new MediaPlayerServiceConnector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaPlayerServiceConnector.connect(this,
                MediaPlayerService.PLAYSTATE_CHANGED_ACTION,
                new MediaPlayerServiceConnector.Listener() {
                    @Override
                    public void onConnected(MediaPlayerService service) {
                        updateActionBarIcons();
                    }
                    @Override
                    public void onDisconnected() {
                    }
                    @Override
                    public void onAction(String action, MediaPlayerService service) {
                        updateActionBarIcons();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayerServiceConnector.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        mMenu = menu;
        updateActionBarIcons();

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_media_player:
                openFullScreenPlayer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFullScreenPlayer(){
        Intent resultIntent = new Intent(this, FullscreenPlayerActivity.class);
        startActivity(resultIntent);
    }

    private void updateActionBarIcons() {
        if (mMediaPlayerServiceConnector.isConnected() && mMenu != null) {
            MenuItem mediaPlayerItem = mMenu.findItem(R.id.menu_media_player);

            MediaPlayerService service = mMediaPlayerServiceConnector.getService();
            if (service.isStopped() || service.isPaused()) {
                mediaPlayerItem.setVisible(false);
            } else {
                mediaPlayerItem.setVisible(true);
            }
        }
    }
}
