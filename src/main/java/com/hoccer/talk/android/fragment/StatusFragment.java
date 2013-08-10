package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.client.HoccerTalkClient;

/**
 * Mix-in fragment for showing client status
 */
public class StatusFragment extends TalkFragment {

    TextView mStatusText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG.debug("onCreateView()");

        View v = inflater.inflate(R.layout.fragment_status, container, false);

        mStatusText = (TextView)v.findViewById(R.id.status_text);

        return v;
    }

    @Override
    public void onServiceConnected() {
        LOG.debug("onServiceConnected()");

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
        LOG.debug("onServiceDisconnected()");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Backend disconnected");
            }
        });
    }

    @Override
    public void onClientStateChanged(int state) {
        final String stateString = HoccerTalkClient.stateToString(state);
        LOG.debug("onClientStateChanged(" + stateString + ")");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText(stateString);
            }
        });
    }

}
