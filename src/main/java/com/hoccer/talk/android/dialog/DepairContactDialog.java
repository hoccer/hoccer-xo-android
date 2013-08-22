package com.hoccer.talk.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

public class DepairContactDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(DepairContactDialog.class);

    TalkActivity mActivity;

    TalkClientContact mContact;

    public DepairContactDialog(TalkActivity activity, TalkClientContact contact) {
        super();
        mActivity = activity;
        mContact = contact;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());

        builder.setTitle(R.string.depaircontact_title);
        builder.setMessage(R.string.depaircontact_question);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mContact != null) {
                    try {
                        mActivity.getTalkClientService().depairContact(mContact.getClientContactId());
                    } catch (RemoteException e) {
                        LOG.error("remote error", e);
                    }
                }
                mActivity.hackReturnedFromDialog();
            }
        });
        builder.setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}