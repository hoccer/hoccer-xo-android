package com.hoccer.xo.android.dialog;

import android.view.View;
import android.widget.*;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.SimpleContactsAdapter;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

public class GroupManageDialog extends DialogFragment {

    private static final Logger LOG = Logger.getLogger(GroupManageDialog.class);

    XoActivity mActivity;

    TalkClientContact mGroup;

    ContactsAdapter mAdapter;

    public GroupManageDialog(XoActivity activity, TalkClientContact group) {
        super();
        mActivity = activity;
        mGroup = group;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LOG.debug("onCreateDialog()");
        if(mAdapter == null) {
            mAdapter = new SimpleContactsAdapter(mActivity);
            mAdapter.onCreate();
            mAdapter.onResume();
            mAdapter.setFilter(new ContactsAdapter.Filter() {
                @Override
                public boolean shouldShow(TalkClientContact contact) {
                    return contact.isClientGroupJoined(mGroup);
                }
            });
        }
        mAdapter.requestReload();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.manage_title);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Ok)");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Cancel)");
                dialog.dismiss();
            }
        });
        builder.setAdapter(mAdapter, null);

        final AlertDialog dialog = builder.create();
        dialog.getListView().setItemsCanFocus(false);
        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                //RelativeLayout contactView = (RelativeLayout) view;
                //CheckedTextView checkedTextView = (CheckedTextView)contactView.findViewById(R.id.contact_name);

                Object object = mAdapter.getItem(index);
                if (object != null && object instanceof TalkClientContact) {
                    /*
                    if (checkedTextView.isChecked()) {
                        TalkClientContact contact = (TalkClientContact)object;
                        mActivity.getXoClient().inviteClientToGroup(mGroup.getGroupId(), contact.getClientId());
                    } else {
                        TalkClientContact contact = (TalkClientContact)object;
                        mActivity.getXoClient().kickClientFromGroup(mGroup.getGroupId(), contact.getClientId());
                    }
                    */
                }
            }
        });

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mAdapter != null) {
            mAdapter.onPause();
            mAdapter.onDestroy();
        }
    }
}
