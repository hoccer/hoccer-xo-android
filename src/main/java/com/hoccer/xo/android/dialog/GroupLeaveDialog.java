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

public class GroupLeaveDialog extends DialogFragment {

    private static final Logger LOG = Logger.getLogger(GroupLeaveDialog.class);

    XoActivity mActivity;

    TalkClientContact mGroup;

    public GroupLeaveDialog(XoActivity activity, TalkClientContact group) {
        super();
        mActivity = activity;
        mGroup = group;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LOG.debug("onCreateDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.leave_title);
        builder.setMessage(R.string.leave_question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onClick(no)");
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onClick(yes)");
                mActivity.getXoClient().leaveGroup(mGroup.getGroupId());
            }
        });

        return builder.create();
    }

}
