/*
 * AboutFragment.java
 * HoccerXO Chat
 *
 * Created by Björn Heller 5/16/13 1:16 AM
 * Copyright (c) 2013. Hoccer Betriebs GmbH
 */

package com.hoccer.talk.android.fragment;

import java.util.logging.Logger;

import android.webkit.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.ITalkActivity;
import com.hoccer.talk.logging.HoccerLoggers;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends SherlockFragment{

    private static final Logger LOG =
            HoccerLoggers.getLogger(AboutFragment.class);

    ITalkActivity mActivity;

    private WebView aboutWebView;
    private String LOG_TAG = "aboutWebView";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.info("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.info("onCreateView()");

       // setContentView(R.layout.fragmentAbout);
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        //enable Javascript inside WebView
        aboutWebView.getSettings().setJavaScriptEnabled(true);
        //load WebView zoomed out
        aboutWebView.getSettings().setLoadWithOverviewMode(true);
        //set viewport to its own dimensions
        aboutWebView.getSettings().setUseWideViewPort(true);
        //open all links in its own window
        aboutWebView.setWebViewClient(new WebViewClient());
        aboutWebView.setWebChromeClient(new WebChromeClient());
        //cache configuration in android webview
        aboutWebView.getSettings().setAppCacheMaxSize(1024*1024*8);
        aboutWebView.getSettings().setAppCachePath("/data/com.hoccer.talk.android/cache");
        aboutWebView.getSettings().setAppCacheEnabled(true);
        aboutWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        //Load this URL in aboutWebView
        aboutWebView.loadUrl("http://www.hoccer.com/xo-about-view");
        return v;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        SherlockFragmentActivity activity = getSherlockActivity();
        inflater.inflate(R.menu.fragment_about, menu);
    }

    @Override
    public void onResume() {
        LOG.info("onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        LOG.info("onPause()");
        super.onPause();
    }
}

