package com.hoccer.talk.android.service;

import com.hoccer.talk.android.service.ITalkClientServiceListener;

/**
 * This is the main interface of the Hoccer Talk backend service.
 */
interface ITalkClientService {

    /** Tell the service that it is still needed */
    void keepAlive();

    /** Instruct the server to connect and retrieve updates */
    void wake();

    /** Provide a callback listener to the service for notifications */
    void setListener(ITalkClientServiceListener listener);

    /** Notify the server of a newly created message */
    void messageCreated(String messageTag);

    /** Generate a pairing token */
    String generatePairingToken();

}
