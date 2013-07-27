package com.hoccer.talk.android.service;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Service;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import com.google.android.gcm.GCMRegistrar;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.android.database.AndroidTalkDatabase;
import com.hoccer.talk.android.push.TalkPushService;
import com.hoccer.talk.client.HoccerTalkClient;
import com.hoccer.talk.client.ITalkClientListener;

import android.os.IBinder;
import android.os.RemoteException;
import com.hoccer.talk.client.TalkClientConfiguration;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientMessage;
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

    boolean mGcmSupported;

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mExecutor = TalkApplication.getExecutor();

        mConnections = new ArrayList<Connection>();

        mClient = new HoccerTalkClient(mExecutor, AndroidTalkDatabase.getInstance(this.getApplicationContext()));
        mClient.registerListener(new ClientListener());

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

    private class ClientListener implements ITalkClientListener {
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
        }

        @Override
        public void onMessageRemoved(TalkClientMessage message) {
            LOG.info("onMessageRemoved(" + message.getClientMessageId()  + ")");
        }

        @Override
        public void onMessageStateChanged(TalkClientMessage message) {
            LOG.info("onMessageStateChanged(" + message.getClientMessageId()  + ")");
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
                        mClient.performTokenPairing(token);
                        mListener.onTokenPairingSucceeded(token);
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
    }

}
