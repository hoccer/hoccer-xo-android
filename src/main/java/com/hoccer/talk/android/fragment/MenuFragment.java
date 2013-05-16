/*
 * MenuFragment.java
 * HoccerXO Chat
 *
 * Created by Björn Heller 5/16/13 1:50 AM
 * Copyright (c) 2013. Hoccer Betriebs GmbH
 */

package com.hoccer.talk.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.ITalkActivity;
import com.hoccer.talk.android.R;
import com.hoccer.talk.logging.HoccerLoggers;

import java.util.logging.Logger;

public class MenuFragment extends SherlockFragment {

    private static final Logger LOG =
            HoccerLoggers.getLogger(MenuFragment.class);

    ITalkActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.info("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        LOG.info("onAttach()");
        super.onAttach(activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.info("onCreateView()");
        return inflater.inflate(R.layout.fragment_menu, container, false);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        SherlockFragmentActivity activity = getSherlockActivity();
        inflater.inflate(R.menu.fragment_menu, menu);
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
