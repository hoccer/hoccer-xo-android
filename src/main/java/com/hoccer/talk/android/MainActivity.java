package com.hoccer.talk.android;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.google.android.gcm.GCMRegistrar;
import com.hoccer.talk.android.fragment.ContactsFragment;
import com.hoccer.talk.android.fragment.MenuFragment;
import com.hoccer.talk.android.fragment.MessagingFragment;
import com.hoccer.talk.android.fragment.AboutFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.android.service.TalkClientService;
import com.hoccer.talk.model.TalkMessage;
import org.apache.log4j.Logger;

public class MainActivity extends SherlockFragmentActivity implements ITalkActivity {

    /** Logger for the activity */
    private static final Logger LOG = Logger.getLogger(MainActivity.class);


    /** Number of views in the ViewPager */
    private static final int NUM_VIEWS = 4;
    /** Index of the menu view in the ViewPager */
    private static final int VIEW_MENU = 0;
    /** Index of the messaging view in the ViewPager */
	private static final int VIEW_MESSAGING = 1;
    /** Index of the about view in the ViewPager */
    private static final int VIEW_ABOUT = 2;
    /** Index of the contact list in the ViewPager */
    private static final int VIEW_CONTACTS  = 3;

    /** Executor for background tasks */
    ScheduledExecutorService mBackgroundExecutor;

    /** Our fragment manager for dealing with transitions and such */
	FragmentManager mFragmentManager;

    /** The view pager we use for fragment sliding */
	ViewPager mViewPager;

    /** Our pager adapter for the ViewPager */
	MainPagerAdapter mViewPagerAdapter;

    /** Our actionbar */
    ActionBar mActionBar;


    /** Fragment for contact management */
    ContactsFragment mContactsFragment;
    /** Fragment for messaging */
    MessagingFragment mMessagingFragment;
    /** Fragment for about */
    AboutFragment mAboutFragment;
    /** Fragment for menu */
    MenuFragment mMenuFragment;


    /** RPC interface to service (null when not connected) */
    ITalkClientService mService;

    /** Service connection object managing mService */
    ServiceConnection mServiceConnection;

    /** Timer for keepalive calls to the service */
    ScheduledFuture<?> mKeepAliveTimer;


	@Override
	protected void onCreate(Bundle state) {
        LOG.info("onCreate()");
		super.onCreate(state);

		// get the fragment manager
		mFragmentManager = getSupportFragmentManager();
		
		// apply the layout (this is indirect and depends on the device)
		setContentView(R.layout.activity_main);
		
		// get and configure the action bar
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		// set up fragment handling
		mViewPager = (ViewPager)findViewById(R.id.main_fragment_pager);
		if(mViewPager != null) {
            // if we have a ViewPager we use a one-pane layout (handset version)
            LOG.info("using one-pane handset layout");
			mViewPagerAdapter = new MainPagerAdapter();
			
			mViewPager.setAdapter(mViewPagerAdapter);
			mViewPager.setCurrentItem(VIEW_MESSAGING);
		} else {
            // if we don't have a ViewPager we use a two-pane layout (tablet version)
            LOG.info("using two-pane tablet layout");
			mContactsFragment = (ContactsFragment)
					mFragmentManager.findFragmentById(
							R.id.main_fragment_contacts);
			mMessagingFragment = (MessagingFragment)
					mFragmentManager.findFragmentById(
							R.id.main_fragment_messaging);
		}

        // trigger GCM registration
        if(!GCMRegistrar.isRegistered(this)) {
            GCMRegistrar.register(this, TalkConfiguration.GCM_SENDER_ID);
        }
	}

    @Override
    protected void onResume() {
        LOG.info("onResume()");
        super.onResume();

        // launch a new background executor
        mBackgroundExecutor = Executors.newSingleThreadScheduledExecutor();

        // start the backend service and bind to it
        Intent serviceIntent = new Intent(getApplicationContext(), TalkClientService.class);
        startService(serviceIntent);
        mServiceConnection = new MainServiceConnection();
        bindService(serviceIntent, mServiceConnection, BIND_IMPORTANT);
    }

    @Override
    protected void onPause() {
        LOG.info("onPause()");
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
        // stop any remaining background tasks
        if(mBackgroundExecutor != null) {
            mBackgroundExecutor.shutdownNow();
            mBackgroundExecutor = null;
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        LOG.info("onCreateOptionsMenu()");
		getSupportMenuInflater().inflate(R.menu.common, menu);
		return true;
	}

    /**
     * Retrieve reference to service binder
     * @return
     */
    @Override
    public ITalkClientService getTalkClientService() {
        return mService;
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
    public class MainServiceListener extends ITalkClientServiceListener.Stub {
        @Override
        public void messageCreated(String messageTag) throws RemoteException {
            LOG.info("callback messageCreated(" + messageTag + ")");
        }

        @Override
        public void messageDeleted(String messageTag) throws RemoteException {
            LOG.info("callback messageDeleted(" + messageTag + ")");
        }

        @Override
        public void deliveryCreated(String messageTag, String receiverId) throws RemoteException {
            LOG.info("callback deliveryCreated(" + messageTag + "," + receiverId + ")");
        }

        @Override
        public void deliveryChanged(String messageTag, String receiverId) throws RemoteException {
            LOG.info("callback deliveryChanged(" + messageTag + "," + receiverId + ")");
        }

        @Override
        public void deliveryDeleted(String messageTag, String receiverId) throws RemoteException {
            LOG.info("callback deliveryDeleted(" + messageTag + "," + receiverId + ")");
        }
    }

    /**
     * Pager adapter for our ViewPager
     *
     * Our view pager calls back to this adapter to
     * get the fragments it should display.
     */
	public class MainPagerAdapter extends FragmentPagerAdapter {
		public MainPagerAdapter() {
			super(mFragmentManager);
		}
        @Override
        public int getCount() {
            return NUM_VIEWS;
        }
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
            // select and create the appropriate fragment
			switch(position) {
			case VIEW_CONTACTS:
				if(mContactsFragment == null) {
					mContactsFragment = new ContactsFragment();
				}
				fragment = mContactsFragment;
				break;
			case VIEW_MESSAGING:
				if(mMessagingFragment == null) {
					mMessagingFragment = new MessagingFragment();
				}
				fragment = mMessagingFragment;
				break;
            case VIEW_ABOUT:
                if(mAboutFragment == null) {
                    mAboutFragment = new AboutFragment(MainActivity.this, MainActivity.this);
                }
                fragment = mAboutFragment;
                break;
            case VIEW_MENU:
                if(mMenuFragment == null) {
                    mMenuFragment = new MenuFragment();
                }
                fragment = mMenuFragment;
                break;
			}
			return fragment;
		}
	}










	public BaseAdapter makeContactListAdapter() {
		ContactListAdapter a = new ContactListAdapter(this);
		a.add("Pia");
		a.add("Udo");
		a.add("George");
		return a;
	}
	
	public class ContactListAdapter extends ArrayAdapter<String> {

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
			
			return v;
		}
		
	}

    public BaseAdapter makeMessageListAdapter() {
		MessageListAdapter a = new MessageListAdapter(this);
		generateTestMessages(a);
		return a;
	}
	
	private void generateTestMessages(MessageListAdapter a) {
		for(int i = 0; i < 10; i++) {
			boolean foo = (i & 2) == 0;
			TalkMessage m = new TalkMessage();
			a.add(m);
		}
	}
	
	class MessageListAdapter extends ArrayAdapter<TalkMessage> {

		LayoutInflater mInflater;
		
		public MessageListAdapter(Context context) {
			super(context, R.layout.item_message, R.id.item_content, new ArrayList<TalkMessage>());
			mInflater = getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TalkMessage m = getItem(position);
			View v = convertView;
			
			if(v == null) {
				v = mInflater.inflate(R.layout.item_message, null);
			}
			
			TextView content = (TextView)v.findViewById(R.id.item_content);
			TextView sender = (TextView)v.findViewById(R.id.item_sender_name);
			
			content.setText(m.getBody());
			sender.setText(m.getSenderId());
			
			return v;
		}
		
	}

}
