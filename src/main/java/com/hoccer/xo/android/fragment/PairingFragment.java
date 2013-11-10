package com.hoccer.xo.android.fragment;

import android.os.Bundle;
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
import android.widget.Toast;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.release.R;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for pairing
 *
 * This offers various ways of finding new friends.
 */
public class PairingFragment extends XoFragment implements View.OnClickListener {

    TextView mTokenMessage;
    TextView mTokenText;
    Button mTokenSendSms;

    EditText mTokenEdit;
    Button mTokenPairButton;

    Button mQrShowButton;
    Button mQrScanButton;

    ScheduledFuture<?> mTokenFuture;

    TextWatcher mTextWatcher;

    String mActiveToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.debug("onCreateView()");

        View view = inflater.inflate(R.layout.fragment_pairing, container, false);

        mTokenMessage = (TextView)view.findViewById(R.id.pairing_token_message);
        mTokenText = (TextView)view.findViewById(R.id.pairing_token_text);
        mTokenText.setVisibility(View.GONE);
        mTokenSendSms = (Button)view.findViewById(R.id.pairing_token_sms);
        mTokenSendSms.setEnabled(false);
        mTokenSendSms.setOnClickListener(this);

        mTokenEdit = (EditText)view.findViewById(R.id.pairing_token_edit);
        mTokenEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        mTokenPairButton = (Button)view.findViewById(R.id.pairing_token_pair);

        mQrScanButton = (Button)view.findViewById(R.id.pairing_scan_qr);
        mQrScanButton.setOnClickListener(this);

        mQrShowButton = (Button)view.findViewById(R.id.pairing_show_qr);
        mQrShowButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        requestNewToken();

        // bind the listener for the button that starts pairing
        mTokenPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPairing(mTokenEdit.getText().toString());
            }
        });

        // reset the token editor
        mTokenEdit.setText("");
        mTokenPairButton.setEnabled(false);
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
                    mTokenPairButton.setEnabled(true);
                } else {
                    mTokenPairButton.setEnabled(false);
                }
            }
        };
        mTokenEdit.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
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

    @Override
    public void onClick(View v) {
        if(v == mQrShowButton) {
            LOG.debug("onClick(qrShow)");
            getXoActivity().showBarcode();
        }
        if(v == mQrScanButton) {
            LOG.debug("onClick(qrScan)");
            getXoActivity().scanBarcode();
        }
        if(v == mTokenSendSms) {
            LOG.debug("onClick(smsSend)");
            getXoActivity().composeInviteSms(mTokenText.getText().toString());
        }
    }

    public void requestNewToken() {
        LOG.debug("requesting new pairing token");
        mTokenText.setVisibility(View.GONE);
        mTokenMessage.setVisibility(View.VISIBLE);
        mTokenSendSms.setEnabled(false);
        // request a new token and show it
        XoApplication.getExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                final String token = getXoClient().generatePairingToken();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTokenText.setText(token);
                        mTokenText.setVisibility(View.VISIBLE);
                        mTokenMessage.setVisibility(View.GONE);
                        mTokenSendSms.setEnabled(true);
                    }
                });
            }
        }, 1, TimeUnit.SECONDS);
    }

    public void initializeWithReceivedToken(String token) {
        LOG.debug("initializeWithReceivedToken(" + token + ")");
        mTokenEdit.setText(token);
    }

    private void performPairing(String token) {
        LOG.debug("performPairing(" + token + ")");
        mActiveToken = token;
        mTokenEdit.setEnabled(false);
        mTokenPairButton.setEnabled(false);
        // XXX callback
        getXoClient().performTokenPairing(token);
    }

    // XXX @Override
    public void onTokenPairingFailed(String token) {
        LOG.debug("onTokenPairingFailed()");
        if(token.equals(mActiveToken)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getXoActivity(), "Pairing failed", Toast.LENGTH_SHORT).show();
                    mTokenEdit.setEnabled(true);
                    mTokenEdit.setText("");
                    mTokenPairButton.setEnabled(false);
                }
            });
        }
    }

    // XXX @Override
    public void onTokenPairingSucceeded(String token) {
        LOG.debug("onTokenPairingSucceeded()");
        if(token.equals(mActiveToken)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getXoActivity(), "Pairing succeeded", Toast.LENGTH_SHORT).show();
                    mTokenEdit.setEnabled(true);
                    mTokenEdit.setText("");
                    mTokenPairButton.setEnabled(false);
                }
            });
        }
    }

}
