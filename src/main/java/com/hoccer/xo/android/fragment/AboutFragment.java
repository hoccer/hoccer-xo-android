/*
 * AboutFragment.java
 * HoccerXO Chat
 *
 * Created by Bjoern Heller 5/16/13 1:16 AM
 * Copyright (c) 2013. Hoccer Betriebs GmbH
 */

package com.hoccer.xo.android.fragment;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * Fragment that shows the "about" web page
 */
public class AboutFragment extends Fragment {


    private static final Logger LOG = Logger.getLogger(AboutFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            TextView appNameView = (TextView) view.findViewById(R.id.tv_about_app_name);
            appNameView.setText(R.string.app_name);

            TextView appVersionView = (TextView) view.findViewById(R.id.tv_about_app_version);
            String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0)
                    .versionName;
            appVersionView.setText(String.format(getString(R.string.about_version_description), versionName));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
