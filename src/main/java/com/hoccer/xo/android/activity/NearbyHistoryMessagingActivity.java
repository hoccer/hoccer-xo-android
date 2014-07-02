package com.hoccer.xo.android.activity;

import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.NearbyChatFragment;
import com.hoccer.xo.release.R;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class NearbyHistoryMessagingActivity extends XoActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_nearby_history_messaging;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fl_fragment_container, new NearbyChatFragment());
        transaction.commit();
    }
}

