package com.hoccer.talk.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.XoActivity;
import com.hoccer.xo.release.R;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

public class NameDialog extends SherlockDialogFragment {

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
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEdit = new EditText(mActivity);
        mEdit.setText(mOldName);
        mEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());

        builder.setTitle(R.string.setname_title);
        builder.setCancelable(true);
        builder.setView(mEdit);
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String newName = mEdit.getText().toString();
                    if(mContact.isSelf()) {
                        mActivity.getService().setClientName(newName);
                    }
                    if(mContact.isGroup()) {
                        mActivity.getService().setGroupName(mContact.getClientContactId(), newName);
                    }
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
                mActivity.hackReturnedFromDialog();
            }
        });
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
