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
import android.text.InputType;
import android.widget.EditText;

public class NameDialog extends DialogFragment {

    private static final Logger LOG = Logger.getLogger(NameDialog.class);

    XoActivity mActivity;

    TalkClientContact mContact;

    String mOldName;

    EditText mEdit;

    public NameDialog(XoActivity activity, TalkClientContact contact) {
        super();
        mActivity = activity;
        mContact = contact;
        mOldName = mContact.getName();
        if(mOldName == null) {
            mOldName = "";
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEdit = new EditText(mActivity);
        mEdit.setText(mOldName);
        mEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.setname_title);
        builder.setCancelable(true);
        builder.setView(mEdit);
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onClick(ok)");
                String newName = mEdit.getText().toString();
                if(mContact.isSelf()) {
                    if(mContact.isSelfRegistered()) {
                        mActivity.getXoClient().setClientString(newName, null);
                    } else {
                        mContact.getSelf().setRegistrationName(newName);
                    }
                }
                if(mContact.isGroup()) {
                    mActivity.getXoClient().setGroupName(mContact, newName);
                }
                mActivity.hackReturnedFromDialog();
            }
        });
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onClick(cancel)");
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
