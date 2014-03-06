package com.hoccer.xo.android.dialog;

import android.view.View;
import android.widget.*;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.GroupManagementContactsAdapter;
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

    private TalkClientContact mGroup;

    private ContactsAdapter mAdapter;
    private ArrayList<TalkClientContact> mContactsToInvite;
    private ArrayList<TalkClientContact> mContactsToKick;

    public GroupManageDialog() {
        super();

        mContactsToInvite = new ArrayList();
        mContactsToKick = new ArrayList();
    }

    public GroupManageDialog(TalkClientContact group) {
        super();

        mGroup = group;
        mContactsToInvite = new ArrayList();
        mContactsToKick = new ArrayList();
    }

    private void restoreDialog(Bundle savedInstanceState) {
        mGroup = (TalkClientContact)savedInstanceState.getSerializable("groupContact");
        mContactsToInvite = (ArrayList<TalkClientContact>)savedInstanceState.getSerializable("contactsToInvite");
        mContactsToKick = (ArrayList<TalkClientContact>)savedInstanceState.getSerializable("contactsToKick");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LOG.debug("onCreateDialog()");
        if (savedInstanceState != null) {
            restoreDialog(savedInstanceState);
        }
        if(mAdapter == null) {
            mAdapter = new GroupManagementContactsAdapter((XoActivity)getActivity(), mGroup, mContactsToInvite, mContactsToKick);
            mAdapter.onCreate();
            mAdapter.onResume();
            mAdapter.setFilter(new ContactsAdapter.Filter() {
                @Override
                public boolean shouldShow(TalkClientContact contact) {
                    return (!contact.isGroup());
                }
            });
        }
        mAdapter.requestReload();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.manage_title);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Ok)");
                updateMemberships();
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
                LinearLayout contactView = (LinearLayout) view;
                CheckedTextView checkedTextView = (CheckedTextView)contactView.findViewById(R.id.contact_name);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                Object object = mAdapter.getItem(index);
                if (object != null && object instanceof TalkClientContact) {
                    TalkClientContact contact = (TalkClientContact)object;
                    if (checkedTextView.isChecked()) {

                        mContactsToInvite.add(contact);

                        if (mContactsToKick.contains(contact)) {
                            mContactsToKick.remove(contact);
                        }

                    } else {

                        mContactsToKick.add(contact);

                        if (mContactsToInvite.contains(contact)) {
                            mContactsToInvite.remove(contact);
                        }
                    }
                }
            }
        });

        return dialog;
    }

    private void updateMemberships() {
        for (TalkClientContact contact : mContactsToInvite) {
            ((XoActivity) getActivity()).getXoClient().inviteClientToGroup(mGroup.getGroupId(), contact.getClientId());
        }
        for (TalkClientContact contact : mContactsToKick) {
            ((XoActivity) getActivity()).getXoClient().kickClientFromGroup(mGroup.getGroupId(), contact.getClientId());
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mAdapter != null) {
            mAdapter.onPause();
            mAdapter.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("groupContact", mGroup);
        outState.putSerializable("contactsToInvite", mContactsToInvite);
        outState.putSerializable("contactsToKick", mContactsToKick);
    }
}
