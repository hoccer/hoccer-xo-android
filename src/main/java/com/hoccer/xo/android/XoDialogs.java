package com.hoccer.xo.android;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.dialog.*;

/**
 * Static dialog launchers
 *
 * This class contains wrappers for launching dialogs.
 */
public class XoDialogs {

    public final static String DIALOG_NAME = "NameDialog";
    public final static String DIALOG_TOKEN = "TokenDialog";
    public final static String DIALOG_CONTACT_BLOCK = "ContactBlockDialog";
    public final static String DIALOG_CONTACT_DELETE = "ContactDeleteDialog";
    public final static String DIALOG_CONTACT_DEPAIR = "ContactDepairDialog";
    public final static String DIALOG_GROUP_DELETE = "GroupDeleteDialog";
    public final static String DIALOG_GROUP_REJECT_INVITATION = "GroupRejectInvitationDialog";
    public final static String DIALOG_GROUP_LEAVE = "GroupLeaveDialog";
    public final static String DIALOG_GROUP_KICK = "GroupKickDialog";
    public final static String DIALOG_GROUP_INVITE = "GroupManageDialog";
    public final static String DIALOG_GROUP_MANAGE = "GroupManageDialog";

    public static void confirmBlockContact(XoActivity activity, TalkClientContact contact) {
        new ContactBlockDialog(activity, contact)
                .show(activity.getFragmentManager(), DIALOG_CONTACT_BLOCK);
    }

    public static void confirmDeleteContact(XoActivity activity, TalkClientContact contact) {
        new ContactDeleteDialog(activity, contact)
                .show(activity.getFragmentManager(), DIALOG_CONTACT_DELETE);
    }

    public static void confirmDeleteGroup(XoActivity activity, TalkClientContact group) {
        new GroupDeleteDialog(activity, group)
                .show(activity.getFragmentManager(), DIALOG_GROUP_DELETE);
    }

    public static void confirmRejectInvitationGroup(XoActivity activity, TalkClientContact group) {
        new GroupRejectInvitationDialog(activity, group)
                .show(activity.getFragmentManager(), DIALOG_GROUP_REJECT_INVITATION);
    }

    public static void confirmLeaveGroup(XoActivity activity, TalkClientContact group) {
        new GroupLeaveDialog(activity, group)
                .show(activity.getFragmentManager(), DIALOG_GROUP_LEAVE);
    }

    public static void selectGroupManage(XoActivity activity, TalkClientContact group) {
        new GroupManageDialog(group)
                .show(activity.getFragmentManager(), DIALOG_GROUP_MANAGE);
    }

    public static void showTokenDialog(XoActivity activity, TalkClientSmsToken token) {
        new TokenDialog(activity, token)
                .show(activity.getFragmentManager(), DIALOG_TOKEN);
    }

}
