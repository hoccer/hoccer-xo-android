package com.hoccer.talk.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

    TextWatcher mTextWatcher;

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
        mTokenEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

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
        // request a new token every now and then and show it
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
        // bind the listener for the button that starts pairing
        mTokenPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPairing(mTokenEdit.getText().toString());
            }
        });

        // reset the token editor
        mTokenEdit.setText("");
        mTokenPair.setEnabled(false);
        // perform pairing on "done" action
        mTokenEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    performPairing(mTokenEdit.getText().toString());
                }
                return false;
            }
        });
        // set up a simple text watcher on the editor
        // so it can disable the pairing button if empty
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String token = s.toString();
                if(token.length() > 0) {
                    mTokenPair.setEnabled(true);
                } else {
                    mTokenPair.setEnabled(false);
                }
            }
        };
        mTokenEdit.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onPause() {
        LOG.info("onPause()");
        // remove the text watcher
        if(mTextWatcher != null) {
            mTokenEdit.removeTextChangedListener(mTextWatcher);
            mTextWatcher = null;
        }
        // cancel token requests
        if(mTokenFuture != null) {
            mTokenFuture.cancel(true);
        }
        super.onPause();
    }

    public void initializeWithReceivedToken(String token) {
        LOG.info("initializeWithReceivedToken(" + token + ")");
        mTokenEdit.setText(token);
    }

    private void performPairing(String token) {
        LOG.info("performPairing(" + token + ")");
        try {
            getTalkActivity().getService().pairUsingToken(token);
        } catch (RemoteException e) {
            LOG.info("pairing failed");
            e.printStackTrace();
        }
    }

}
