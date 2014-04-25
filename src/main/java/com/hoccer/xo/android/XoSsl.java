package com.hoccer.xo.android;

import com.hoccer.talk.client.HttpClientWithKeystore;
import com.hoccer.talk.client.XoClientConfiguration;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * Static SSL configuration
 * <p/>
 * This class takes care of our SSL initialization.
 */
public class XoSsl {

    private static Logger LOG = Logger.getLogger(XoSsl.class);

    private static KeyStore KEYSTORE = null;

    private static WebSocketClientFactory WS_CLIENT_FACTORY = null;

    public static KeyStore getKeyStore() {
        if (KEYSTORE == null) {
            throw new RuntimeException("SSL KeyStore not initialized");
        }
        return KEYSTORE;
    }

    public static WebSocketClientFactory getWebSocketClientFactory() {
        if (WS_CLIENT_FACTORY == null) {
            LOG.info("Creating WebSocketClientFactory");

            ExecutorThreadPool pool = new ExecutorThreadPool(XoApplication.getExecutor());
            WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory(pool);
            SslContextFactory sslContextFactory = webSocketClientFactory.getSslContextFactory();
            sslContextFactory.setTrustAll(false);
            sslContextFactory.setKeyStore(getKeyStore());
            sslContextFactory.setEnableCRLDP(false);
            sslContextFactory.setEnableOCSP(false);
            sslContextFactory.setSessionCachingEnabled(XoClientConfiguration.TLS_SESSION_CACHE_ENABLED);
            sslContextFactory.setSslSessionCacheSize(XoClientConfiguration.TLS_SESSION_CACHE_SIZE);
            sslContextFactory.setIncludeCipherSuites(XoClientConfiguration.TLS_CIPHERS);
            sslContextFactory.setIncludeProtocols(XoClientConfiguration.TLS_PROTOCOLS);
            try {
                webSocketClientFactory.start();
                WS_CLIENT_FACTORY = webSocketClientFactory;
            } catch (Exception e) {
                LOG.error("Could not initialize WebSocketClientFactory: ", e);
            }
        }
        return WS_CLIENT_FACTORY;
    }

    public static void initialize(XoApplication application) {
        // set up SSL
        LOG.info("Initializing ssl KeyStore");
        try {
            // get the KeyStore
            KeyStore ks = KeyStore.getInstance("BKS");
            // load our keys into it
            InputStream in = application.getResources().openRawResource(R.raw.ssl_bks);
            try {
                ks.load(in, "password".toCharArray());
            } finally {
                in.close();
            }
            // configure HttpClient
            HttpClientWithKeystore.initializeSsl(ks);
            // remember the KeyStore
            KEYSTORE = ks;
        } catch (Exception e) {
            LOG.error("Error initializing SSL KeyStore: ", e);
        }
    }

}
