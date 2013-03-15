package com.hoccer.talk.android.service;

import com.hoccer.talk.android.service.ITalkClientListener;

/**
 * This is the main interface of the Hoccer Talk backend service.
 */
interface ITalkClientService {

    /** Tell the service that it is still needed */
    void keepAlive();

    /** Provide a callback listener to the service for notifications */
    void setListener(ITalkClientListener listener);

    /** Notify the server of a newly created message */
    void messageCreated(String messageTag);

}
