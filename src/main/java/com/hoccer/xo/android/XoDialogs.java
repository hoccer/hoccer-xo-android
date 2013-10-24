package com.hoccer.xo.android;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.dialog.ContactDeleteDialog;
import com.hoccer.xo.android.dialog.ContactDepairDialog;
import com.hoccer.xo.android.dialog.GroupInviteDialog;
import com.hoccer.xo.android.dialog.GroupKickDialog;
import com.hoccer.xo.android.dialog.GroupLeaveDialog;
import com.hoccer.xo.android.dialog.NameDialog;
import com.hoccer.xo.android.dialog.TokenDialog;

public class XoDialogs {

    public final static String DIALOG_NAME = "NameDialog";
    public final static String DIALOG_TOKEN = "TokenDialog";
    public final static String DIALOG_CONTACT_DELETE = "ContactDeleteDialog";
    public final static String DIALOG_CONTACT_DEPAIR = "ContactDepairDialog";
    public final static String DIALOG_GROUP_KICK = "GroupKickDialog";
    public final static String DIALOG_GROUP_INVITE = "GroupInviteDialog";

    public static void changeName(XoActivity activity, TalkClientContact contact) {
        new NameDialog(activity, contact)
                .show(activity.getSupportFragmentManager(), DIALOG_NAME);
    }

    public static void confirmDeleteContact(XoActivity activity, TalkClientContact contact) {
        new ContactDeleteDialog(activity, contact)
                .show(activity.getSupportFragmentManager(), DIALOG_CONTACT_DELETE);
    }

    public static void confirmDepairContact(XoActivity activity, TalkClientContact contact) {
        new ContactDepairDialog(activity, contact)
                .show(activity.getSupportFragmentManager(), DIALOG_CONTACT_DEPAIR);
    }

    public static void confirmGroupLeave(XoActivity activity, TalkClientContact group) {
        new GroupLeaveDialog(activity, group)
                .show(activity.getSupportFragmentManager(), DIALOG_GROUP_KICK);
    }

    public static void selectGroupInvite(XoActivity activity, TalkClientContact group) {
        new GroupInviteDialog(activity, group)
                .show(activity.getSupportFragmentManager(), DIALOG_GROUP_INVITE);
    }

    public static void selectGroupKick(XoActivity activity, TalkClientContact group) {
        new GroupKickDialog(activity, group)
                .show(activity.getSupportFragmentManager(), DIALOG_GROUP_KICK);
    }

    public static void showTokenDialog(XoActivity activity, TalkClientSmsToken token) {
        new TokenDialog(activity, token)
                .show(activity.getSupportFragmentManager(), DIALOG_TOKEN);
    }

}
