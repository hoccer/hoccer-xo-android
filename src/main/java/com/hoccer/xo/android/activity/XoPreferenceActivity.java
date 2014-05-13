package com.hoccer.xo.android.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.view.AttachmentTransferControlView;
import com.hoccer.xo.release.R;

import net.hockeyapp.android.CrashManager;
import org.apache.log4j.Logger;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.sql.SQLException;

public class XoPreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(XoPreferenceActivity.class);
    private AttachmentTransferControlView mSpinner;
    private Handler mDialogDismisser;
    private Dialog mWaitingDialog;

    private MediaPlayerService mMediaPlayerService;
    private ServiceConnection mMediaPlayerServiceConnection;
    private Menu mMenu;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (XoConfiguration.DEVELOPMENT_MODE_ENABLED) {
            addPreferencesFromResource(R.xml.development_preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
        getListView().setBackgroundColor(Color.WHITE);

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindMediaPlayerService(intent);
        createMediaPlayerBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashesIfEnabled();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean result = super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.fragment_preferences, menu);
        mMenu = menu;
        updateActionBarIcons(menu);

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_media_player:
                openFullScreenPlayer();
                updateActionBarIcons(mMenu);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void checkForCrashesIfEnabled() {
        if (XoConfiguration.reportingEnable()) {
            CrashManager.register(this, XoConfiguration.HOCKEYAPP_ID);
        }
    }

    public void createDialog() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.waiting_dialog, null);
        mSpinner = (AttachmentTransferControlView) view.findViewById(R.id.content_progress);

        mWaitingDialog = new Dialog(this);
        mWaitingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mWaitingDialog.setContentView(view);
        mWaitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mWaitingDialog.setCanceledOnTouchOutside(false);
        if (!isFinishing()) {
            mWaitingDialog.show();
        }
        mWaitingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });

        Handler spinnerStarter = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mSpinner.prepareToUpload();
                mSpinner.spin();
            }
        };
        mDialogDismisser = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mWaitingDialog.dismiss();
                mSpinner.completeAndGone();
            }
        };
        spinnerStarter.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("preference_keysize")) {
            createDialog();
            regenerateKeys();
        }
    }

    private void regenerateKeys() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XoApplication.getXoClient().regenerateKeyPair();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    mDialogDismisser.sendEmptyMessage(0);
                }
            }
        });
        t.start();
    }

    @Override
    protected void onDestroy() {
        if (mSpinner != null) {
            mSpinner.completeAndGone();
        }
        super.onDestroy();

        unbindService(mMediaPlayerServiceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    private void openFullScreenPlayer(){
        Intent resultIntent = new Intent(this, FullscreenPlayerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
                .addParentStack(FullscreenPlayerActivity.class)
                .addNextIntent(resultIntent);

        stackBuilder.startActivities();
    }

    private void createMediaPlayerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MediaPlayerService.PLAYSTATE_CHANGED_ACTION)) {
                    updateActionBarIcons(mMenu);
                }
            }
        };
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAYSTATE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void updateActionBarIcons( Menu menu){
        if ( mMediaPlayerService != null && menu != null) {
            MenuItem mediaPlayerItem = menu.findItem(R.id.menu_media_player);

            if ( mMediaPlayerService.isStopped() || mMediaPlayerService.isPaused()) {
                mediaPlayerItem.setVisible(false);
            }else {
                mediaPlayerItem.setVisible(true);
            }
        }
    }

    private void bindMediaPlayerService(Intent intent) {

        mMediaPlayerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
                updateActionBarIcons( mMenu);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        bindService(intent, mMediaPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }
}
