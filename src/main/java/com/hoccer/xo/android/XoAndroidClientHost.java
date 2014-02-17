package com.hoccer.xo.android;

import com.hoccer.talk.client.IXoClientDatabaseBackend;
import com.hoccer.talk.client.IXoClientHost;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientConfiguration;
import com.hoccer.xo.android.database.AndroidTalkDatabase;

import org.eclipse.jetty.websocket.WebSocketClientFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Android-specific implementation of an XO client host
 *
 * This directs the client towards the android-specific ormlite backend,
 * binds it to the right WS socket factory for SSL security
 * and allows the client to read files from content providers.
 */
public class XoAndroidClientHost implements IXoClientHost {

    Context mContext;

    public XoAndroidClientHost(Context context) {
        mContext = context;
    }

    @Override
    public ScheduledExecutorService getBackgroundExecutor() {
        return XoApplication.getExecutor();
    }

    @Override
    public ScheduledExecutorService getIncomingBackgroundExecutor() {
        return XoApplication.getIncomingExecutor();
    }

    @Override
    public IXoClientDatabaseBackend getDatabaseBackend() {
        return AndroidTalkDatabase.getInstance(mContext);
    }

    @Override
    public WebSocketClientFactory getWebSocketFactory() {
        return XoSsl.getWebSocketClientFactory();
    }

    @Override
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return XoApplication.getUncaughtExceptionHandler();
    }

    @Override
    public InputStream openInputStreamForUrl(String url) throws IOException {
        return mContext.getContentResolver().openInputStream(Uri.parse(url));
    }

    @Override
    public String getServerUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String serverUri = preferences.getString("preference_server_uri", null);
        if (serverUri == null || serverUri.equalsIgnoreCase("") || !serverUri
                .startsWith("wss://")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("preference_server_uri", XoClientConfiguration.SERVER_URI);
            editor.commit();
            serverUri = XoClientConfiguration.SERVER_URI;
        }

        return serverUri;
    }

}
