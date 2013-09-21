/*
 * AboutFragment.java
 * HoccerXO Chat
 *
 * Created by Björn Heller 5/16/13 1:16 AM
 * Copyright (c) 2013. Hoccer Betriebs GmbH
 */

package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.hoccer.xo.release.R;
import com.hoccer.talk.android.TalkFragment;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Fragment that shows the "about" web page
 *
 * This uses webapp caching to cache the page.
 */
public class AboutFragment extends TalkFragment {

    private static final String ABOUT_URL = "http://www.hoccer.com/xo-about-view";

    private static final Logger LOG = Logger.getLogger(AboutFragment.class);

    private WebView mAboutWebView;

    public AboutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.debug("onCreateView()");

        // inflate the layout
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        // get the web view
        mAboutWebView = (WebView)v.findViewById(R.id.about_webview);
        // open all links in its own window
        mAboutWebView.setWebViewClient(new WebViewClient());
        mAboutWebView.setWebChromeClient(new WebChromeClient());

        // get settings for web view
        WebSettings webSettings = mAboutWebView.getSettings();
        // enable Javascript inside web view
        webSettings.setJavaScriptEnabled(true);
        // always zoom out on load
        webSettings.setLoadWithOverviewMode(true);
        // set viewport to its own dimensions
        webSettings.setUseWideViewPort(true);
        // cache configuration in android web view
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        webSettings.setAppCachePath(getActivity().getFilesDir().toString() + File.separator + "webapp-cache");
        webSettings.setAppCacheEnabled(true);
        // set cache mode
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        return v;
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        // load our target URL
        mAboutWebView.loadUrl(ABOUT_URL);
    }

}
