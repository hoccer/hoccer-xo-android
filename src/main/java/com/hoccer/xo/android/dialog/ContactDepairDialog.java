package com.hoccer.xo.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.xo.android.XoActivity;
import com.hoccer.xo.release.R;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

public class ContactDepairDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(ContactDepairDialog.class);

    XoActivity mActivity;

    TalkClientContact mContact;

    public ContactDepairDialog(XoActivity activity, TalkClientContact contact) {
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
                        mActivity.getXoService().depairContact(mContact.getClientContactId());
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
