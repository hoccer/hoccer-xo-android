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

    /** Set name in client presence */
    void setClientName(String newName);

    /** Set status in client presence */
    void setClientStatus(String newStatus);

    /** Set new avatar */
    void setClientAvatar(int uploadId);

    /** Set group name */
    void setGroupName(int contactId, String newName);

    /** Set new group avatar */
    void setGroupAvatar(int contactId, int uploadId);


    /** Generate a pairing token */
    String generatePairingToken();
    /** Pair using token */
    void pairUsingToken(String token);
    /** Depair a contact */
    void depairContact(int contactId);
    /** Delete a contact completely */
    void deleteContact(int contactId);

    /** Perform outstanding deliveries */
    void performDeliveries();

    /** Create new group */
    void createGroup();
    void inviteToGroup(int groupContactId, int clientContactId);
    void joinGroup(int contactId);
    void leaveGroup(int contactId);

    /** Block given contact */
    void blockContact(int contactId);
    /** Unblock given contact */
    void unblockContact(int contactId);

    void requestDownload(int clientDownloadId);

    void markAsSeen(int messageId);

}
