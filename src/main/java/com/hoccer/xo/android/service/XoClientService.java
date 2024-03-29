package com.hoccer.xo.android.service;

import android.app.*;
import com.google.android.gcm.GCMRegistrar;

import com.hoccer.talk.android.push.TalkPushService;
import com.hoccer.talk.client.IXoStateListener;
import com.hoccer.talk.client.IXoTokenListener;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.IXoUnseenListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientConfiguration;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.activity.ContactsActivity;
import com.hoccer.xo.android.activity.MessagingActivity;
import com.hoccer.xo.android.sms.SmsReceiver;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Android service for Hoccer Talk
 *
 * This service wraps a Talk client instance for use by Android applications.
 *
 * It should be started with startService() and kept alive using keepAlive() RPC calls
 * for as long as it is needed. If not called regularly the service will stop itself.
 */


public class XoClientService extends Service {

    private static final Logger LOG = Logger.getLogger(XoClientService.class);

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private static final int NOTIFICATION_UNSEEN_MESSAGES = 0;

    private static final int NOTIFICATION_UNCONFIRMED_INVITATIONS = 1;

    /** Executor for ourselves and the client */
    ScheduledExecutorService mExecutor;

    /** Hoccer client that we serve */
    XoClient mClient;

    /** Reference to latest auto-shutdown future */
    ScheduledFuture<?> mShutdownFuture;

    /** All service connections */
    ArrayList<Connection> mConnections;

    /** Preferences containing service configuration */
    SharedPreferences mPreferences;

    /** Listener for configuration changes */
    SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener;

    /** Connectivity manager for monitoring */
    ConnectivityManager mConnectivityManager;

    /** Our connectivity change broadcast receiver */
    ConnectivityReceiver mConnectivityReceiver;

    /** Previous state of connectivity */
    boolean mPreviousConnectionState = false;

    /** Type of previous connection */
    int mPreviousConnectionType = -1;

    /** Notification manager */
    NotificationManager mNotificationManager;

    /** Time of last notification (for cancellation backoff) */
    long mNotificationTimestamp;

    boolean mAutoDownloadMobile = false;

    boolean mAutoDownloadWifi = true;

    ClientListener mClientListener;

    boolean mGcmSupported;

    private ClientIdReceiver m_clientIdReceiver;

    @Override
    public void onCreate() {
        LOG.debug("onCreate()");
        super.onCreate();

        mExecutor = XoApplication.getExecutor();

        mConnections = new ArrayList<Connection>();

        mClient = XoApplication.getXoClient();

        if (mClientListener == null) {
            mClientListener = new ClientListener();
            mClient.registerTokenListener(mClientListener);
            mClient.registerStateListener(mClientListener);
            mClient.registerUnseenListener(mClientListener);
            mClient.registerTransferListener(mClientListener);
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("preference_service_uri")) {
                    configureServiceUri();
                }
                if (key.equals("preference_download_auto_mobile")
                        || key.equals("preference_download_auto_wifi")) {
                    configureAutoDownloads();
                }
            }
        };
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferencesListener);

        configureAutoDownloads();

        doVerifyGcm();

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        registerConnectivityReceiver();
        handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter("com.hoccer.xo.android.service.XoClientService$ClientIdReceiver");
        filter.addAction("CONTACT_ID_IN_CONVERSATION");
        m_clientIdReceiver = new ClientIdReceiver();
        registerReceiver(m_clientIdReceiver, filter);
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
        unregisterConnectivityReceiver();
        if (mClientListener != null) {
            mClient.unregisterTokenListener(mClientListener);
            mClient.unregisterStateListener(mClientListener);
            mClient.unregisterUnseenListener(mClientListener);
            mClient.unregisterTransferListener(mClientListener);
            mClientListener = null;
        }
        // XXX unregister client listeners
        if (mPreferencesListener != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
            mPreferencesListener = null;
        }
        unregisterReceiver(m_clientIdReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.debug("onStartCommand(" + ((intent == null) ? "null" : intent.toString()) + ")");
        if (intent != null) {
            if (intent.hasExtra(TalkPushService.EXTRA_WAKE_CLIENT)) {
                wakeClient();
            }
            if (intent.hasExtra(TalkPushService.EXTRA_GCM_REGISTERED)) {
                doUpdateGcm(true);
            }
            if (intent.hasExtra(TalkPushService.EXTRA_GCM_UNREGISTERED)) {
                doUpdateGcm(true);
            }
            if (intent.hasExtra(SmsReceiver.EXTRA_SMS_URL_RECEIVED)) {
                String sender = intent.getStringExtra(SmsReceiver.EXTRA_SMS_SENDER);
                String body = intent.getStringExtra(SmsReceiver.EXTRA_SMS_BODY);
                String url = intent.getStringExtra(SmsReceiver.EXTRA_SMS_URL_RECEIVED);
                mClient.handleSmsUrl(sender, body, url);
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LOG.debug("onBind(" + intent.toString() + ")");

        if (!mClient.isActivated()) {
            mClient.activate();
        }

        Connection newConnection = new Connection(intent);

        mConnections.add(newConnection);

        return newConnection;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.debug("onUnbind(" + intent.toString() + ")");
        return super.onUnbind(intent);
    }

    private void configureServiceUri() {
        String uriString = mPreferences.getString("preference_service_uri", "");
        if (uriString.isEmpty()) {
            uriString = XoClientConfiguration.SERVER_URI;
        }
        URI uri = URI.create(uriString);
        mClient.setServiceUri(uri);
    }

    private void configureAutoDownloads() {
        mAutoDownloadMobile = mPreferences.getBoolean("preference_download_auto_mobile", false);
        mAutoDownloadWifi = mPreferences.getBoolean("preference_download_auto_wifi", true);
    }

    private void wakeClient() {
        if (mPreviousConnectionState) {
            mClient.wake();
        }
    }

    private void doVerifyGcm() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doVerifyGcm()");
        }

        // check our manifest for GCM compatibility
        boolean manifestAllowsGcm = false;
        try {
            GCMRegistrar.checkManifest(this);
            manifestAllowsGcm = true;
        } catch (IllegalStateException ex) {
            LOG.warn("GCM unavailable due to manifest problems", ex);
        }

        // check GCM device support
        boolean deviceSupportsGcm = false;
        if (manifestAllowsGcm) {
            try {
                GCMRegistrar.checkDevice(this);
                deviceSupportsGcm = true;
            } catch (UnsupportedOperationException ex) {
                LOG.warn("GCM not supported by device", ex);
            }
        }

        // make the final decision
        mGcmSupported = deviceSupportsGcm && manifestAllowsGcm;
        if (mGcmSupported) {
            LOG.info("GCM is supported");
        } else {
            LOG.warn("GCM not supported");
        }
    }

    private void doRegisterGcm(boolean forced) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doRegisterGcm(" + (forced ? "forced" : "") + ")");
        }
        if (mGcmSupported) {
            if (forced || !GCMRegistrar.isRegistered(this)) {
                LOG.debug("requesting GCM registration");
                GCMRegistrar.register(this, XoConfiguration.GCM_SENDER_ID);
            } else {
                LOG.debug("no need to request GCM registration");
            }
        }
    }

    private void doUpdateGcm(boolean forced) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doUpdateGcm(" + (forced ? "forced" : "") + ")");
        }
        if (mGcmSupported && GCMRegistrar.isRegistered(this)) {
            // check if we got here already
            if (forced || !GCMRegistrar.isRegisteredOnServer(this)) {
                LOG.debug("updating GCM registration");
                // perform the registration call
                mClient.registerGcm(this.getPackageName(), GCMRegistrar.getRegistrationId(this));
                // set the registration timeout (XXX move elsewhere)
                GCMRegistrar.setRegisterOnServerLifespan(
                        this, XoConfiguration.GCM_REGISTRATION_EXPIRATION * 1000);
                // tell the registrar that we did this successfully
                GCMRegistrar.setRegisteredOnServer(this, true);
            } else {
                LOG.debug("no need to update GCM registration");
            }
        } else {
            if (forced || GCMRegistrar.isRegisteredOnServer(this)) {
                LOG.debug("retracting GCM registration");
                mClient.unregisterGcm();
                GCMRegistrar.setRegisteredOnServer(this, false);
            }
        }
    }

    private void doShutdown() {
        LOG.info("shutting down");
        // command the client to deactivate
        if (mClient.isActivated()) {
            mClient.deactivateNow();
        }
        // stop ourselves
        stopSelf();
    }

    private void scheduleShutdown() {
        shutdownShutdown();
        mShutdownFuture = mExecutor.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.debug("keep-alive timeout");
                        doShutdown();
                    }
                },
                XoConfiguration.SERVICE_KEEPALIVE_TIMEOUT, TimeUnit.SECONDS
        );
    }

    private void shutdownShutdown() {
        if (mShutdownFuture != null) {
            mShutdownFuture.cancel(false);
            mShutdownFuture = null;
        }
    }

    private void registerConnectivityReceiver() {
        LOG.debug("registerConnectivityReceiver()");
        if (mConnectivityReceiver == null) {
            mConnectivityReceiver = new ConnectivityReceiver();
            registerReceiver(mConnectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void unregisterConnectivityReceiver() {
        LOG.debug("unregisterConnectivityReceiver()");
        if (mConnectivityReceiver != null) {
            unregisterReceiver(mConnectivityReceiver);
            mConnectivityReceiver = null;
        }
    }

    private void handleConnectivityChange(NetworkInfo activeNetwork) {
        if (activeNetwork == null) {
            LOG.debug("connectivity change: no connectivity");
            mClient.deactivate();
            mPreviousConnectionState = false;
            mPreviousConnectionType = -1;
        } else {
            LOG.debug("connectivity change:"
                    + " type " + activeNetwork.getTypeName()
                    + " state " + activeNetwork.getState().name());

            int previousState = mClient.getState();
            if (activeNetwork.isConnected()) {
                if (previousState <= XoClient.STATE_INACTIVE) {
                    mClient.activate();
                }
            } else if (activeNetwork.isConnectedOrConnecting()) {
                if (previousState <= XoClient.STATE_INACTIVE) {
                    mClient.activate();
                }
            } else {
                if (previousState > XoClient.STATE_INACTIVE) {
                    mClient.deactivate();
                }
            }

            boolean netState = activeNetwork.isConnected();
            int netType = activeNetwork.getType();

            if (XoConfiguration.CONNECTIVITY_RECONNECT_ON_CHANGE) {
                if (netState && !mClient.isIdle()) {
                    if (!mPreviousConnectionState
                            || mPreviousConnectionType == -1
                            || mPreviousConnectionType != netType) {
                        if (mClient.getState() < XoClient.STATE_CONNECTING) {
                            mClient.reconnect("connection change");
                        }
                    }
                }
            }

            mPreviousConnectionState = netState;
            mPreviousConnectionType = netType;
        }
    }

    private void updateInvitateNotification(List<TalkClientSmsToken> unconfirmedTokens,
            boolean notify) {
        LOG.debug("updateInvitateNotification()");
        XoClientDatabase db = mClient.getDatabase();

        // cancel present notification if everything has been seen
        // we back off here to prevent interruption of any in-progress alarms
        if (unconfirmedTokens == null || unconfirmedTokens.isEmpty()) {
            LOG.debug("no unconfirmed tokens");
            mNotificationManager.cancel(NOTIFICATION_UNCONFIRMED_INVITATIONS);
            return;
        }

        int numUnconfirmed = unconfirmedTokens.size();

        // log about what we got
        LOG.debug("notifying " + numUnconfirmed + " invitations ");

        // build the notification
        Notification.Builder builder = new Notification.Builder(this);
        // always set the small icon (should be different depending on if we have a large one)
        builder.setSmallIcon(R.drawable.ic_notification);
        // large icon XXX
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        builder.setLargeIcon(largeIcon);
        // determine if alarms should be sounded
        if (notify) {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        // set total number of messages of more than one
        if (numUnconfirmed > 1) {
            builder.setNumber(numUnconfirmed);
        }
        // create pending intent
        Intent contactsIntent = new Intent(this, ContactsActivity.class);
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            pendingIntent = TaskStackBuilder.create(this).addNextIntent(contactsIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent
                    .getActivity(this, 0, contactsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        builder.setContentIntent(pendingIntent);
        // set fields
        if (numUnconfirmed > 1) {
            builder.setContentTitle(numUnconfirmed + " unconfirmed invitations");
        } else {
            builder.setContentTitle(numUnconfirmed + " unconfirmed invitation");
        }

        // finish up
        Notification notification = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        // log about it
        LOG.debug("invite notification " + notification.toString());
        // update the notification
        mNotificationManager.notify(NOTIFICATION_UNCONFIRMED_INVITATIONS, notification);
    }

    private void updateMessageNotification(List<TalkClientMessage> allUnseenMessages,
            boolean notify) {
        LOG.debug("updateMessageNotification()");
        XoClientDatabase db = mClient.getDatabase();

        // we re-collect messages to this to eliminate
        // messages from deleted contacts that are still in the db (XXX)
        List<TalkClientMessage> unseenMessages = new ArrayList<TalkClientMessage>();

        // determine where we are in time
        long now = System.currentTimeMillis();
        long passed = Math.max(0, now - mNotificationTimestamp);


        // do not sound alarms overly often (sound, vibrate)
        if (passed < XoConfiguration.NOTIFICATION_ALARM_BACKOFF) {
            notify = false;
        }

        // we are commited to notifying, update timestamp
        mNotificationTimestamp = now;

        // collect conversation contacts and sort messages accordingly
        // also removes messages from deleted clients
        List<TalkClientContact> contacts = new ArrayList<TalkClientContact>();
        Map<Integer, TalkClientContact> contactsById = new HashMap<Integer, TalkClientContact>();

        for (TalkClientMessage message : allUnseenMessages) {
            TalkClientContact contact = message.getConversationContact();
            if (contact != null) {
                int contactId = contact.getClientContactId();
                if (!contactsById.containsKey(contactId)) {
                    try {
                        db.refreshClientContact(contact);
                    } catch (SQLException e) {
                        LOG.error("sql error", e);
                    }
                    if (!contact.isDeleted()) {
                        contactsById.put(contactId, contact);
                        contacts.add(contact);
                        unseenMessages.add(message);
                    }
                }
            }  else {
                LOG.error("message without contact in unseen messages");
            }
        }

        // if we have no messages after culling then cancel notification
        if (unseenMessages.isEmpty()) {
            LOG.debug("no unseen messages");
            cancelMessageNotification();
            return;
        }

        // for easy reference
        int numUnseen = unseenMessages.size();
        int numContacts = contacts.size();

        // log about what we got
        LOG.debug("notifying " + numUnseen + " messages from " + numContacts + " contacts");

        // build the notification
        Notification.Builder builder = new Notification.Builder(this);
        // always set the small icon (should be different depending on if we have a large one)
        builder.setSmallIcon(R.drawable.ic_notification);
        // large icon XXX
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        builder.setLargeIcon(largeIcon);
        // determine if alarms should be sounded
        if (notify) {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        // set total number of messages of more than one
        if (numUnseen > 1) {
            builder.setNumber(numUnseen);
        }
        // fill in content
        if (contacts.size() == 1) {
            TalkClientContact singleContact = contacts.get(0);
            // create intent to start the messaging activity for the right contact
            Intent messagingIntent = new Intent(this, MessagingActivity.class);
            messagingIntent.putExtra("clientContactId", singleContact.getClientContactId());
            // make a pending intent with correct back-stack
            PendingIntent pendingIntent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                pendingIntent =
                        TaskStackBuilder.create(this)
                                .addParentStack(ContactsActivity.class)
                                .addNextIntentWithParentStack(messagingIntent)
                                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent
                        .getActivity(this, 0, messagingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            // add the intent to the notification
            builder.setContentIntent(pendingIntent);
            // title is always the contact name
            builder.setContentTitle(singleContact.getName());
            // text depends on number of messages
            if (unseenMessages.size() == 1) {
                TalkClientMessage singleMessage = unseenMessages.get(0);
                builder.setContentText(singleMessage.getText());
            } else {
                builder.setContentText(numUnseen + " new messages");
            }
        } else {
            // create pending intent
            Intent contactsIntent = new Intent(this, ContactsActivity.class);
            PendingIntent pendingIntent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                pendingIntent =
                        TaskStackBuilder.create(this)
                                .addNextIntent(contactsIntent)
                                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent
                        .getActivity(this, 0, contactsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            builder.setContentIntent(pendingIntent);
            // concatenate contact names
            StringBuilder sb = new StringBuilder();
            int last = contacts.size() - 1;
            for (int i = 0; i < contacts.size(); i++) {
                TalkClientContact contact = contacts.get(i);
                sb.append(contact.getName());
                if (i < last) {
                    sb.append(", ");
                }
            }
            // set fields
            builder.setContentTitle(sb.toString());
            builder.setContentText(numUnseen + " new messages");
        }

        // finish up
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        // log about it
        LOG.debug("message notification " + notification.toString());
        // update the notification
        mNotificationManager.notify(NOTIFICATION_UNSEEN_MESSAGES, notification);
    }

    private void cancelMessageNotification() {
        long now = System.currentTimeMillis();
        long cancelTime = mNotificationTimestamp + XoConfiguration.NOTIFICATION_CANCEL_BACKOFF;
        long delay = Math.max(0, cancelTime - now);
        mExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                mNotificationManager.cancel(NOTIFICATION_UNSEEN_MESSAGES);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.debug("onConnectivityChange()");
            handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());
        }
    }

    private class ClientListener implements
            IXoStateListener,
            IXoUnseenListener,
            IXoTokenListener,
            IXoTransferListener,
            MediaScannerConnection.OnScanCompletedListener {

        Hashtable<String, TalkClientDownload> mScanningDownloads
                = new Hashtable<String, TalkClientDownload>();

        @Override
        public void onClientStateChange(XoClient client, int state) {
            LOG.debug("onClientStateChange(" + XoClient.stateToString(state) + ")");
            if (state == XoClient.STATE_ACTIVE) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        doRegisterGcm(XoConfiguration.GCM_ALWAYS_REGISTER);
                        doUpdateGcm(XoConfiguration.GCM_ALWAYS_UPDATE);
                    }
                });
            }
        }

        // XXX
        //@Override
        //public void onPushRegistrationRequested() {
        //    LOG.info("onPushRegistrationRequested()");
        //    mExecutor.execute(new Runnable() {
        //        @Override
        //        public void run() {
        //            doRegisterGcm(false);
        //            doUpdateGcm(true);
        //        }
        //    });
        //}

        @Override
        public void onUnseenMessages(List<TalkClientMessage> unseenMessages, boolean notify) {
            LOG.debug("onUnseenMessages(" + unseenMessages.size() + "," + notify + ")");
            ActivityManager activityManager = (ActivityManager) getApplicationContext().
                    getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
            if (unseenMessages == null || unseenMessages.isEmpty()) {
                LOG.debug("no unseen messages");
                cancelMessageNotification();
                return;
            }
            if (services.get(0).topActivity.getShortClassName().toString()
                .equalsIgnoreCase(MessagingActivity.class.getName().toString())) {
                    m_clientIdReceiver.setContactId(unseenMessages.get(0).getConversationContact().getClientContactId());
                    m_clientIdReceiver.setNotificationData(unseenMessages, notify);
                    Intent intent = new Intent();
                    intent.setAction("CHECK_ID_IN_CONVERSATION");
                    sendBroadcast(intent);
            } else {
                updateMessageNotification(unseenMessages, notify);
            }
        }

        @Override
        public void onTokensChanged(List<TalkClientSmsToken> tokens, boolean newTokens) {
            LOG.debug("onTokensChanged(" + tokens.size() + "," + newTokens + ")");
            try {
                updateInvitateNotification(tokens, newTokens);
            } catch (Throwable t) {
                LOG.error("exception updating invite notification", t);
            }
        }

        @Override
        public void onDownloadRegistered(TalkClientDownload download) {
            LOG.debug("onDownloadRegistered(" + download.getClientDownloadId() + ")");
            if (download.isAttachment()) {
                boolean auto = false;
                switch (mPreviousConnectionType) {
                    case ConnectivityManager.TYPE_MOBILE:
                    case ConnectivityManager.TYPE_BLUETOOTH:
                    case ConnectivityManager.TYPE_WIMAX:
                        if (mAutoDownloadMobile) {
                            auto = true;
                        }
                        break;
                    case ConnectivityManager.TYPE_ETHERNET:
                    case ConnectivityManager.TYPE_WIFI:
                        if (mAutoDownloadWifi) {
                            auto = true;
                        }
                        break;
                }
                if (auto) {
                    mClient.requestDownload(download);
                }
            }
        }

        @Override
        public void onDownloadStateChanged(TalkClientDownload download) {
            if (download.isAttachment() && download.isContentAvailable()
                    && download.getContentUrl() == null) {
                String[] path = new String[]{download.getDataFile()};
                String[] ctype = new String[]{download.getContentType()};
                LOG.debug("requesting media scan of " + ctype[0] + " at " + path[0]);
                mScanningDownloads.put(path[0], download);
                MediaScannerConnection.scanFile(
                        XoClientService.this,
                        path, ctype, this
                );
            }
        }

        @Override
        public void onDownloadStarted(TalkClientDownload download) {
        }

        @Override
        public void onDownloadProgress(TalkClientDownload download) {
        }

        @Override
        public void onDownloadFinished(TalkClientDownload download) {
        }

        @Override
        public void onUploadStarted(TalkClientUpload upload) {
        }

        @Override
        public void onUploadProgress(TalkClientUpload upload) {
        }

        @Override
        public void onUploadFinished(TalkClientUpload upload) {
        }

        @Override
        public void onUploadStateChanged(TalkClientUpload upload) {
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            LOG.debug("media scan of " + path + " completed - uri " + uri.toString());
            TalkClientDownload download = mScanningDownloads.get(path);
            if (download != null) {
                download.provideContentUrl(mClient.getTransferAgent(), uri.toString());
            }
            mScanningDownloads.remove(path);
        }
    }

    public class Connection extends IXoClientService.Stub {

        int mId;

        Intent mBindIntent;

        Connection(Intent bindIntent) {
            mId = ID_COUNTER.incrementAndGet();
            mBindIntent = bindIntent;
            LOG.debug("[" + mId + "] connected");
        }

        @Override
        public void keepAlive() throws RemoteException {
            LOG.debug("[" + mId + "] keepAlive()");
            scheduleShutdown();
        }

        @Override
        public void wake() throws RemoteException {
            LOG.debug("[" + mId + "] wake()");
            wakeClient();
        }

        @Override
        public void reconnect() throws RemoteException {
            LOG.debug("[" + mId + "] reconnect()");
            mClient.reconnect("client request");
        }
    }

    private class ClientIdReceiver extends BroadcastReceiver {
        private int m_id;
        private List<TalkClientMessage> m_unseenMessages = new ArrayList<TalkClientMessage>();
        private boolean m_notify;

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (m_id != intent.getIntExtra("id", -1)) {
                updateMessageNotification(m_unseenMessages, m_notify);
            }
        }

        public void setContactId(int clientContactId) {
            m_id = clientContactId;
        }

        public void setNotificationData(List<TalkClientMessage> unseenMessages, boolean notify) {
            m_unseenMessages = unseenMessages;
            m_notify = notify;
        }
    }
}
