package com.hoccer.talk.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import org.apache.log4j.Logger;

public class TokenDialog extends SherlockDialogFragment implements DialogInterface.OnClickListener {

    private static final Logger LOG = Logger.getLogger(TokenDialog.class);

    TalkActivity mActivity;

    TalkClientSmsToken mToken;

    public TokenDialog (TalkActivity activity, TalkClientSmsToken token) {
        super();
        mActivity = activity;
        mToken = token;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());

        builder.setTitle("Invitation");
        builder.setCancelable(true);
        builder.setNegativeButton("Decline", this);
        builder.setPositiveButton("Accept", this);
        builder.setNeutralButton("Cancel", this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE) {
            if(mToken != null) {
                try {
                    mActivity.getService().useSmsToken(mToken.getSmsTokenId());
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
                mActivity.hackReturnedFromDialog();
            }
        }
        if(which == DialogInterface.BUTTON_NEGATIVE) {
            if(mToken != null) {
                try {
                    mActivity.getService().rejectSmsToken(mToken.getSmsTokenId());
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
                mActivity.hackReturnedFromDialog();
            }
        }
        if(which == DialogInterface.BUTTON_NEUTRAL) {
            dialog.dismiss();
            mActivity.hackReturnedFromDialog();
        }
    }

}
