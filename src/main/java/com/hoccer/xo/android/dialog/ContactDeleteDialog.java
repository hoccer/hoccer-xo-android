package com.hoccer.xo.android.dialog;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ContactDeleteDialog extends DialogFragment {

    private static final Logger LOG = Logger.getLogger(ContactDeleteDialog.class);

    XoActivity mActivity;

    TalkClientContact mContact;

    public ContactDeleteDialog(XoActivity activity, TalkClientContact contact) {
        super();
        mActivity = activity;
        mContact = contact;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LOG.debug("onCreateDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.deletecontact_title);
        builder.setMessage(R.string.deletecontact_question);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onClick(yes)");
                if(mContact != null) {
                    mActivity.getXoClient().deleteContact(mContact);
                }
                mActivity.hackReturnedFromDialog();
            }
        });
        builder.setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onClick(no)");
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
