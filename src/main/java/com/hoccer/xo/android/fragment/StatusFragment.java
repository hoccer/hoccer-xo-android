package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.IXoStateListener;
import com.hoccer.talk.client.XoClient;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.release.R;

/**
 * Mix-in fragment for showing client status
 */
public class StatusFragment extends XoFragment implements IXoStateListener {

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
    public void onClientStateChange(XoClient client, final int state) {
        String stateString = XoClient.stateToString(state);
        LOG.debug("onClientStateChanged(" + stateString + ")");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyClientState(state);
            }
        });
    }

    private void applyClientState(int state) {
        int statusText;
        switch(state) {
            case XoClient.STATE_IDLE:
                statusText = R.string.client_state_idle;
                break;
            case XoClient.STATE_INACTIVE:
                statusText = R.string.client_state_inactive;
                break;
            case XoClient.STATE_ACTIVE:
                statusText = R.string.client_state_active;
                break;
            case XoClient.STATE_CONNECTING:
                statusText = R.string.client_state_connecting;
                break;
            case XoClient.STATE_LOGIN:
                statusText = R.string.client_state_login;
                break;
            case XoClient.STATE_RECONNECTING:
                statusText = R.string.client_state_reconnecting;
                break;
            case XoClient.STATE_REGISTERING:
                statusText = R.string.client_state_registering;
                break;
            case XoClient.STATE_SYNCING:
                statusText = R.string.client_state_syncing;
                break;
            default:
                statusText = R.string.client_state_unknown;
                break;
        }
        mStatusText.setText(statusText);

        final FragmentTransaction tr = getActivity().getSupportFragmentManager().beginTransaction();
        if (state == XoClient.STATE_ACTIVE
                || state == XoClient.STATE_IDLE) {
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
