package com.hoccer.talk.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.hoccer.talk.android.activity.AboutActivity;
import com.hoccer.talk.android.activity.MessagingActivity;
import com.hoccer.talk.android.activity.PairingActivity;
import com.hoccer.talk.android.activity.PreferenceActivity;
import com.hoccer.talk.android.activity.ProfileActivity;
import com.hoccer.talk.android.adapter.ContactsAdapter;
import com.hoccer.talk.android.adapter.ConversationAdapter;
import com.hoccer.talk.android.adapter.RichContactsAdapter;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentRegistry;
import com.hoccer.talk.android.content.ContentSelection;
import com.hoccer.talk.android.database.AndroidTalkDatabase;
import com.hoccer.talk.android.dialog.ContactDeleteDialog;
import com.hoccer.talk.android.dialog.ContactDepairDialog;
import com.hoccer.talk.android.dialog.GroupInviteDialog;
import com.hoccer.talk.android.dialog.GroupKickDialog;
import com.hoccer.talk.android.dialog.GroupLeaveDialog;
import com.hoccer.talk.android.dialog.NameDialog;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.android.service.TalkClientService;
import com.hoccer.talk.client.HoccerTalkClient;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base class for activities working with the talk client
 *
 * This encapsulated commonalities:
 *  - Database access
 *  - Permanent connection to the client service
 *  - Methods for moving between activities
 *  - Methods for constructing view adapters
 */
public abstract class TalkActivity extends SherlockFragmentActivity
        implements ITalkActivity, ITalkClientServiceListener {

    public final static int REQUEST_SELECT_AVATAR = 23;
    public final static int REQUEST_SELECT_ATTACHMENT = 42;
    public final static int REQUEST_SCAN_BARCODE = IntentIntegrator.REQUEST_CODE; // XXX dirty

    public final static String DIALOG_NAME = "NameDialog";
    public final static String DIALOG_CONTACT_DELETE = "ContactDeleteDialog";
    public final static String DIALOG_CONTACT_DEPAIR = "ContactDepairDialog";
    public final static String DIALOG_GROUP_KICK = "GroupKickDialog";
    public final static String DIALOG_GROUP_INVITE = "GroupInviteDialog";

    protected Logger LOG = null;

    private ActionBar mActionBar;

    /** Executor for background tasks */
    ScheduledExecutorService mBackgroundExecutor;

    /** RPC interface to service (null when not connected) */
    ITalkClientService mService;

    /** Service connection object managing mService */
    ServiceConnection mServiceConnection;

    /** Timer for keepalive calls to the service */
    ScheduledFuture<?> mKeepAliveTimer;

    /** Talk client database */
    TalkClientDatabase mDatabase;

    /** List of all talk fragments */
    ArrayList<TalkFragment> mTalkFragments = new ArrayList<TalkFragment>();

    /** Client listeners */
    ArrayList<ITalkClientServiceListener> mListeners = new ArrayList<ITalkClientServiceListener>();

    /** Ongoing avatar selection */
    ContentSelection mAvatarSelection = null;

    /** Ongoing attachment selection */
    ContentSelection mAttachmentSelection = null;

    /** ZXing wrapper service */
    IntentIntegrator mBarcodeService = null;

    boolean mUpEnabled = false;

    public TalkActivity() {
        LOG = Logger.getLogger(getClass());
        mListeners.add(this);
    }

    protected abstract int getLayoutResource();
    protected abstract int getMenuResource();

    public ScheduledExecutorService getBackgroundExecutor() {
        return mBackgroundExecutor;
    }

    public ITalkClientService getService() {
        return mService;
    }

    public void registerTalkFragment(TalkFragment fragment) {
        mTalkFragments.add(fragment);
        mListeners.add(fragment);
    }

    public void unregisterTalkFragment(TalkFragment fragment) {
        mTalkFragments.remove(fragment);
        mListeners.remove(fragment);
    }

    public void registerListener(ITalkClientServiceListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(ITalkClientServiceListener listener) {
        mListeners.remove(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        // set up database connection
        mDatabase = new TalkClientDatabase(AndroidTalkDatabase.getInstance(this.getApplicationContext()));
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

        // launch a new background executor
        mBackgroundExecutor = TalkApplication.getExecutor();

        // start the backend service and bind to it
        Intent serviceIntent = new Intent(getApplicationContext(), TalkClientService.class);
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
                    getTalkClientService().createGroup();
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
                ContentObject co = ContentRegistry.get(this).createSelectedAvatar(mAvatarSelection, data);
                if(co != null) {
                    LOG.debug("selected avatar " + co.getContentUrl());
                    for(TalkFragment fragment: mTalkFragments) {
                        fragment.onAvatarSelected(co);
                    }
                }
            }
            return;
        }

        if(requestCode == REQUEST_SELECT_ATTACHMENT) {
            ContentObject co = ContentRegistry.get(this).createSelectedAttachment(mAttachmentSelection, data);
            if(co != null) {
                LOG.debug("selected attachment " + co.getContentUrl());
                for(TalkFragment fragment: mTalkFragments) {
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
        LOG.info("enableUpNavigation()");
        mUpEnabled = true;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void navigateUp() {
        LOG.info("navigateUp()");
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
                TalkConfiguration.SERVICE_KEEPALIVE_PING_DELAY,
                TalkConfiguration.SERVICE_KEEPALIVE_PING_INTERVAL,
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
     * Attach our listener to the client service
     */
    private void attachServiceListener() {
        if(mService != null) {
            try {
                mService.setListener(new MainServiceListener());
            } catch (RemoteException e) {
                LOG.error("remote exception", e);
            }
        }
    }

    /**
     * Connection to our backend service
     */
    public class MainServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOG.debug("onServiceConnected()");
            mService = (ITalkClientService)service;
            scheduleKeepAlive();
            attachServiceListener();
            try {
                mService.wake();
            } catch (RemoteException e) {
                LOG.error("remote error", e);
            }
            for(TalkFragment fragment: mTalkFragments) {
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
            for(TalkFragment fragment: mTalkFragments) {
                fragment.onServiceDisconnected();
            }
        }
    }

    /**
     * Listener for events from service
     *
     * This gets called when the network side of the client has changed
     * the database. Views should be updated according to what has changed.
     */
    public class MainServiceListener extends ITalkClientServiceListener.Stub implements ITalkClientServiceListener {
        @Override
        public void onClientStateChanged(int state) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onClientStateChanged(state);
            }
        }
        @Override
        public void onTokenPairingFailed(String token) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onTokenPairingFailed(token);
            }
        }
        @Override
        public void onTokenPairingSucceeded(String token) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onTokenPairingSucceeded(token);
            }
        }
        @Override
        public void onContactAdded(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onContactAdded(contactId);
            }
        }
        @Override
        public void onContactRemoved(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onContactRemoved(contactId);
            }
        }
        @Override
        public void onClientPresenceChanged(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onClientPresenceChanged(contactId);
            }
        }
        @Override
        public void onClientRelationshipChanged(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onClientRelationshipChanged(contactId);
            }
        }
        @Override
        public void onGroupCreationSucceeded(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onGroupCreationSucceeded(contactId);
            }
        }
        @Override
        public void onGroupCreationFailed() throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onGroupCreationFailed();
            }
        }
        @Override
        public void onGroupPresenceChanged(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onGroupPresenceChanged(contactId);
            }
        }
        @Override
        public void onGroupMembershipChanged(int contactId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onGroupMembershipChanged(contactId);
            }
        }
        @Override
        public void onMessageAdded(int contactId, int messageId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onMessageAdded(contactId, messageId);
            }
        }
        @Override
        public void onMessageRemoved(int contactId, int messageId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onMessageRemoved(contactId, messageId);
            }
        }
        @Override
        public void onMessageStateChanged(int contactId, int messageId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onMessageStateChanged(contactId, messageId);
            }
        }
        @Override
        public void onUploadAdded(int contactId, int downloadId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onUploadAdded(contactId, downloadId);
            }
        }
        @Override
        public void onUploadRemoved(int contactId, int downloadId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onUploadRemoved(contactId, downloadId);
            }
        }
        @Override
        public void onUploadProgress(int contactId, int downloadId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onUploadProgress(contactId, downloadId);
            }
        }
        @Override
        public void onUploadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onUploadStateChanged(contactId, downloadId, state);
            }
        }
        @Override
        public void onDownloadAdded(int contactId, int downloadId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onDownloadAdded(contactId, downloadId);
            }
        }
        @Override
        public void onDownloadRemoved(int contactId, int downloadId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onDownloadRemoved(contactId, downloadId);
            }
        }
        @Override
        public void onDownloadProgress(int contactId, int downloadId) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onDownloadProgress(contactId, downloadId);
            }
        }
        @Override
        public void onDownloadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
            for(ITalkClientServiceListener listener: mListeners) {
                listener.onDownloadStateChanged(contactId, downloadId, state);
            }
        }
    }

    @Override
    public int getClientState() {
        int state = HoccerTalkClient.STATE_INACTIVE;
        if(mService != null) {
            try {
                state = mService.getClientState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return state;
    }

    @Override
    public ITalkClientService getTalkClientService() {
        return mService;
    }

    @Override
    public TalkClientDatabase getTalkClientDatabase() {
        return mDatabase;
    }

    @Override
    public ConversationAdapter makeConversationAdapter() {
        ConversationAdapter adapter = new ConversationAdapter(this);
        adapter.register();
        adapter.reload();
        return adapter;
    }

    @Override
    public TalkAdapter makeContactListAdapter() {
        TalkAdapter adapter = new RichContactsAdapter(this);
        adapter.register();
        adapter.reload();
        return adapter;
    }

    @Override
    public void showContactProfile(TalkClientContact contact) {
        LOG.debug("showContactProfile(" + contact.getClientContactId() + ")");
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("clientContactId", contact.getClientContactId());
        startActivity(intent);
    }

    @Override
    public void showContactConversation(TalkClientContact contact) {
        LOG.debug("showContactConversation(" + contact.getClientContactId() + ")");
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra("clientContactId", contact.getClientContactId());
        startActivity(intent);
    }

    @Override
    public void showPairing() {
        LOG.debug("showPairing()");
        startActivity(new Intent(this, PairingActivity.class));
    }

    @Override
    public void showAbout() {
        LOG.debug("showAbout()");
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
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
        mBarcodeService.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    public void showBarcode() {
        LOG.debug("showBarcode()");
        TalkApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final String token;
                try {
                    token = getTalkClientService().generatePairingToken();
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

    public void hackReturnedFromDialog() {
        LOG.debug("hackReturnedFromDialog()");
    }

    public void changeName(TalkClientContact contact) {
        new NameDialog(this, contact)
                .show(getSupportFragmentManager(), DIALOG_NAME);
    }

    public void confirmDeleteContact(TalkClientContact contact) {
        new ContactDeleteDialog(this, contact)
                .show(getSupportFragmentManager(), DIALOG_CONTACT_DELETE);
    }

    public void confirmDepairContact(TalkClientContact contact) {
        new ContactDepairDialog(this, contact)
                .show(getSupportFragmentManager(), DIALOG_CONTACT_DEPAIR);
    }

    public void confirmGroupLeave(TalkClientContact group) {
        new GroupLeaveDialog(this, group)
                .show(getSupportFragmentManager(), DIALOG_GROUP_KICK);
    }

    public void selectGroupInvite(TalkClientContact group) {
        new GroupInviteDialog(this, group)
                .show(getSupportFragmentManager(), DIALOG_GROUP_INVITE);
    }

    public void selectGroupKick(TalkClientContact group) {
        new GroupKickDialog(this, group)
                .show(getSupportFragmentManager(), DIALOG_GROUP_KICK);
    }

    /** Just a dummy so we can implement the listener interface */
    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public void onClientStateChanged(int state) throws RemoteException {
    }

    @Override
    public void onTokenPairingFailed(String token) throws RemoteException {
    }

    @Override
    public void onTokenPairingSucceeded(String token) throws RemoteException {
    }

    @Override
    public void onContactAdded(int contactId) throws RemoteException {
    }

    @Override
    public void onContactRemoved(int contactId) throws RemoteException {
    }

    @Override
    public void onClientPresenceChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onClientRelationshipChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onGroupCreationSucceeded(int contactId) throws RemoteException {
    }

    @Override
    public void onGroupCreationFailed() throws RemoteException {
    }

    @Override
    public void onGroupPresenceChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onGroupMembershipChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onMessageAdded(int contactId, int messageId) throws RemoteException {
    }

    @Override
    public void onMessageRemoved(int contactId, int messageId) throws RemoteException {
    }

    @Override
    public void onMessageStateChanged(int contactId, int messageId) throws RemoteException {
    }

    @Override
    public void onUploadAdded(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onUploadRemoved(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onUploadProgress(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onUploadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
    }

    @Override
    public void onDownloadAdded(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onDownloadRemoved(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onDownloadProgress(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onDownloadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
    }
}
