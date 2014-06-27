/*
 * AboutFragment.java
 * HoccerXO Chat
 *
 * Created by Bjoern Heller 5/16/13 1:16 AM
 * Copyright (c) 2013. Hoccer Betriebs GmbH
 */

package com.hoccer.xo.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * Fragment that shows the "about" web page
 *
 */
public class AboutFragment extends Fragment {


    private static final Logger LOG = Logger.getLogger(AboutFragment.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
