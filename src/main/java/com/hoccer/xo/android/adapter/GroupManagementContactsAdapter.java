package com.hoccer.xo.android.adapter;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;

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

    public GroupManagementContactsAdapter(XoActivity activity, TalkClientContact group) {
        super(activity);

        mGroup = group;
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
        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        nameView.setText(contact.getName());

        AvatarView avatarView = (AvatarView) view.findViewById(R.id.contact_icon);
        avatarView.setContact(contact);

        CheckedTextView checkedTextView = (CheckedTextView)view.findViewById(R.id.contact_name);
        if (contact.isClientGroupInvited(mGroup) || contact.isClientGroupJoined(mGroup)) {
            checkedTextView.setChecked(true);
        } else {
            checkedTextView.setChecked(false);
        }
    }

}
