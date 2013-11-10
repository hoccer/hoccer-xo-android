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

public class GroupInviteDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(GroupInviteDialog.class);

    XoActivity mActivity;

    TalkClientContact mGroup;

    public GroupInviteDialog(XoActivity activity, TalkClientContact group) {
        super();
        mActivity = activity;
        mGroup = group;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ContactsAdapter adapter = new SimpleContactsAdapter(mActivity);
        adapter.setFilter(new ContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
                return contact.isClientRelated() && !contact.isClientGroupJoined(mGroup);
            }
        });
        adapter.reload();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.invite_title);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Object object = adapter.getItem(which);
                if(object != null && object instanceof TalkClientContact) {
                    TalkClientContact contact = (TalkClientContact)object;
                    mActivity.getXoClient().inviteClientToGroup(mGroup.getGroupId(), contact.getClientId());
                }
            }
        });

        return builder.create();
    }

}
