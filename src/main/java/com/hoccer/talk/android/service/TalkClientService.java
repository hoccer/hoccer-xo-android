package com.hoccer.talk.android.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.google.android.gcm.GCMRegistrar;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.android.activity.ContactsActivity;
import com.hoccer.talk.android.activity.MessagingActivity;
import com.hoccer.talk.android.database.AndroidTalkDatabase;
import com.hoccer.talk.android.push.TalkPushService;
import com.hoccer.talk.client.*;

import android.os.IBinder;
import android.os.RemoteException;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import org.apache.log4j.Logger;

/**
 * Android service for Hoccer Talk
 *
 * This service wraps a Talk client instance for use by Android applications.
 *
 * It should be started with startService() and kept alive using keepAlive() RPC calls
 * for as long as it is needed. If not called regularly the service will stop itself.
 *
 *
 */
public class TalkClientService extends Service {

	private static final Logger LOG = Logger.getLogger(TalkClientService.class);

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private static final int NOTIFICATION_UNSEEN_MESSAGES = 0;

    /** Executor for ourselves and the client */
    ScheduledExecutorService mExecutor;

    /** Hoccer client that we serve */
    HoccerTalkClient mClient;

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

    boolean mGcmSupported;

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mExecutor = TalkApplication.getExecutor();

        mConnections = new ArrayList<Connection>();

        mClient = new HoccerTalkClient(mExecutor, AndroidTalkDatabase.getInstance(this.getApplicationContext()));
        mClient.setAvatarDirectory(TalkApplication.getAvatarDirectory().toString());
        mClient.setAttachmentDirectory(TalkApplication.getAttachmentDirectory().toString());
        mClient.setEncryptedUploadDirectory(TalkApplication.getEncryptedUploadDirectory().toString());
        mClient.setEncryptedDownloadDirectory(TalkApplication.getEncryptedDownloadDirectory().toString());

        ClientListener clientListener = new ClientListener();
        mClient.registerListener(clientListener);
        mClient.registerUnseenListener(clientListener);
        mClient.getTransferAgent().registerListener(clientListener);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("preference_service_uri")) {
                    configureServiceUri();
                }
            }
        };
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferencesListener);

        doVerifyGcm();

        registerConnectivityReceiver();
        handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

    @Override
    public void onDestroy() {
        LOG.info("onDestroy()");
        super.onDestroy();
        mExecutor.shutdownNow();
        unregisterConnectivityReceiver();
        if(mPreferencesListener != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
            mPreferencesListener = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.info("onStartCommand(" + ((intent == null) ? "null" : intent.toString()) + ")");
        if(intent != null) {
            if(intent.hasExtra(TalkPushService.EXTRA_WAKE_CLIENT)) {
                wakeClient();
            }
            if(intent.hasExtra(TalkPushService.EXTRA_GCM_REGISTERED)) {
                doUpdateGcm(true);
            }
            if(intent.hasExtra(TalkPushService.EXTRA_GCM_UNREGISTERED)) {
                doUpdateGcm(true);
            }
        }
        return START_STICKY;
    }

    @Override
	public IBinder onBind(Intent intent) {
        LOG.info("onBind(" + intent.toString() + ")");

        if(!mClient.isActivated()) {
            mClient.activate();
        }

        Connection newConnection = new Connection(intent);

        mConnections.add(newConnection);

        return newConnection;
	}

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.info("onUnbind(" + intent.toString() + ")");
        return super.onUnbind(intent);
    }

    private void configureServiceUri() {
        String uriString = mPreferences.getString("preference_service_uri", "");
        if(uriString.isEmpty()) {
            uriString = TalkClientConfiguration.SERVER_URI;
        }
        URI uri = URI.create(uriString);
        mClient.setServiceUri(uri);
    }

    private void wakeClient() {
        if(mPreviousConnectionState) {
            mClient.wake();
        }
    }

    private void doVerifyGcm() {
        if(LOG.isDebugEnabled()) {
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
            if(manifestAllowsGcm) {
            try {
                GCMRegistrar.checkDevice(this);
                deviceSupportsGcm = true;
            } catch (UnsupportedOperationException ex) {
                LOG.warn("GCM not supported by device", ex);
            }
        }

        // make the final decision
        mGcmSupported = deviceSupportsGcm && manifestAllowsGcm;
        if(mGcmSupported) {
            LOG.info("GCM is supported");
        } else {
            LOG.warn("GCM not supported");
        }
    }

    private void doRegisterGcm(boolean forced) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("doRegisterGcm(" + (forced ? "forced" : "") + ")");
        }
        if(mGcmSupported) {
            if (forced || !GCMRegistrar.isRegistered(this)) {
                LOG.info("requesting GCM registration");
                GCMRegistrar.register(this, TalkConfiguration.GCM_SENDER_ID);
            }
        }
    }

    private void doUpdateGcm(boolean forced) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("doUpdateGcm(" + (forced ? "forced" : "") + ")");
        }
        if(mGcmSupported && GCMRegistrar.isRegistered(this)) {
            // check if we got here already
            if(forced || !GCMRegistrar.isRegisteredOnServer(this)) {
                LOG.info("updating GCM registration");
                // perform the registration call
                mClient.registerGcm(this.getPackageName(), GCMRegistrar.getRegistrationId(this));
                // set the registration timeout (XXX move elsewhere)
                GCMRegistrar.setRegisterOnServerLifespan(
                        this, TalkConfiguration.GCM_REGISTRATION_EXPIRATION * 1000);
                // tell the registrar that we did this successfully
                GCMRegistrar.setRegisteredOnServer(this, true);
            } else {
                LOG.debug("no need to update GCM registration");
            }
        } else {
            if(forced || GCMRegistrar.isRegisteredOnServer(this)) {
                LOG.info("retracting GCM registration");
                mClient.unregisterGcm();
                GCMRegistrar.setRegisteredOnServer(this, false);
            }
        }
    }

    private void doShutdown() {
        LOG.info("shutting down");
        // command the client to deactivate
        if(mClient.isActivated()) {
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
                        LOG.info("keep-alive timeout");
                        doShutdown();
                    }
                },
                TalkConfiguration.SERVICE_KEEPALIVE_TIMEOUT, TimeUnit.SECONDS
        );
    }

    private void shutdownShutdown() {
        if(mShutdownFuture != null) {
            mShutdownFuture.cancel(false);
            mShutdownFuture = null;
        }
    }

    private void registerConnectivityReceiver() {
        LOG.info("registerConnectivityReceiver()");
        if(mConnectivityReceiver == null) {
            mConnectivityReceiver = new ConnectivityReceiver();
            registerReceiver(mConnectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void unregisterConnectivityReceiver() {
        LOG.info("unregisterConnectivityReceiver()");
        if(mConnectivityReceiver != null) {
            unregisterReceiver(mConnectivityReceiver);
            mConnectivityReceiver = null;
        }
    }

    private void handleConnectivityChange(NetworkInfo activeNetwork) {
        if(activeNetwork == null) {
            LOG.info("connectivity change: no connectivity");
            mClient.deactivate();
            mPreviousConnectionState = false;
            mPreviousConnectionType = -1;
        } else {
            LOG.info("connectivity change:"
                    + " type " + activeNetwork.getTypeName()
                    + " state " + activeNetwork.getState().name());

            int previousState = mClient.getState();
            if(activeNetwork.isConnected()) {
                if(previousState <= HoccerTalkClient.STATE_INACTIVE) {
                    mClient.activate();
                }
            } else if(activeNetwork.isConnectedOrConnecting()) {
                if(previousState <= HoccerTalkClient.STATE_INACTIVE) {
                    mClient.activate();
                }
            } else {
                if(previousState > HoccerTalkClient.STATE_INACTIVE) {
                    mClient.deactivate();
                }
            }

            boolean netState = activeNetwork.isConnected();
            int netType = activeNetwork.getType();

            if(TalkConfiguration.CONNECTIVITY_RECONNECT_ON_CHANGE) {
                if(netState && !mClient.isIdle()) {
                    if(!mPreviousConnectionState
                            || mPreviousConnectionType == -1
                            || mPreviousConnectionType != netType) {
                        if(mClient.getState() < HoccerTalkClient.STATE_CONNECTING) {
                            mClient.reconnect("connection change");
                        }
                    }
                }
            }

            mPreviousConnectionState = netState;
            mPreviousConnectionType = netType;
        }
    }

    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.info("onConnectivityChange()");
            handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());
        }
    }

    private void updateNotification(List<TalkClientMessage> unseenMessages, boolean notify) {
        LOG.info("updateNotification()");
        TalkClientDatabase db = mClient.getDatabase();

        // determine where we are in time
        long now = System.currentTimeMillis();
        long passed = Math.max(0, now - mNotificationTimestamp);

        // cancel present notification if everything has been seen
        // we back off here to prevent interruption of any in-progress alarms
        if(unseenMessages == null || unseenMessages.isEmpty()) {
            LOG.debug("no unseen messages");
            long cancelTime = mNotificationTimestamp + TalkConfiguration.NOTIFICATION_CANCEL_BACKOFF;
            long delay = Math.max(0, cancelTime - now);
            mExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(NOTIFICATION_UNSEEN_MESSAGES);
                }
            }, delay, TimeUnit.MILLISECONDS);
            return;
        }

        // do not sound alarms overly often (sound, vibrate)
        if(passed < TalkConfiguration.NOTIFICATION_ALARM_BACKOFF) {
            notify = false;
        }

        // we are commited to notifying, update timestamp
        mNotificationTimestamp = now;

        // collect conversation contacts and sort messages accordingly
        List<TalkClientContact> contacts = new ArrayList<TalkClientContact>();
        Map<Integer, TalkClientContact> contactsById = new HashMap<Integer, TalkClientContact>();
        for(TalkClientMessage message: unseenMessages) {
            TalkClientContact contact = message.getConversationContact();
            int contactId = contact.getClientContactId();
            if(!contactsById.containsKey(contactId)) {
                contactsById.put(contactId, contact);
                contacts.add(contact);
                try {
                    db.refreshClientContact(contact);
                } catch (SQLException e) {
                    LOG.error("sql error", e);
                }
            }
        }

        // for easy reference
        int numUnseen = unseenMessages.size();
        int numContacts = contacts.size();

        // log about what we got
        LOG.debug("notifying " + numUnseen + " messages from " + numContacts + " contacts");

        // build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // always set the small icon (should be different depending on if we have a large one)
        builder.setSmallIcon(R.drawable.ic_notification);
        // large icon XXX
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        builder.setLargeIcon(largeIcon);
        // determine if alarms should be sounded
        if(notify) {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        // set total number of messages of more than one
        if(numUnseen > 1) {
            builder.setNumber(numUnseen);
        }
        // fill in content
        if(contacts.size() == 1) {
            TalkClientContact singleContact = contacts.get(0);
            // create intent to start the messaging activity for the right contact
            Intent messagingIntent = new Intent(this, MessagingActivity.class);
            messagingIntent.putExtra("clientContactId", singleContact.getClientContactId());
            // make a pending intent with correct back-stack
            PendingIntent pendingIntent =
                    TaskStackBuilder.create(this)
                        .addParentStack(ContactsActivity.class)
                        .addNextIntentWithParentStack(messagingIntent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            // add the intent to the notification
            builder.setContentIntent(pendingIntent);
            // title is always the contact name
            builder.setContentTitle(singleContact.getName());
            // text depends on number of messages
            if(unseenMessages.size() == 1) {
                TalkClientMessage singleMessage = unseenMessages.get(0);
                builder.setContentText(singleMessage.getText());
            } else {
                builder.setContentText(numUnseen + " new messages");
            }
        } else {
            // create pending intent
            Intent contactsIntent = new Intent(this, ContactsActivity.class);
            PendingIntent pendingIntent =
                    TaskStackBuilder.create(this)
                        .addNextIntent(contactsIntent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            // concatenate contact names
            StringBuilder sb = new StringBuilder();
            int last = contacts.size() - 1;
            for(int i = 0; i < contacts.size(); i++) {
                TalkClientContact contact = contacts.get(i);
                sb.append(contact.getName());
                if(i < last) {
                    sb.append(", ");
                }
            }
            // set fields
            builder.setContentTitle(sb.toString());
            builder.setContentText(numUnseen + " new messages");
        }

        // finish up
        Notification notification = builder.build();
        LOG.info("notification " + notification.toString());
        mNotificationManager.notify(NOTIFICATION_UNSEEN_MESSAGES, notification);
    }

    private void checkBinders() {
        for(Connection connection: mConnections) {
            boolean listenerAlive = true;
            if(connection.hasListener()) {
                listenerAlive = connection.getListener().asBinder().isBinderAlive();
            }
            if(!(connection.asBinder().isBinderAlive() && listenerAlive)) {
                mConnections.remove(connection);
            }
        }
    }

    private class ClientListener implements ITalkClientListener, ITalkTransferListener, ITalkUnseenListener {
        @Override
        public void onClientStateChange(HoccerTalkClient client, int state) {
            LOG.info("onClientStateChange(" + HoccerTalkClient.stateToString(state) + ")");
            if(state == HoccerTalkClient.STATE_ACTIVE) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        doRegisterGcm(TalkConfiguration.GCM_ALWAYS_REGISTER);
                        doUpdateGcm(TalkConfiguration.GCM_ALWAYS_UPDATE);
                    }
                });
            }
            checkBinders();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onClientStateChanged(mClient.getState());
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onPushRegistrationRequested() {
            LOG.info("onPushRegistrationRequested()");
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    doRegisterGcm(false);
                    doUpdateGcm(true);
                }
            });
        }

        @Override
        public void onContactAdded(TalkClientContact contact) {
            LOG.info("onContactAdded(" + contact.getClientContactId() + ")");
            checkBinders();
            int contactId = contact.getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onContactAdded(contactId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onContactRemoved(TalkClientContact contact) {
            LOG.info("onContactRemoved(" + contact.getClientContactId() + ")");
            checkBinders();
            int contactId = contact.getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onContactRemoved(contactId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onClientPresenceChanged(TalkClientContact contact) {
            LOG.info("onClientPresenceChanged(" + contact.getClientContactId() + ")");
            checkBinders();
            int contactId = contact.getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onClientPresenceChanged(contactId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onClientRelationshipChanged(TalkClientContact contact) {
            LOG.info("onClientRelationshipChanged(" + contact.getClientContactId() + ")");
            checkBinders();
            int contactId = contact.getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onClientRelationshipChanged(contactId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onGroupPresenceChanged(TalkClientContact contact) {
            LOG.info("onGroupPresenceChanged(" + contact.getClientContactId() + ")");
            checkBinders();
            int contactId = contact.getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onGroupPresenceChanged(contactId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onGroupMembershipChanged(TalkClientContact contact) {
            LOG.info("onGroupMembership(" + contact.getClientContactId() + ")");
            checkBinders();
            int contactId = contact.getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onGroupMembershipChanged(contactId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onMessageAdded(TalkClientMessage message) {
            LOG.info("onMessageAdded(" + message.getClientMessageId()  + ")");
            checkBinders();
            int messageId = message.getClientMessageId();
            int contactId = message.getConversationContact().getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onMessageAdded(contactId, messageId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onMessageRemoved(TalkClientMessage message) {
            LOG.info("onMessageRemoved(" + message.getClientMessageId()  + ")");
            checkBinders();
            int messageId = message.getClientMessageId();
            int contactId = message.getConversationContact().getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onMessageRemoved(contactId, messageId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onMessageStateChanged(TalkClientMessage message) {
            LOG.info("onMessageStateChanged(" + message.getClientMessageId()  + ")");
            checkBinders();
            int messageId = message.getClientMessageId();
            int contactId = message.getConversationContact().getClientContactId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onMessageStateChanged(contactId, messageId);
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onDownloadStarted(TalkClientDownload download) {
            LOG.info("onDownloadStarted(" + download.getClientDownloadId() + ")");
            checkBinders();
            int downloadId = download.getClientDownloadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onDownloadAdded(0, downloadId); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onDownloadProgress(TalkClientDownload download) {
            LOG.info("onDownloadProgress(" + download.getClientDownloadId() + ")");
            checkBinders();
            int downloadId = download.getClientDownloadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onDownloadProgress(0, downloadId); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onDownloadFinished(TalkClientDownload download) {
            LOG.info("onDownloadFinished(" + download.getClientDownloadId() + ")");
            checkBinders();
            int downloadId = download.getClientDownloadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onDownloadRemoved(0, downloadId); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
            if(download.getType().equals(TalkTransfer.Type.ATTACHMENT)) {
                String path = TalkApplication.getAttachmentLocation(download).toString();
                String type = download.getContentType();
                LOG.info("triggering media scan of " + path);
                MediaScannerConnection.scanFile(TalkClientService.this, new String[]{path}, new String[]{type},
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                LOG.info("completed scan of path " + path);
                                LOG.info("content uri is " + uri.toString());
                            }
                        });
            }
        }

        @Override
        public void onDownloadStateChanged(TalkClientDownload download) {
            LOG.info("onDownloadStateChanged(" + download.getClientDownloadId() + ")");
            checkBinders();
            int downloadId = download.getClientDownloadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onDownloadStateChanged(0, downloadId, download.getState().toString()); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onUploadStarted(TalkClientUpload upload) {
            LOG.info("onUploadStarted(" + upload.getClientUploadId() + ")");
            checkBinders();
            int downloadId = upload.getClientUploadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onUploadAdded(0, downloadId); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onUploadProgress(TalkClientUpload upload) {
            LOG.info("onUploadProgress(" + upload.getClientUploadId() + ")");
            checkBinders();
            int downloadId = upload.getClientUploadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onUploadProgress(0, downloadId); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onUploadFinished(TalkClientUpload upload) {
            LOG.info("onUploadFinished(" + upload.getClientUploadId() + ")");
            checkBinders();
            int downloadId = upload.getClientUploadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onUploadRemoved(0, downloadId); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onUploadStateChanged(TalkClientUpload upload) {
            LOG.info("onUploadStateChanged(" + upload.getClientUploadId() + ")");
            checkBinders();
            int downloadId = upload.getClientUploadId();
            for(Connection connection: mConnections) {
                if(connection.hasListener()) {
                    try {
                        connection.getListener().onUploadStateChanged(0, downloadId, upload.getState().toString()); // XXX
                    } catch (RemoteException e) {
                        LOG.error("callback error", e);
                    }
                }
            }
        }

        @Override
        public void onUnseenMessages(List<TalkClientMessage> unseenMessages, boolean notify) {
            updateNotification(unseenMessages, notify);
        }
    }

    public class Connection extends ITalkClientService.Stub {

        int mId;

        Intent mBindIntent;

        ITalkClientServiceListener mListener;

        Connection(Intent bindIntent) {
            mId = ID_COUNTER.incrementAndGet();
            mBindIntent = bindIntent;
            mListener = null;
            LOG.info("[" + mId + "] connected");
        }

        public boolean hasListener() {
            return mListener != null;
        }

        public ITalkClientServiceListener getListener() {
            return mListener;
        }

        @Override
        public int getClientState() throws RemoteException {
            return mClient.getState();
        }

        @Override
        public void setClientName(String newName) throws RemoteException {
            LOG.info("[" + mId + "] setClientName(" + newName + ")");
            mClient.setClientString(newName, null);
        }

        @Override
        public void setClientStatus(String newStatus) throws RemoteException {
            LOG.info("[" + mId + "] setClientStatus(" + newStatus + ")");
            mClient.setClientString(null, newStatus);
        }

        @Override
        public void setClientAvatar(int uploadId) throws RemoteException {
            LOG.info("[" + mId + "] setClientAvatar(" + uploadId + ")");
            try {
                mClient.setClientAvatar(mClient.getDatabase().findClientUploadById(uploadId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
		public void keepAlive() throws RemoteException {
            LOG.info("[" + mId + "] keepAlive()");
            scheduleShutdown();
		}

        @Override
        public void wake() throws RemoteException {
            LOG.info("[" + mId + "] wake()");
            wakeClient();
        }

        @Override
        public void reconnect() throws RemoteException {
            LOG.info("[" + mId + "] reconnect()");
            mClient.reconnect("client request");
        }

        @Override
        public void setListener(ITalkClientServiceListener listener) throws RemoteException {
            LOG.info("[" + mId + "] setListener()");
            mListener = listener;
        }

        @Override
        public void performDeliveries() throws RemoteException {
            LOG.info("[" + mId + "] requestDelivery()");
            mClient.requestDelivery();
        }

        @Override
        public void createGroup() throws RemoteException {
            LOG.info("[" + mId + "] createGroup()");
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    LOG.info("[" + mId + "] creating group");
                    try {
                        TalkClientContact contact = mClient.createGroup();
                        LOG.info("[" + mId + "] group creation ok");
                        mListener.onGroupCreationSucceeded(contact.getClientContactId());
                    } catch (Throwable t) {
                        LOG.error("[" + mId + "] group creation failed");
                        try {
                            mListener.onGroupCreationFailed();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void inviteToGroup(int groupContactId, int clientContactId) throws RemoteException {
            LOG.info("[" + mId + "] inviteToGroup(" + groupContactId + "," + clientContactId + ")");
            try {
                mClient.inviteClientToGroup(
                        mClient.getDatabase().findClientContactById(groupContactId).getGroupId(),
                        mClient.getDatabase().findClientContactById(clientContactId).getClientId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void joinGroup(int contactId) throws RemoteException {
            LOG.info("[" + mId + "] joinGroup(" + contactId + ")");
            try {
                mClient.joinGroup(mClient.getDatabase().findClientContactById(contactId).getGroupId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void leaveGroup(int contactId) throws RemoteException {
            LOG.info("[" + mId + "] leaveGroup(" + contactId + ")");
            try {
                mClient.leaveGroup(mClient.getDatabase().findClientContactById(contactId).getGroupId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String generatePairingToken() throws RemoteException {
            LOG.info("[" + mId + "] generatePairingToken()");
            String res = mClient.generatePairingToken();
            LOG.info("token: " + res);
            return res;
        }

        @Override
        public void pairUsingToken(final String token) throws RemoteException {
            LOG.info("[" + mId + "] pairUsingToken(" + token + ")");
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    LOG.info("[" + mId + "] pairing");
                    try {
                        if(mClient.performTokenPairing(token)) {
                            mListener.onTokenPairingSucceeded(token);
                        } else {
                            mListener.onTokenPairingFailed(token);
                        }
                        LOG.info("[" + mId + "] pairing ok");
                    } catch (Throwable t) {
                        LOG.error("[" + mId + "] pairing failed", t);
                        try {
                            mListener.onTokenPairingFailed(token);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void depairContact(int contactId) throws RemoteException {
            LOG.info("[" + mId + "] depairContact(" + contactId + ")");
            try {
                mClient.depairContact(mClient.getDatabase().findClientContactById(contactId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void deleteContact(int contactId) throws RemoteException {
            LOG.info("[" + mId + "] deleteContact(" + contactId + ")");
            // XXX
        }

        @Override
        public void blockContact(int contactId) throws RemoteException {
            LOG.info("[" + mId + "] blockContact(" + contactId + ")");
            try {
                mClient.blockContact(mClient.getDatabase().findClientContactById(contactId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void unblockContact(int contactId) throws RemoteException {
            LOG.info("[" + mId + "] unblockContact(" + contactId + ")");
            try {
                mClient.unblockContact(mClient.getDatabase().findClientContactById(contactId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void requestDownload(int clientDownloadId) throws RemoteException {
            LOG.info("[" + mId + "] requestDownload(" + clientDownloadId + ")");
            try {
                mClient.requestDownload(mClient.getDatabase().findClientDownloadById(clientDownloadId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void markAsSeen(int clientMessageId) throws RemoteException {
            LOG.info("[" + mId + "] markAsSeen(" + clientMessageId + ")");
            try {
                mClient.markAsSeen(mClient.getDatabase().findClientMessageById(clientMessageId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
