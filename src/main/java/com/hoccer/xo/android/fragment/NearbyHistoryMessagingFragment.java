package com.hoccer.xo.android.fragment;


import com.hoccer.xo.android.adapter.NearbyChatAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.view.OverscrollListView;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NearbyHistoryMessagingFragment extends XoListFragment {

    @Override
    public void onResume() {
        super.onResume();
        setListAdapter(new NearbyChatAdapter(getListView(), getXoActivity()));
    }
}