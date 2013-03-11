package com.hoccer.talk.android.service;

import com.hoccer.talk.android.service.ITalkClientListener;

/**
 * This is the main interface of the Hoccer Talk backend service.
 */
interface ITalkClientService {

    /**
     *
     */
    void keepAlive();

    /**
     *
     */
    void setListener(ITalkClientListener listener);

    void messageCreated(String messageTag);

}
