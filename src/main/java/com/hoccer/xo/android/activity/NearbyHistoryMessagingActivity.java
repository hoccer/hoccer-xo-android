package com.hoccer.xo.android.activity;

import com.hoccer.xo.android.adapter.NearbyChatAdapter;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.fragment.NearbyChatFragment;
import com.hoccer.xo.release.R;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;

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
    protected void onResume() {
        super.onResume();
        ListView listView = (ListView) findViewById(R.id.lv_nearby_history_chat);
        listView.setAdapter(new NearbyChatAdapter(listView, this));
    }
}

