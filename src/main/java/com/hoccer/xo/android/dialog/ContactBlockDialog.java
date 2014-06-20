package com.hoccer.xo.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class ContactBlockDialog extends DialogFragment {

    private static final Logger LOG = Logger.getLogger(ContactBlockDialog.class);

    XoActivity mActivity;

    TalkClientContact mContact;

    public ContactBlockDialog(XoActivity activity, TalkClientContact contact) {
        super();
        mActivity = activity;
        mContact = contact;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LOG.debug("onCreateDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mContact != null) {
                    mActivity.getXoClient().blockContact(mContact);
                }
            }
        });
        builder.setTitle(R.string.dialog_block_user_title);
        builder.setMessage(R.string.dialog_block_user_message);
        return builder.create();
    }
}
