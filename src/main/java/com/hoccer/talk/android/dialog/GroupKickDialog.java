package com.hoccer.talk.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.adapter.ContactsAdapter;
import com.hoccer.talk.android.adapter.SimpleContactsAdapter;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

public class GroupKickDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(GroupInviteDialog.class);

    TalkActivity mActivity;

    TalkClientContact mGroup;

    public GroupKickDialog(TalkActivity activity, TalkClientContact group) {
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
                return contact.isClientGroupJoined(mGroup);
            }
        });
        adapter.reload();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.kick_title);
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
                    try {
                        mActivity.getTalkClientService()
                                .kickFromGroup(mGroup.getClientContactId(),
                                        contact.getClientContactId());
                    } catch (RemoteException e) {
                        LOG.error("remote error", e);
                    }
                }
            }
        });

        return builder.create();
    }

}
