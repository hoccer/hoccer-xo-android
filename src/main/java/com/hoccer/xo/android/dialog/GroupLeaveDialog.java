package com.hoccer.xo.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.SimpleContactsAdapter;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class GroupLeaveDialog extends SherlockDialogFragment {

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
        ContactsAdapter adapter = new SimpleContactsAdapter(mActivity);
        adapter.setFilter(new ContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
                return contact.isClientRelated();
            }
        });
        adapter.reload();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.leave_title);
        builder.setMessage(R.string.leave_question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.getXoClient().leaveGroup(mGroup.getGroupId());
            }
        });

        return builder.create();
    }

}
