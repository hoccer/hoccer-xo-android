package com.hoccer.talk.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import org.apache.log4j.Logger;

public class NameDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(NameDialog.class);

    TalkActivity mActivity;

    String mOldName;

    EditText mEdit;

    public NameDialog(TalkActivity activity, String oldName) {
        super();
        mActivity = activity;
        mOldName = oldName;
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
                    mActivity.getService().setClientName(mEdit.getText().toString());
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
