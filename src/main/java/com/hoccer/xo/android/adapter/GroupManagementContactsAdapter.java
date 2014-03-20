package com.hoccer.xo.android.adapter;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;

import java.util.ArrayList;

/**
 * Contacts adapter for group management lists
 *
 * This displays the avatar and the name of a contact and a checkbox.
 *
 * It is used mostly for managing users in a group context.
 *
 */
public class GroupManagementContactsAdapter extends ContactsAdapter {

    private TalkClientContact mGroup;
    private ArrayList<TalkClientContact> mContactsToInvite;
    private ArrayList<TalkClientContact> mContactsToKick;

    public GroupManagementContactsAdapter(XoActivity activity, TalkClientContact group, ArrayList<TalkClientContact> contactsToInvite, ArrayList<TalkClientContact> contactsToKick) {
        super(activity);

        mGroup = group;
        mContactsToInvite = contactsToInvite;
        mContactsToKick = contactsToKick;
    }

    @Override
    protected int getClientLayout() {
        return R.layout.item_contact_checked;
    }

    @Override
    protected int getGroupLayout() {
        return R.layout.item_contact_checked;
    }

    @Override
    protected int getSeparatorLayout() {
        return R.layout.item_contact_separator;
    }

    @Override
    protected int getTokenLayout() {
        return -1;
    }

    @Override
    protected void updateToken(View view, TalkClientSmsToken token) {
        LOG.debug("updateToken(" + token.getSmsTokenId() + ")");
    }

    @Override
    protected void updateContact(final View view, final TalkClientContact contact) {
        LOG.debug("updateContact(" + contact.getClientContactId() + ")");
        CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.contact_name_checked);
        checkedTextView.setText(contact.getName());

        AvatarView avatarView = (AvatarView) view.findViewById(R.id.contact_icon);
        avatarView.setContact(contact);

        if (contact.isClientGroupInvited(mGroup) || contact.isClientGroupJoined(mGroup)) {
            checkedTextView.setChecked(true);
        } else {
            checkedTextView.setChecked(false);
        }

        if (mContactsToInvite.contains(contact)) {
            checkedTextView.setChecked(true);
        } else if (mContactsToKick.contains(contact)) {
            checkedTextView.setChecked(false);
        }

    }

}
