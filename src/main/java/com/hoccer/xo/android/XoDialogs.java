package com.hoccer.xo.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.dialog.*;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * This class contains static helper methods for dialogs.
 */
public class XoDialogs {

    private static final Logger LOG = Logger.getLogger(XoDialogs.class);

    public static void showYesNoDialog(final String tag, final int titleId, final int messageId, final XoActivity activity, final DialogInterface.OnClickListener yesListener, final DialogInterface.OnClickListener noListener) {
        DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                LOG.debug("Creating dialog: " + tag);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(titleId);
                builder.setMessage(messageId);
                builder.setNegativeButton(R.string.common_no, noListener);
                builder.setPositiveButton(R.string.common_yes, yesListener);
                return builder.create();
            }
        };
        dialogFragment.show(activity.getFragmentManager(), tag);
    }

    public static void showOkDialog(final String tag, final int titleId, final int messageId, final XoActivity activity, final DialogInterface.OnClickListener okListener) {
        DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                LOG.debug("Creating dialog: " + tag);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(titleId);
                builder.setMessage(messageId);
                builder.setNeutralButton(R.string.common_ok, okListener);
                return builder.create();
            }
        };
        dialogFragment.show(activity.getFragmentManager(), tag);
    }

    public final static String DIALOG_NAME = "NameDialog";
    public final static String DIALOG_TOKEN = "TokenDialog";
    public final static String DIALOG_CONTACT_DEPAIR = "ContactDepairDialog";
    public final static String DIALOG_GROUP_KICK = "GroupKickDialog";
    public final static String DIALOG_GROUP_INVITE = "GroupManageDialog";
    public final static String DIALOG_GROUP_MANAGE = "GroupManageDialog";

    public static void selectGroupManage(XoActivity activity, TalkClientContact group) {
        new GroupManageDialog(group)
                .show(activity.getFragmentManager(), DIALOG_GROUP_MANAGE);
    }

    public static void showTokenDialog(XoActivity activity, TalkClientSmsToken token) {
        new TokenDialog(activity, token)
                .show(activity.getFragmentManager(), DIALOG_TOKEN);
    }

}
