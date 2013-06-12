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
import com.hoccer.talk.android.R;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

public class ProfileFragment extends SherlockFragment {

    private static final Logger LOG = Logger.getLogger(ProfileFragment.class);

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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        SherlockFragmentActivity activity = getSherlockActivity();
        inflater.inflate(R.menu.fragment_profile, menu);
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

    public void showProfile(TalkClientContact contact) {

    }

}
