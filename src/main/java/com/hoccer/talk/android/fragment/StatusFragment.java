package com.hoccer.talk.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.client.HoccerTalkClient;

public class StatusFragment extends TalkFragment {

    TextView mStatusText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG.info("onCreateView()");

        View v = inflater.inflate(R.layout.fragment_status, container, false);

        mStatusText = (TextView)v.findViewById(R.id.status_text);

        return v;
    }

    @Override
    public void onServiceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int state = getTalkActivity().getClientState();
                mStatusText.setText(HoccerTalkClient.stateToString(state));
            }
        });
    }

    @Override
    public void onServiceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Backend disconnected");
            }
        });
    }

    @Override
    public void onClientStateChanged(final int state) throws RemoteException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText(HoccerTalkClient.stateToString(state));
            }
        });
    }

}
