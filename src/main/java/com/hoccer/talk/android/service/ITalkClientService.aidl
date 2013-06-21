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

    void reconnect();

    /** Provide a callback listener to the service for notifications */
    void setListener(ITalkClientServiceListener listener);

    /** Get the current state of the client */
    int getClientState();

    /** Set name in client presence (works without connection) */
    void setClientName(String newName);

    /** Set status in client presence (works without connection) */
    void setClientStatus(String newStatus);

    /** Notify the server of a newly created message */
    void messageCreated(String messageTag);

    /** Generate a pairing token */
    String generatePairingToken();

    /** Pair using token */
    void pairUsingToken(String token);

    void depairContact(int contactId);

    /** Create new group */
    void createGroup();

    /** Block given contact */
    void blockContact(int contactId);

    /** Unblock given contact */
    void unblockContact(int contactId);

}
