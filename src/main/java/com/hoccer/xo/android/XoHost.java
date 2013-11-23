package com.hoccer.xo.android;

import android.content.Context;
import android.net.Uri;
import com.hoccer.talk.client.IXoClientDatabaseBackend;
import com.hoccer.talk.client.IXoClientHost;
import com.hoccer.xo.android.database.AndroidTalkDatabase;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

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
public class XoHost implements IXoClientHost {

    Context mContext;

    public XoHost(Context context) {
        mContext = context;
    }

    @Override
    public ScheduledExecutorService getBackgroundExecutor() {
        return XoApplication.getExecutor();
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
    public InputStream openInputStreamForUrl(String url) throws IOException {
        return mContext.getContentResolver().openInputStream(Uri.parse(url));
    }

}