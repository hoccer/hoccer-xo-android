package com.hoccer.talk.android.service;

/**
 * This interface is exposed by service clients such as UI activities.
 *
 * A binder implementing this interface can be provided to the service
 * using the ITalkClientService.setListener() method.
 *
 */
interface ITalkClientServiceListener {

    void onClientStateChanged(int state);

    void onTokenPairingFailed(String token);
    void onTokenPairingSucceeded(String token);

    void onContactAdded(int contactId);
    void onContactRemoved(int contactId);

    void onClientPresenceChanged(int contactId);
    void onClientRelationshipChanged(int contactId);

    void onGroupCreationSucceeded(int contactId);
    void onGroupCreationFailed();

    void onGroupPresenceChanged(int contactId);
    void onGroupMembershipChanged(int contactId);

    void onMessageAdded(int messageId);
    void onMessageRemoved(int messageId);
    void onMessageStateChanged(int messageId);

}
