package com.hoccer.xo.android.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.release.R;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * Contacts adapter for simple lists
 *
 * This displays just the avatar and the name of a contact.
 *
 * It is used mostly for selecting users in a group context.
 *
 */
public class SimpleContactsAdapter extends ContactsAdapter {

    public SimpleContactsAdapter(XoActivity activity) {
        super(activity);
    }

    @Override
    protected int getClientLayout() {
        return R.layout.item_contact_simple;
    }

    @Override
    protected int getGroupLayout() {
        return R.layout.item_contact_simple;
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
    }

    @Override
    protected void updateContact(final View view, final TalkClientContact contact) {
        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        nameView.setText(contact.getName());

        TalkClientDownload avatarDownload = contact.getAvatarDownload();
        ImageView iconView = (ImageView) view.findViewById(R.id.contact_icon);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showContactProfile(contact);
            }
        });
        String avatarUri;
        if(avatarDownload == null || !avatarDownload.getState().equals(TalkClientDownload.State.COMPLETE)) {
            if(contact.isGroup()) {
                avatarUri = "content://" + R.drawable.avatar_default_group;
            } else {
                avatarUri = "content://" + R.drawable.avatar_default_contact;
            }
        } else {
            File avatarFile = XoApplication.getAvatarLocation(avatarDownload);
            if(avatarFile != null) {
                avatarUri = "file://" + avatarFile.toString();
            } else {
                if(contact.isGroup()) {
                    avatarUri = "content://" + R.drawable.avatar_default_group;
                } else {
                    avatarUri = "content://" + R.drawable.avatar_default_contact;
                }
            }
        }
        ImageLoader.getInstance().displayImage(avatarUri, iconView);
    }

}
