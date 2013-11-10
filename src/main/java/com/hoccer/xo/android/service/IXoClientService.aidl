package com.hoccer.xo.android.service;

/**
 * This is the main interface of the Hoccer Talk backend service.
 */
interface IXoClientService {

    /** Tell the service that it is still needed */
    void keepAlive();

    /** Instruct the server to connect and retrieve updates */
    void wake();

    void reconnect();

}
