/*
* File: TalkMessage.java
* 
* Type: TalkMessage
* Package: com.hoccer.talk.model
* 
* Project: hoccer-talk-android
* Created by tec on Feb 15, 2013
*
* Copyright (c) 2013 Hoccer Betriebs GmbH. All rights reserved.
*
*/

package com.hoccer.talk.android.model;

public class TalkMessage {

	String mSender;
	String mContent;
	
	public TalkMessage(String pSender, String pContent) {
		mSender = pSender;
		mContent = pContent;
	}
	
	public String getContent() {
		return mContent;
	}
	
	public String getSender() {
		return mSender;
	}
	
}
