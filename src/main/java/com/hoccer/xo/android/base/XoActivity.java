package com.hoccer.xo.android.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.activity.AboutActivity;
import com.hoccer.xo.android.activity.LicensesActivity;
import com.hoccer.xo.android.activity.MessagingActivity;
import com.hoccer.xo.android.activity.PairingActivity;
import com.hoccer.xo.android.activity.PreferenceActivity;
import com.hoccer.xo.android.activity.ProfileActivity;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.ConversationAdapter;
import com.hoccer.xo.android.adapter.RichContactsAdapter;
import com.hoccer.xo.android.content.ContentRegistry;
import com.hoccer.xo.android.content.ContentSelection;
import com.hoccer.xo.android.database.AndroidTalkDatabase;
import com.hoccer.xo.android.service.IXoClientService;
import com.hoccer.xo.android.service.XoClientService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base class for our activities
 *
 * All our activities inherit from SherlockFragmentActivity
 * to maintain a common look and feel in the whole application.
 *
 * These activites continually keep the background service which
 * we use for connection retention alive by calling it via RPC.
 *
 */
public abstract class XoActivity extends SherlockFragmentActivity {

    public final static int REQUEST_SELECT_AVATAR = 23;
    public final static int REQUEST_SELECT_ATTACHMENT = 42;
    public final static int REQUEST_SCAN_BARCODE = IntentIntegrator.REQUEST_CODE; // XXX dirty

    protected Logger LOG = null;

    private ActionBar mActionBar;

    /** Executor for background tasks */
    ScheduledExecutorService mBackgroundExecutor;

    /** RPC interface to service (null when not connected) */
    IXoClientService mService;

    /** Service connection object managing mService */
    ServiceConnection mServiceConnection;

    /** Timer for keepalive calls to the service */
    ScheduledFuture<?> mKeepAliveTimer;

    /** Talk client database */
    XoClientDatabase mDatabase;

    /** List of all talk fragments */
    ArrayList<IXoFragment> mTalkFragments = new ArrayList<IXoFragment>();

    /** Ongoing avatar selection */
    ContentSelection mAvatarSelection = null;

    /** Ongoing attachment selection */
    ContentSelection mAttachmentSelection = null;

    /** ZXing wrapper service */
    IntentIntegrator mBarcodeService = null;

    boolean mUpEnabled = false;

    public XoActivity() {
        LOG = Logger.getLogger(getClass());
    }

    protected abstract int getLayoutResource();
    protected abstract int getMenuResource();

    public XoClient getXoClient() {
        return XoApplication.getXoClient();
    }

    public ScheduledExecutorService getBackgroundExecutor() {
        return mBackgroundExecutor;
    }

    public IXoClientService getService() {
        return mService;
    }

    public void registerXoFragment(IXoFragment fragment) {
        mTalkFragments.add(fragment);
    }

    public void unregisterXoFragment(IXoFragment fragment) {
        mTalkFragments.remove(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        // set up database connection
        mDatabase = new XoClientDatabase(AndroidTalkDatabase.getInstance(this.getApplicationContext()));
        try {
            mDatabase.initialize();
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }

        // set layout
        setContentView(getLayoutResource());

        // get and configure the action bar
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // get the barcode scanning service
        mBarcodeService = new IntentIntegrator(this);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        // get the background executor
        mBackgroundExecutor = XoApplication.getExecutor();

        // start the backend service and bind to it
        Intent serviceIntent = new Intent(getApplicationContext(), XoClientService.class);
        startService(serviceIntent);
        mServiceConnection = new MainServiceConnection();
        bindService(serviceIntent, mServiceConnection, BIND_IMPORTANT);
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();

        // stop keeping the service alive
        shutdownKeepAlive();

        // drop reference to service binder
        if(mService != null) {
            mService = null;
        }
        // unbind service connection
        if(mServiceConnection != null) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }

    @Override
    protected void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        getSupportMenuInflater().inflate(R.menu.common, menu);
        getSupportMenuInflater().inflate(getMenuResource(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;
            case R.id.menu_my_profile:
                try {
                    showContactProfile(mDatabase.findSelfContact(false));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menu_pair:
                showPairing();
                break;
            case R.id.menu_new_group:
                try {
                    getXoService().createGroup();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menu_scan_code:
                scanBarcode();
                break;
            case R.id.menu_show_code:
                showBarcode();
                break;
            case R.id.menu_settings:
                showPreferences();
                break;
            case R.id.menu_about:
                showAbout();
                break;
            case R.id.menu_licenses:
                showLicenses();
                break;
            case R.id.menu_reconnect:
                try {
                    mService.reconnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private String mBarcodeToken = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOG.debug("onActivityResult(" + requestCode + "," + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null) {
            return;
        }

        if(requestCode == REQUEST_SELECT_AVATAR) {
            if(mAvatarSelection != null) {
                IContentObject co = ContentRegistry.get(this).createSelectedAvatar(mAvatarSelection, data);
                if(co != null) {
                    LOG.debug("selected avatar " + co.getContentUrl());
                    for(IXoFragment fragment: mTalkFragments) {
                        fragment.onAvatarSelected(co);
                    }
                }
            }
            return;
        }

        if(requestCode == REQUEST_SELECT_ATTACHMENT) {
            IContentObject co = ContentRegistry.get(this).createSelectedAttachment(mAttachmentSelection, data);
            if(co != null) {
                LOG.debug("selected attachment " + co.getContentUrl());
                for(IXoFragment fragment: mTalkFragments) {
                    fragment.onAttachmentSelected(co);
                }
            }
            return;
        }

        if(requestCode == REQUEST_SCAN_BARCODE) {
            IntentResult barcode = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(barcode != null) {
                LOG.debug("scanned barcode: " + barcode.getContents());
                String code = barcode.getContents();
                if(code.startsWith("hxo://")) {
                    mBarcodeToken = code.replace("hxo://", "");
                }
            }
            return;
        }

    }

    protected void enableUpNavigation() {
        LOG.debug("enableUpNavigation()");
        mUpEnabled = true;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void navigateUp() {
        LOG.debug("navigateUp()");
        if(mUpEnabled) {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // we are not on our own task stack, so create one
                TaskStackBuilder.create(this)
                        // add parents to back stack
                        .addNextIntentWithParentStack(upIntent)
                                // navigate up to next parent
                        .startActivities();
            } else {
                // we are on our own task stack, so navigate upwards
                NavUtils.navigateUpTo(this, upIntent);
            }
        }
    }

    /**
     * Schedule regular keep-alive calls to the service
     */
    private void scheduleKeepAlive() {
        shutdownKeepAlive();
        mKeepAliveTimer = mBackgroundExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(mService != null) {
                    try {
                        mService.keepAlive();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        },
                XoConfiguration.SERVICE_KEEPALIVE_PING_DELAY,
                XoConfiguration.SERVICE_KEEPALIVE_PING_INTERVAL,
                TimeUnit.SECONDS);
    }

    /**
     * Stop sending keep-alive calls to the service
     */
    private void shutdownKeepAlive() {
        if(mKeepAliveTimer != null) {
            mKeepAliveTimer.cancel(false);
            mKeepAliveTimer = null;
        }
    }

    /**
     * Connection to our backend service
     */
    public class MainServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOG.debug("onServiceConnected()");
            mService = (IXoClientService)service;
            scheduleKeepAlive();
            try {
                mService.wake();
            } catch (RemoteException e) {
                LOG.error("remote error", e);
            }
            for(IXoFragment fragment: mTalkFragments) {
                fragment.onServiceConnected();
            }
            if(mBarcodeToken != null) {
                try {
                    mService.pairUsingToken(mBarcodeToken);
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
                mBarcodeToken = null;
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOG.debug("onServiceDisconnected()");
            shutdownKeepAlive();
            mService = null;
            for(IXoFragment fragment: mTalkFragments) {
                fragment.onServiceDisconnected();
            }
        }
    }

    public IXoClientService getXoService() {
        return mService;
    }

    public XoClientDatabase getXoDatabase() {
        return mDatabase;
    }

    public ConversationAdapter makeConversationAdapter() {
        ConversationAdapter adapter = new ConversationAdapter(this);
        adapter.register();
        return adapter;
    }

    public ContactsAdapter makeContactListAdapter() {
        ContactsAdapter adapter = new RichContactsAdapter(this);
        adapter.register();
        return adapter;
    }

    public void wakeClient() {
        if(mService != null) {
            try {
                mService.wake();
            } catch (RemoteException e) {
                LOG.error("remote error", e);
            }
        }
    }

    public void showContactProfile(TalkClientContact contact) {
        LOG.debug("showContactProfile(" + contact.getClientContactId() + ")");
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_CLIENT_CONTACT_ID,
                        contact.getClientContactId());
        startActivity(intent);
    }

    public void showContactConversation(TalkClientContact contact) {
        LOG.debug("showContactConversation(" + contact.getClientContactId() + ")");
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(MessagingActivity.EXTRA_CLIENT_CONTACT_ID,
                        contact.getClientContactId());
        startActivity(intent);
    }

    public void showPairing() {
        LOG.debug("showPairing()");
        startActivity(new Intent(this, PairingActivity.class));
    }

    public void showAbout() {
        LOG.debug("showAbout()");
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void showLicenses() {
        LOG.debug("showLicenses()");
        startActivity(new Intent(this, LicensesActivity.class));
    }

    public void showPreferences() {
        LOG.debug("showPreferences()");
        startActivity(new Intent(this, PreferenceActivity.class));
    }

    public void selectAvatar() {
        LOG.debug("selectAvatar()");
        mAvatarSelection = ContentRegistry.get(this).selectAvatar(this, REQUEST_SELECT_AVATAR);
    }

    public void selectAttachment() {
        LOG.debug("selectAttachment()");
        mAttachmentSelection = ContentRegistry.get(this).selectAttachment(this, REQUEST_SELECT_ATTACHMENT);
    }

    public void scanBarcode() {
        LOG.debug("scanBarcode()");
        wakeClient();
        mBarcodeService.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    public void showBarcode() {
        LOG.debug("showBarcode()");
        XoApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final String token;
                try {
                    token = getXoService().generatePairingToken();
                } catch (RemoteException e) {
                    LOG.error("could not generate token", e);
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBarcodeService.shareText("hxo://" + token);
                    }
                });
            }
        });
    }

    public void composeInviteSms(String token) {
        LOG.debug("composeInviteSms(" + token + ")");

        try {
            TalkClientContact self = mDatabase.findSelfContact(false);

            String message =
                    "Hey! I'm now using the free app Hoccer XO for secure chatting. " +
                    "Download it now: http://hoccer.com/ Then add me as a contact: " +
                    "hxo://" + token + "\nxo " + self.getName();

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:"));
            intent.putExtra("sms_body", message);

            startActivity(intent);
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
    }

    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
    }

}
