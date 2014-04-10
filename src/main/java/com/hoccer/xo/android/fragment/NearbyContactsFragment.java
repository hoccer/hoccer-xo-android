package com.hoccer.xo.android.fragment;

import com.hoccer.xo.android.error.EnvironmentUpdaterException;
import com.hoccer.xo.android.nearby.EnvironmentUpdater;

import org.apache.log4j.Logger;

import android.app.ListFragment;
import android.os.Bundle;

/**
 * Created by jacob on 10.04.14.
 */
public class NearbyContactsFragment extends ListFragment {

    private static final Logger LOG = Logger.getLogger(NearbyContactsFragment.class);

    private EnvironmentUpdater mEnvironmentUpdater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEnvironmentUpdater = new EnvironmentUpdater(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mEnvironmentUpdater.startEnvironmentTracking();
        } catch (EnvironmentUpdaterException e) {
            // TODO: notify the user that we dont see any environment currently
            LOG.error("no environment information available", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mEnvironmentUpdater.stopEnvironmentTracking();
    }
}
