package com.hoccer.talk.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.hoccer.talk.android.activity.AboutActivity;
import com.hoccer.talk.android.activity.PairingActivity;
import com.hoccer.talk.android.activity.ProfileActivity;
import com.hoccer.talk.android.database.AndroidTalkDatabase;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.android.service.TalkClientService;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class TalkActivity extends SherlockFragmentActivity implements ITalkActivity {

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

    public TalkActivity() {
        LOG = Logger.getLogger(getClass());
    }

    protected abstract int getLayoutResource();

    public ScheduledExecutorService getBackgroundExecutor() {
        return mBackgroundExecutor;
    }

    public ITalkClientService getService() {
        return mService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("onCreate()");

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
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOG.info("onResume()");

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
        super.onPause();
        LOG.info("onPause()");

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
        super.onDestroy();
        LOG.info("onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.info("onCreateOptionsMenu()");
        getSupportMenuInflater().inflate(R.menu.common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.info("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_myprofile:
                try {
                    showContactProfile(mDatabase.findSelfContact(false));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menu_about:
                showAbout();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
                // XXX fault
                e.printStackTrace();
            }
        }
    }

    /**
     * Connection to our backend service
     */
    public class MainServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOG.info("onServiceConnected()");
            mService = (ITalkClientService)service;
            scheduleKeepAlive();
            attachServiceListener();
            try {
                mService.wake();
            } catch (RemoteException e) {
                e.printStackTrace();  // XXX
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOG.info("onServiceDisconnected()");
            shutdownKeepAlive();
            mService = null;
            // XXX this should be indicated
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
        public void onTokenPairingFailed(String token) throws RemoteException {
        }
        @Override
        public void onTokenPairingSucceeded(String token) throws RemoteException {
        }
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
    public BaseAdapter makeMessageListAdapter() {
        return null;
    }

    @Override
    public BaseAdapter makeContactListAdapter() {
        ContactListAdapter a = new ContactListAdapter(this);
        try {
            a.addAll(mDatabase.findAllClientContacts());
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
        return a;
    }

    @Override
    public void showContactProfile(TalkClientContact contact) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("clientContactId", contact.getClientContactId());
        startActivity(intent);
    }

    @Override
    public void showPairing() {
        startActivity(new Intent(this, PairingActivity.class));
    }

    @Override
    public void showAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public class ContactListAdapter extends ArrayAdapter<TalkClientContact> {

        LayoutInflater mInflater;

        public ContactListAdapter(Context context) {
            super(context, R.id.contact_name);
            mInflater = getLayoutInflater();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if(v == null) {
                v = mInflater.inflate(R.layout.item_contact, null);
            }

            TextView nameView = (TextView)v.findViewById(R.id.contact_name);
            nameView.setText(getItem(position).getContactType());

            return v;
        }

    }

}
