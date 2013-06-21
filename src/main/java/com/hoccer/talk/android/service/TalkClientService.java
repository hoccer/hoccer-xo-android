package com.hoccer.talk.android.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.android.gcm.GCMRegistrar;
import com.hoccer.talk.android.TalkConfiguration;
import com.hoccer.talk.android.database.AndroidTalkDatabase;
import com.hoccer.talk.android.push.TalkPushService;
import com.hoccer.talk.client.HoccerTalkClient;
import com.hoccer.talk.client.ITalkClientListener;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.hoccer.talk.client.model.TalkClientContact;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
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

    private static final AtomicInteger ID_COUNTER =
        new AtomicInteger();

    /** Connectivity manager for monitoring */
    ConnectivityManager mConnectivityManager;

    /** Our connectivity change broadcast receiver */
    ConnectivityReceiver mConnectivityReceiver;

    /** Executor for ourselves and the client */
    ScheduledExecutorService mExecutor;

    /** Hoccer client that we serve */
    HoccerTalkClient mClient;

    /** Reference to latest auto-shutdown future */
    ScheduledFuture<?> mShutdownFuture;

    ArrayList<Connection> mConnections;

	@Override
	public void onCreate() {
        LOG.info("onCreate()");
		super.onCreate();

        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        mConnections = new ArrayList<Connection>();

        mClient = new HoccerTalkClient(mExecutor, AndroidTalkDatabase.getInstance(this.getApplicationContext()));
        mClient.registerListener(new ClientListener());
	}

    @Override
    public void onDestroy() {
        LOG.info("onDestroy()");
        super.onDestroy();
        mExecutor.shutdownNow();
        unregisterConnectivityReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.info("onStartCommand(" + ((intent == null) ? "null" : intent.toString()) + ")");
        if(intent != null) {
            if(intent.hasExtra(TalkPushService.EXTRA_WAKE)) {
                mClient.wake();
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

    private void doUpdateGcm() {
        LOG.info("updating GCM registration");
        // only if we are registered (registration triggers this code path)
        if(GCMRegistrar.isRegistered(this)) {
            // check if we got here already
            if(TalkConfiguration.GCM_ALWAYS_REGISTER || !GCMRegistrar.isRegisteredOnServer(this)) {
                // perform the registration call
                mClient.registerGcm(this.getPackageName(),
                        GCMRegistrar.getRegistrationId(this));
                // set the registration timeout (XXX move elsewhere)
                GCMRegistrar.setRegisterOnServerLifespan(
                        this, TalkConfiguration.GCM_REGISTRATION_EXPIRATION * 1000);
                // tell the registrar that we did this successfully
                GCMRegistrar.setRegisteredOnServer(this, true);
            } else {
                LOG.info("already registered on server");
            }
        } else {
            LOG.info("not registered yet");
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
        } else {
            LOG.info("connectivity change:"
                    + " type " + activeNetwork.getTypeName()
                    + " state " + activeNetwork.getState().name());
            if(activeNetwork.isConnectedOrConnecting()) {
                if(mClient.getState() == HoccerTalkClient.STATE_INACTIVE) {
                    mClient.activate();
                }
                if(mClient.getState() <= HoccerTalkClient.STATE_CONNECTING) {
                    mClient.wake();
                }
            } else {
                if(mClient.getState() >= HoccerTalkClient.STATE_INACTIVE) {
                    mClient.deactivate();
                }
            }
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
            if(state == HoccerTalkClient.STATE_IDLE) {
                registerConnectivityReceiver();
                handleConnectivityChange(mConnectivityManager.getActiveNetworkInfo());
            }
            if(state == HoccerTalkClient.STATE_INACTIVE) {
                unregisterConnectivityReceiver();
            }
            if(state == HoccerTalkClient.STATE_ACTIVE) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        doUpdateGcm();
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
            mClient.setClientString(newName, null);
        }

        @Override
        public void setClientStatus(String newStatus) throws RemoteException {
            mClient.setClientString(null, newStatus);
        }

        @Override
		public void keepAlive()
                throws RemoteException {
            LOG.info("[" + mId + "] keepAlive()");
            scheduleShutdown();
		}

        @Override
        public void wake()
                throws RemoteException {
            LOG.info("[" + mId + "] wake()");
            mClient.wake();
        }

        @Override
        public void setListener(ITalkClientServiceListener listener)
                throws RemoteException {
            LOG.info("[" + mId + "] setListener()");
            mListener = listener;
        }

        @Override
        public void messageCreated(String messageTag) throws RemoteException {
            LOG.info("[" + mId + "] messageCreated(" + messageTag + ")");
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
                        LOG.error("[" + mId + "] group creation ok");
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
