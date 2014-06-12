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

public class GroupRejectInvitationDialog extends DialogFragment {

    private static final Logger LOG = Logger.getLogger(GroupRejectInvitationDialog.class);

    XoActivity mActivity;

    TalkClientContact mGroup;

    public GroupRejectInvitationDialog(XoActivity activity, TalkClientContact group) {
        super();
        mActivity = activity;
        mGroup = group;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LOG.debug("onCreateDialog()");

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.reject_invitation_title);
        builder.setMessage(R.string.reject_invitation_question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Cancel)");
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Ok)");
                mActivity.getXoClient().leaveGroup(mGroup.getGroupId());
                mActivity.finish();
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
