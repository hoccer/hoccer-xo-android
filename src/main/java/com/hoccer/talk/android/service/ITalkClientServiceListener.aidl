package com.hoccer.talk.android.service;

/**
 * This interface is exposed by service clients such as UI activities.
 *
 * A binder implementing this interface can be provided to the service
 * using the ITalkClientService.setListener() method.
 *
 */
interface ITalkClientServiceListener {

    void messageCreated(String messageTag);
    void messageDeleted(String messageTag);

    void deliveryCreated(String messageTag, String receiverId);
    void deliveryChanged(String messageTag, String receiverId);
    void deliveryDeleted(String messageTag, String receiverId);

}
