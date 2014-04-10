package com.hoccer.xo.android.fragment;

import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.error.EnvironmentUpdaterException;
import com.hoccer.xo.android.nearby.EnvironmentUpdater;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Created by jacob on 10.04.14.
 */
public class NearbyContactsFragment extends XoListFragment {

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
        ArrayAdapter adapter = new ArrayAdapter(getXoActivity(), android.R.layout.simple_list_item_1, new String[] {"test1","test2","test3","test4","test5","test6","test7"});
        setListAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEnvironmentUpdater.stopEnvironmentTracking();
    }
}
