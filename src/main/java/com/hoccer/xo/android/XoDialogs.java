package com.hoccer.xo.android;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.dialog.ContactDeleteDialog;
import com.hoccer.xo.android.dialog.GroupManageDialog;
import com.hoccer.xo.android.dialog.TokenDialog;

/**
 * Static dialog launchers
 *
 * This class contains wrappers for launching dialogs.
 */
public class XoDialogs {

    public final static String DIALOG_NAME = "NameDialog";
    public final static String DIALOG_TOKEN = "TokenDialog";
    public final static String DIALOG_CONTACT_DELETE = "ContactDeleteDialog";
    public final static String DIALOG_CONTACT_DEPAIR = "ContactDepairDialog";
    public final static String DIALOG_GROUP_KICK = "GroupKickDialog";
    public final static String DIALOG_GROUP_INVITE = "GroupManageDialog";
    public final static String DIALOG_GROUP_MANAGE = "GroupManageDialog";

    public static void confirmDeleteContact(XoActivity activity, TalkClientContact contact) {
        new ContactDeleteDialog(activity, contact)
                .show(activity.getFragmentManager(), DIALOG_CONTACT_DELETE);
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
