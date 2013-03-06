package com.hoccer.talk.android.service;

import com.hoccer.talk.android.service.ITalkClientListener;

interface ITalkClientService {

    void keepAlive();

    void setListener(ITalkClientListener listener);

    void messageCreated(String messageTag);

}
