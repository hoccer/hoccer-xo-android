package com.hoccer.xo.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

/**
 * This class contains static helper methods for dialogs.
 */
public class XoDialogs {

    private static final Logger LOG = Logger.getLogger(XoDialogs.class);

    public static void showYesNoDialog(final String tag, final int titleId, final int messageId, final Activity activity, final DialogInterface.OnClickListener yesListener, final DialogInterface.OnClickListener noListener) {
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

    public static void showOkDialog(final String tag, final int titleId, final int messageId, final Activity activity, final DialogInterface.OnClickListener okListener) {
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

    // extended onClick listener providing the password field content
    public interface OnPasswordClickListener {
        public void onClick(DialogInterface dialog, int id, String password);
    }

    public static void showPasswordDialog(final String tag, final int titleId, final Activity activity, final XoDialogs.OnPasswordClickListener okListener, final DialogInterface.OnClickListener cancelListener) {
        final LinearLayout passwordInputView = (LinearLayout)activity.getLayoutInflater().inflate(R.layout.view_password_input, null);
        final EditText passwordInput = (EditText) passwordInputView.findViewById(R.id.password_input);
        DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                LOG.debug("Creating dialog: " + tag);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(titleId);
                builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        okListener.onClick(dialog, id, passwordInput.getText().toString());
                    }
                });
                builder.setNegativeButton(R.string.common_cancel, cancelListener);
                builder.setView(passwordInputView);
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return dialog;
            }
        };
        dialogFragment.show(activity.getFragmentManager(), tag);
    }

    public interface OnOkClickListener {
        public void onClick(DialogInterface dialog, int which, String input);
    }
    public interface OnCancelClickListener {
        public void onClick(DialogInterface dialog, int which);
    }

    public static void showCreateDialog(final String tag, final int titleId, final int messageId, final Activity activity, final XoDialogs.OnOkClickListener okListener, final OnCancelClickListener cancelListener) {
        final View dialogView = (LinearLayout)activity.getLayoutInflater().inflate(R.layout.dialog_create_new_item, null);
        final EditText input = (EditText) dialogView.findViewById(R.id.et_input_name);
        final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                LOG.debug("Creating dialog: " + tag);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(titleId);
                builder.setMessage(messageId);
                builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        okListener.onClick(dialog, which, input.getText().toString());
                    }
                });
                builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        cancelListener.onClick(dialog, which);
                    }
                });
                builder.setView(dialogView);
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return dialog;
            }
        };
        dialogFragment.show(activity.getFragmentManager(), tag);
    }
}
