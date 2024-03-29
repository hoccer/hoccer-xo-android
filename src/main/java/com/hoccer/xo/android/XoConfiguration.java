package com.hoccer.xo.android;

import com.hoccer.talk.client.XoClient;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;

import java.sql.SQLException;
import java.util.Locale;

/**
 * Static client configuration
 *
 * This class collects various android-specific settings for the XO client.
 */
public class XoConfiguration {
	
    /* HockeyApp constant id  */
    public static final String HOCKEYAPP_ID = "60f2a55705e94d33e62a7b1643671f46";

    /* Directories in external storage */
    public static final String EXTERNAL_ATTACHMENTS = "Hoccer XO";

    /* Directories in internal storage */
    public static final String INTERNAL_AVATARS = "avatars";
    public static final String INTERNAL_DOWNLOADS = "downloads";
    public static final String INTERNAL_UPLOADS = "uploads";
    public static final String INTERNAL_GENERATED = "generated";

    /* Enable or disable development settings in preferences */
    public static final boolean DEVELOPMENT_MODE_ENABLED = false;

    /**
     * Background executor thread count
     *
     * AFAIK this must be at least 3 for RPC to work.
     */
    public static final int CLIENT_THREADS = 4;

    /** Notification alarm back-off (msecs) */
    public static final long NOTIFICATION_ALARM_BACKOFF = 5000;
    /** Notification cancellation back-off (msecs) */
    public static final long NOTIFICATION_CANCEL_BACKOFF = 2000;

    /** If true, GCM registration will be performed forcibly on every connect */
    public static final boolean GCM_ALWAYS_REGISTER = false;
    /** If true, GCM registration should always be pushed to server */
    public static final boolean GCM_ALWAYS_UPDATE = true;
    /** GCM sender id for push notifications */
    public static final String GCM_SENDER_ID = "1894273085";
    /** GCM server registration expiration (seconds) */
    public static final long GCM_REGISTRATION_EXPIRATION = 24 * 3600;

    /** Log tag to use in logcat */
    public static final String LOG_LOGCAT_TAG = "HoccerXO";
    /** The layout for android logcat */
    public static final Layout LOG_LOGCAT_LAYOUT = new PatternLayout("[%t] %-5p %c - %m%n");
    /** Base name of log files */
    public static final String LOG_FILE_NAME = "hoccer-xo.log";
    /** The maximum number of log files to keep */
    public static final int LOG_FILE_COUNT = 10;
    /** The maximum size of each log file */
    public static final int LOG_FILE_SIZE = 1024 * 1024;
    /** The layout for log files */
    public static final Layout LOG_FILE_LAYOUT = new PatternLayout("[%t] %-5p %c - %m%n");

    /** Whether to reconnect explicitly on connection changes */
    public static final boolean CONNECTIVITY_RECONNECT_ON_CHANGE = true;

    /** Delay after which new activities send their first keepalive (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_DELAY    = 60;
    /** Interval at which activities send keepalives to the client service (seconds) */
    public static final int SERVICE_KEEPALIVE_PING_INTERVAL = 600;
    /** Timeout after which the client service terminates automatically (seconds) */
    public static final int SERVICE_KEEPALIVE_TIMEOUT       = 1800;

    /** The tag describing server-side support mode */
    public static final String SERVER_SUPPORT_TAG = "log";

    private static boolean sIsSupportModeEnabled = false;

    private static SharedPreferences sPreferences;
    private static SharedPreferences.OnSharedPreferenceChangeListener sPreferencesListener;

    public static void initialize(XoApplication application) {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("preference_enable_server_side_support_mode")) {
                    sIsSupportModeEnabled = sPreferences.getBoolean("preference_enable_server_side_support_mode", false);
                    XoApplication.getXoClient().scheduleHello();
                } else if(key.equals("preference_server_uri")) {
                    XoApplication.reinitializeXoClient();
                }
            }
        };
        sPreferences.registerOnSharedPreferenceChangeListener(sPreferencesListener);
        sIsSupportModeEnabled = sPreferences.getBoolean("preference_enable_server_side_support_mode", false);
        if (!sPreferences.contains("preference_keysize")) {
            SharedPreferences.Editor editor = sPreferences.edit();
            editor.putString("preference_keysize", "2048");
            editor.commit();
        }
    }

    public static final void shutdown() {
        if(sPreferencesListener != null) {
            sPreferences.unregisterOnSharedPreferenceChangeListener(sPreferencesListener);
            sPreferencesListener = null;
        }
    }

    public static boolean needToRegenerateKey() {
        return sPreferences.getBoolean("NEED_TO_REGENERATE_KEYS", true);
    }

    public static void setRegenerationDone() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean("NEED_TO_REGENERATE_KEYS", false);
        editor.commit();
    }

    public static boolean isSupportModeEnabled() {
        return sIsSupportModeEnabled;
    }

    public static boolean reportingEnable() {
        return DEVELOPMENT_MODE_ENABLED || sPreferences.getBoolean("preference_crash_report", false);
    }
}
