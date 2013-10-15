package com.hoccer.xo.android.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.HoccerTalkClient;
import com.hoccer.talk.client.ITalkStateListener;
import com.hoccer.xo.android.XoFragment;
import com.hoccer.xo.release.R;

/**
 * Mix-in fragment for showing client status
 */
public class StatusFragment extends XoFragment implements ITalkStateListener {

    TextView mStatusText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG.debug("onCreateView()");

        View v = inflater.inflate(R.layout.fragment_status, container, false);

        mStatusText = (TextView)v.findViewById(R.id.status_text);

        return v;
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        getXoClient().registerStateListener(this);
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
        getXoClient().unregisterStateListener(this);
    }

    @Override
    public void onServiceConnected() {
        LOG.debug("onServiceConnected()");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int state = getXoClient().getState();
                applyClientState(state);
            }
        });
    }

    @Override
    public void onServiceDisconnected() {
        LOG.debug("onServiceDisconnected()");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Internal error");
            }
        });
    }

    @Override
    public void onClientStateChange(HoccerTalkClient client, final int state) {
        String stateString = HoccerTalkClient.stateToString(state);
        LOG.debug("onClientStateChanged(" + stateString + ")");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyClientState(state);
            }
        });
    }

    private void applyClientState(int state) {
        String statusText;
        Resources r = getResources();
        switch(state) {
            case HoccerTalkClient.STATE_IDLE:
                statusText = r.getString(R.string.client_state_idle);
                break;
            case HoccerTalkClient.STATE_INACTIVE:
                statusText = r.getString(R.string.client_state_inactive);
                break;
            case HoccerTalkClient.STATE_ACTIVE:
                statusText = r.getString(R.string.client_state_active);
                break;
            case HoccerTalkClient.STATE_CONNECTING:
                statusText = r.getString(R.string.client_state_connecting);
                break;
            case HoccerTalkClient.STATE_LOGIN:
                statusText = r.getString(R.string.client_state_login);
                break;
            case HoccerTalkClient.STATE_RECONNECTING:
                statusText = r.getString(R.string.client_state_reconnecting);
                break;
            case HoccerTalkClient.STATE_REGISTERING:
                statusText = r.getString(R.string.client_state_registering);
                break;
            case HoccerTalkClient.STATE_SYNCING:
                statusText = r.getString(R.string.client_state_syncing);
                break;
            default:
                statusText = r.getString(R.string.client_state_unknown);
                break;
        }
        mStatusText.setText(statusText);

        final FragmentTransaction tr =
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();
        if (state == HoccerTalkClient.STATE_ACTIVE
                || state == HoccerTalkClient.STATE_IDLE) {
            tr.hide(StatusFragment.this);
        } else {
            tr.show(StatusFragment.this);
        }
        tr.commitAllowingStateLoss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        /* This is a workaround for an AOSP bug with the given number,
         * related to saving state for this kind of fragment.
         * Don't ask me for the details, its magic.
         */
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

}
