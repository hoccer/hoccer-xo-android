package com.hoccer.talk.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PairingFragment extends TalkFragment {

    TextView mTokenText;

    EditText mTokenEdit;
    Button mTokenPair;

    ScheduledFuture<?> mTokenFuture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.info("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.info("onCreateView()");
        View view = inflater.inflate(R.layout.fragment_pairing, container, false);

        mTokenText = (TextView)view.findViewById(R.id.pairing_token_text);

        mTokenEdit = (EditText)view.findViewById(R.id.pairing_token_edit);

        mTokenPair = (Button)view.findViewById(R.id.pairing_token_pair);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        inflater.inflate(R.menu.fragment_pairing, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        LOG.info("onResume()");
        mTokenFuture = TalkApplication.getExecutor().scheduleAtFixedRate(
                new Runnable() {
                    public void run() {
                        LOG.info("requesting new pairing token");
                        try {
                            final String token = getTalkActivity().getService().generatePairingToken();
                            LOG.info("got token");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LOG.info("setting token text");
                                    mTokenText.setText(token);
                                }
                            });
                        } catch (RemoteException e) {
                            LOG.info("generation failed");
                            e.printStackTrace();
                        }
                    }
                }, 1, 120, TimeUnit.SECONDS
        );
    }

    @Override
    public void onPause() {
        LOG.info("onPause()");
        if(mTokenFuture != null) {
            mTokenFuture.cancel(true);
        }
        super.onPause();
    }

}
