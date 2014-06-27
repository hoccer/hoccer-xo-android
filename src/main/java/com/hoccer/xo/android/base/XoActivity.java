package com.hoccer.xo.android.base;

import android.app.*;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.hoccer.talk.client.IXoAlertListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientConfiguration;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.XoSoundPool;
import com.hoccer.xo.android.activity.*;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.RichContactsAdapter;
import com.hoccer.xo.android.content.*;
import com.hoccer.xo.android.content.contentselectors.ImageSelector;
import com.hoccer.xo.android.database.AndroidTalkDatabase;
import com.hoccer.xo.android.service.IXoClientService;
import com.hoccer.xo.android.service.XoClientService;
import com.hoccer.xo.android.view.chat.attachments.AttachmentTransferControlView;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;

import net.hockeyapp.android.CrashManager;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.provider.MediaStore;
import android.provider.Telephony;
import android.view.*;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base class for our activities
 * These activites continually keep the background service which
 * we use for connection retention alive by calling it via RPC.
 */
public abstract class XoActivity extends FragmentActivity {

    public final static int REQUEST_SELECT_AVATAR = 23;

    public final static int REQUEST_CROP_AVATAR = 24;

    public final static int REQUEST_SELECT_ATTACHMENT = 42;

    public final static int REQUEST_SCAN_BARCODE = IntentIntegrator.REQUEST_CODE; // XXX dirty

    protected Logger LOG = null;

    /**
     * Executor for background tasks
     */
    ScheduledExecutorService mBackgroundExecutor;

    /**
     * RPC interface to service (null when not connected)
     */
    IXoClientService mService;

    /**
     * Service connection object managing mService
     */
    ServiceConnection mServiceConnection;

    /**
     * Timer for keepalive calls to the service
     */
    ScheduledFuture<?> mKeepAliveTimer;

    /**
     * Talk client database
     */
    XoClientDatabase mDatabase;

    /**
     * List of all talk fragments
     */
    ArrayList<IXoFragment> mTalkFragments = new ArrayList<IXoFragment>();

    /**
     * Ongoing avatar selection
     */
    ContentSelection mAvatarSelection = null;

    /**
     * Ongoing attachment selection
     */
    ContentSelection mAttachmentSelection = null;

    /**
     * ZXing wrapper service
     */
    IntentIntegrator mBarcodeService = null;

    boolean mUpEnabled = false;

    private ActionBar mActionBar;

    private String mBarcodeToken = null;

    private AttachmentTransferControlView mSpinner;
    private Handler mDialogDismisser;
    private Dialog mDialog;
    private ScreenReceiver mScreenListener;
    private XoAlertListener mAlertListener;



    public XoActivity() {
        LOG = Logger.getLogger(getClass());
    }

    protected abstract int getLayoutResource();

    protected abstract int getMenuResource();

    public XoClient getXoClient() {
        return XoApplication.getXoClient();
    }

    public XoSoundPool getXoSoundPool() {
        return XoApplication.getXoSoundPool();
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


    // Application background/foreground observation
    public static boolean isAppInBackground = false;
    public static boolean isWindowFocused = false;
    public static boolean isMenuOpened = false;
    public static boolean isBackPressed = false;
    public static boolean isBackgroundActive = false;

    protected void applicationWillEnterForeground() {
        LOG.debug("Application will enter foreground.");
        isAppInBackground = false;
        isBackgroundActive = false;
        XoApplication.enterForegroundMode();
    }

    protected void applicationWillEnterBackground() {
        LOG.debug("Application will enter background.");
        isAppInBackground = true;
        XoApplication.enterBackgroundMode();
    }

    protected void applicationWillEnterBackgroundActive() {
        LOG.debug("Application will enter background active.");
        isAppInBackground = false;
        XoApplication.enterBackgroundActiveMode();
    }

    protected void setBackgroundActive() {
        isBackgroundActive = true;
    }

    public void startExternalActivity(Intent intent) {
        LOG.debug(getClass() + " starting external activity " + intent.toString());
        setBackgroundActive();
        startActivity(intent);
    }

    public void startExternalActivityForResult(Intent intent, int requestCode) {
        LOG.debug(getClass() + " starting external activity " +  intent.toString() + " for request code: " + requestCode);
        setBackgroundActive();
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onStart() {
        if (isAppInBackground || isBackgroundActive) {
            applicationWillEnterForeground();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isWindowFocused) {
            if (isBackgroundActive) {
                applicationWillEnterBackgroundActive();
            } else {
                applicationWillEnterBackground();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!(this instanceof ContactsActivity)) {
            isBackPressed = true;
        }
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;
        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    /*@Override
    public boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }*/

    //--------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        // set up database connection
        mDatabase = new XoClientDatabase(
                AndroidTalkDatabase.getInstance(this.getApplicationContext()));
        try {
            mDatabase.initialize();
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }

        // set layout
        setContentView(getLayoutResource());

        // get and configure the action bar
        mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // get the barcode scanning service
        mBarcodeService = new IntentIntegrator(this);

        // screen state listener
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenListener = new ScreenReceiver();
        registerReceiver(mScreenListener, filter);

        mAlertListener = new XoAlertListener(this);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        checkForCrashesIfEnabled();

        // get the background executor
        mBackgroundExecutor = XoApplication.getExecutor();

        // start the backend service and bind to it
        Intent serviceIntent = new Intent(getApplicationContext(), XoClientService.class);
        startService(serviceIntent);
        mServiceConnection = new MainServiceConnection();
        bindService(serviceIntent, mServiceConnection, BIND_IMPORTANT);
        checkKeys();

        getXoClient().registerAlertListener(mAlertListener);
    }

    private void checkForCrashesIfEnabled() {
        if (XoConfiguration.reportingEnable()) {
            CrashManager.register(this, XoConfiguration.HOCKEYAPP_ID);
        }
    }

    private void checkKeys() {
        if (XoConfiguration.needToRegenerateKey()) {
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
                    XoConfiguration.setRegenerationDone();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    mDialogDismisser.sendEmptyMessage(0);
                }
            }
        });
        t.start();
    }

    public void createDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.waiting_dialog, null);
        mSpinner = (AttachmentTransferControlView) view.findViewById(R.id.content_progress);

        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
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
                try {
                    mDialog.dismiss();
                    mSpinner.completeAndGone();
                } catch (IllegalArgumentException e) {
                    LOG.error("Dialog is not attached to current activity.");
                    e.printStackTrace();
                    //TODO: Once upon a time we will redesign all this stuff... Maybe.
                }
            }
        };
        spinnerStarter.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();

        // stop keeping the service alive
        shutdownKeepAlive();

        // drop reference to service binder
        if (mService != null) {
            mService = null;
        }
        // unbind service connection
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }

        getXoClient().unregisterAlertListener(mAlertListener);
    }

    @Override
    protected void onDestroy() {
        LOG.debug("onDestroy()");
        unregisterReceiver(mScreenListener);
        super.onDestroy();
    }

    private class ScreenReceiver extends BroadcastReceiver {

        private boolean wasScreenOn = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                wasScreenOn = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                wasScreenOn = true;
            }
        }

        public boolean isScreenOn() {
            return wasScreenOn;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LOG.debug("onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.common, menu);
        int activityMenu = getMenuResource();
        if (activityMenu >= 0) {
            getMenuInflater().inflate(activityMenu, menu);
        }
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
                showNewGroup();
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

    private Intent selectedAvatarPreProcessing(Intent data) {
        String uuid = UUID.randomUUID().toString();
        String filePath = XoApplication.getAvatarDirectory().getPath() + File.separator + uuid
                + ".jpg";
        String croppedImagePath = XoApplication.getAttachmentDirectory().getAbsolutePath()
                + File.separator
                + "tmp_crop";
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(croppedImagePath);
            if (bitmap == null) {
                return null;
            }
            File avatarFile = new File(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream(avatarFile));
            Uri uri = getImageContentUri(getBaseContext(), avatarFile);
            data.setData(uri);

            File tmpImage = new File(croppedImagePath);
            if (tmpImage.exists()) {
                tmpImage.delete();
            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOG.debug("onActivityResult(" + requestCode + "," + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if (requestCode == REQUEST_SELECT_AVATAR) {
            if (mAvatarSelection != null) {
                ImageSelector selector = (ImageSelector) mAvatarSelection.getSelector();
                startExternalActivityForResult(selector.createCropIntent(this, data.getData()),
                        REQUEST_CROP_AVATAR);
            }
            return;
        }

        if (requestCode == REQUEST_CROP_AVATAR) {
            data = selectedAvatarPreProcessing(data);
            if (data != null) {
                IContentObject co = ContentRegistry.get(this).createSelectedAvatar(mAvatarSelection,
                        data);
                if (co != null) {
                    LOG.debug("selected avatar " + co.getContentDataUrl());
                    for (IXoFragment fragment : mTalkFragments) {
                        fragment.onAvatarSelected(co);
                    }
                }
            } else {
                Toast.makeText(this, R.string.error_avatar_selection, Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (requestCode == REQUEST_SELECT_ATTACHMENT) {
            IContentObject co = ContentRegistry.get(this)
                    .createSelectedAttachment(mAttachmentSelection, data);
            if (co != null) {
                LOG.debug("selected attachment " + co.getContentDataUrl());
                for (IXoFragment fragment : mTalkFragments) {
                    fragment.onAttachmentSelected(co);
                }
            }
            return;
        }

        if (requestCode == REQUEST_SCAN_BARCODE) {
            IntentResult barcode = IntentIntegrator
                    .parseActivityResult(requestCode, resultCode, data);
            if (barcode != null) {
                LOG.debug("scanned barcode: " + barcode.getContents());
                String code = barcode.getContents();
                if (code.startsWith(XoClientConfiguration.HXO_URL_SCHEME)) {
                    mBarcodeToken = code.replace(XoClientConfiguration.HXO_URL_SCHEME, "");
                }
            }
        }
    }

    protected void enableUpNavigation() {
        LOG.debug("enableUpNavigation()");
        mUpEnabled = true;
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("NewApi")
    private void navigateUp() {
        LOG.debug("navigateUp()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && mUpEnabled) {
            Intent upIntent = getParentActivityIntent();
            if (upIntent != null) {
                // we have a parent, navigate up
                if (shouldUpRecreateTask(upIntent)) {
                    // we are not on our own task stack, so create one
                    TaskStackBuilder.create(this)
                            // add parents to back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // navigate up to next parent
                            .startActivities();
                } else {
                    // we are on our own task stack, so navigate upwards
                    navigateUpTo(upIntent);
                }
            } else {
                // we don't have a parent, navigate back instead
                onBackPressed();
            }
        } else {
            onBackPressed();
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
                                                                          if (mService != null) {
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
                TimeUnit.SECONDS
        );
    }

    /**
     * Stop sending keep-alive calls to the service
     */
    private void shutdownKeepAlive() {
        if (mKeepAliveTimer != null) {
            mKeepAliveTimer.cancel(false);
            mKeepAliveTimer = null;
        }
    }

    public IXoClientService getXoService() {
        return mService;
    }

    public XoClientDatabase getXoDatabase() {
        return mDatabase;
    }

    public ContactsAdapter makeContactListAdapter() {
        return new RichContactsAdapter(this);
    }

//    public ConversationAdapter makeConversationAdapter() {
//        return new ConversationAdapter(this);
//    }

    public void wakeClient() {
        if (mService != null) {
            try {
                mService.wake();
            } catch (RemoteException e) {
                LOG.error("remote error", e);
            }
        }
    }

    public void showContactProfile(TalkClientContact contact) {
        LOG.debug("showContactProfile(" + contact.getClientContactId() + ")");
        Intent intent;
        if (contact.isGroup()) {
            intent = new Intent(this, GroupProfileActivity.class);
            intent.putExtra(GroupProfileActivity.EXTRA_CLIENT_CONTACT_ID,
                    contact.getClientContactId());
        } else {
            intent = new Intent(this, SingleProfileActivity.class);
            intent.putExtra(SingleProfileActivity.EXTRA_CLIENT_CONTACT_ID,
                    contact.getClientContactId());
        }
        startActivity(intent);
    }

    public void showNewGroup() {
        LOG.debug("showNewGroup()");
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra(GroupProfileActivity.EXTRA_CLIENT_CREATE_GROUP, true);
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

    public void showPreferences() {
        LOG.debug("showPreferences()");
        startActivity(new Intent(this, XoPreferenceActivity.class));
    }

    public void selectAvatar() {
        LOG.debug("selectAvatar()");
        mAvatarSelection = ContentRegistry.get(this).selectAvatar(this, REQUEST_SELECT_AVATAR);
    }

    public void selectAttachment() {
        LOG.debug("selectAttachment()");

        setBackgroundActive();

        mAttachmentSelection = ContentRegistry.get(this)
                .selectAttachment(this, REQUEST_SELECT_ATTACHMENT);
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
                final String token = getXoClient().generatePairingToken();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBarcodeService.shareText(XoClientConfiguration.HXO_URL_SCHEME + token);
                    }
                });
            }
        });
    }

    public void composeInviteSms(String token) {
        LOG.debug("composeInviteSms(" + token + ")");

        try {
            TalkClientContact self = mDatabase.findSelfContact(false);

            String message = String
                    .format(getString(R.string.sms_invitation_text), XoClientConfiguration.HXO_URL_SCHEME, token, self.getName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //At least KitKat
                String defaultSmsPackageName = Telephony.Sms
                        .getDefaultSmsPackage(this); //Need to change the build to API 19

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);

                if (defaultSmsPackageName != null) {
                    sendIntent.setPackage(defaultSmsPackageName);
                }
                startActivity(sendIntent);
            } else {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"));
                intent.putExtra("sms_body", message);

                startActivity(intent);
            }
        } catch (SQLException e) {
            LOG.error("sql error", e);
        }
    }

    public void hackReturnedFromDialog() {
    }

    public void showPopupForMessageItem(ChatMessageItem messageItem, View messageItemView) {
    }

    public void clipBoardItemSelected(IContentObject contentObject) {
    }

    /**
     * Connection to our backend service
     */
    public class MainServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOG.debug("onServiceConnected()");
            mService = (IXoClientService) service;
            scheduleKeepAlive();
            try {
                mService.wake();
            } catch (RemoteException e) {
                LOG.error("remote error", e);
            }
            for (IXoFragment fragment : mTalkFragments) {
                fragment.onServiceConnected();
            }
            if (mBarcodeToken != null) {
                // XXX perform token pairing with callback
                getXoClient().performTokenPairing(mBarcodeToken);
                mBarcodeToken = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOG.debug("onServiceDisconnected()");
            shutdownKeepAlive();
            mService = null;
            for (IXoFragment fragment : mTalkFragments) {
                fragment.onServiceDisconnected();
            }
        }
    }

    /**
     * This class is an implementation of IXoAlertListener which displays alerts inside an AlertDialog.
     * Links and other data inside the message text are tappable.
     */
    public class XoAlertListener implements IXoAlertListener {

        private Context mContext;

        XoAlertListener(Context context) {
            mContext = context;
        }

        @Override
        public void onInternalAlert(String title, String message) {
            final String alertTitle = title;
            final String alertMessage = message;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayAlert(alertTitle, alertMessage);
                }
            });
        }

        @Override
        public void onAlertMessageReceived(String message) {
            final String alertMessage = message;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayAlert(null, alertMessage);
                }
            });
        }

        /**
         * Displays an AlertDialog from a given title and message string.
         * The displayed message text is interactive: links etc. can be tapped.
         *
         * @param title   The given alert title
         * @param message The given alert message
         */
        private void displayAlert(String title, String message) {

            // Scan for urls other information
            final SpannableString interactiveMessage = new SpannableString(message);
            Linkify.addLinks(interactiveMessage, Linkify.ALL);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            if (title != null) {
                builder.setTitle(title);
            }
            builder.setMessage(interactiveMessage);
            builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int index) {
                    dialog.dismiss();
                }
            });

            Dialog dialog = builder.create();
            dialog.show();

            // Make message interactive
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

}
