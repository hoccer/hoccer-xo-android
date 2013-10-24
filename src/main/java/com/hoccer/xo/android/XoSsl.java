package com.hoccer.xo.android;

import com.hoccer.talk.client.HttpClientWithKeystore;
import com.hoccer.talk.client.XoClientConfiguration;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * Static SSL configuration
 *
 * This class takes care of our SSL initialization.
 */
public class XoSsl {

    private static Logger LOG = Logger.getLogger(XoSsl.class);

    private static KeyStore KEYSTORE = null;

    private static WebSocketClientFactory WS_CLIENT_FACTORY = null;

    public static KeyStore getKeyStore() {
        if(KEYSTORE == null) {
            throw new RuntimeException("SSL security not initialized");
        }
        return KEYSTORE;
    }

    public static WebSocketClientFactory getWebSocketClientFactory() {
        if(WS_CLIENT_FACTORY == null) {
            LOG.info("creating ws client factory");
            WebSocketClientFactory wscFactory = new WebSocketClientFactory();
            SslContextFactory sslcFactory = wscFactory.getSslContextFactory();
            sslcFactory.setTrustAll(false);
            sslcFactory.setKeyStore(getKeyStore());
            sslcFactory.setEnableCRLDP(false);
            sslcFactory.setEnableOCSP(false);
            sslcFactory.setSessionCachingEnabled(XoClientConfiguration.TLS_SESSION_CACHE_ENABLED);
            sslcFactory.setSslSessionCacheSize(XoClientConfiguration.TLS_SESSION_CACHE_SIZE);
            sslcFactory.setIncludeCipherSuites(XoClientConfiguration.TLS_CIPHERS);
            sslcFactory.setIncludeProtocols(XoClientConfiguration.TLS_PROTOCOLS);
            try {
                wscFactory.start();
            } catch (Exception e) {
                LOG.error("could not initialize ws client factory", e);
            }
            WS_CLIENT_FACTORY = wscFactory;
        }
        return WS_CLIENT_FACTORY;
    }

    public static void initialize(XoApplication application) {
        // set up SSL
        LOG.info("initializing ssl keystore");
        try {
            // get the keystore
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
            // remember the keystore
            KEYSTORE = ks;
        } catch (Exception e) {
            LOG.error("error initializing SSL keystore", e);
        }
    }

}
