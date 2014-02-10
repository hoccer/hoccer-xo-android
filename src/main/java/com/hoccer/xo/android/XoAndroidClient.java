package com.hoccer.xo.android;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.client.IXoClientHost;
import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.client.XoClientConfiguration;

import org.eclipse.jetty.websocket.WebSocketClient;

import java.net.URI;

import better.jsonrpc.websocket.JsonRpcWsClient;

/**
 * Created by jacob on 10.02.14.
 */
public class XoAndroidClient extends XoClient {

    /**
     * Create a Hoccer Talk client using the given client database
     */
    public XoAndroidClient(IXoClientHost host) {
        super(host);
    }

    @Override
    protected void createJsonRpcClient(URI uri, WebSocketClient wsClient, ObjectMapper rpcMapper) {
        String protocol = XoClientConfiguration.USE_BSON_PROTOCOL
                ? XoClientConfiguration.PROTOCOL_STRING_BSON
                : XoClientConfiguration.PROTOCOL_STRING_JSON;
        mConnection = new JsonRpcWsClient(uri, protocol, wsClient, rpcMapper, mHost.getIncomingBackgroundExecutor());
        mConnection.setMaxIdleTime(XoClientConfiguration.CONNECTION_IDLE_TIMEOUT);
        mConnection.setSendKeepAlives(XoClientConfiguration.KEEPALIVE_ENABLED);
        if(XoClientConfiguration.USE_BSON_PROTOCOL) {
            mConnection.setSendBinaryMessages(true);
        }
    }

}
