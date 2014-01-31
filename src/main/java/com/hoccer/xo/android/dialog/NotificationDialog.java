package com.hoccer.xo.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import org.apache.log4j.Logger;


public class NotificationDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(NotificationDialog.class);
    private final int mMessageResource;
    private final int mTitleResource;

    public NotificationDialog(int titleResource, int messageResource){
        mMessageResource = messageResource;
        mTitleResource = titleResource;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TextView textView = new TextView(getSherlockActivity());
        textView.setText(mMessageResource);

        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setTitle(mTitleResource);
        builder.setView(textView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LOG.debug("onclick(OK)");
                dismiss();
            }
        });

        return builder.create();
    }
}