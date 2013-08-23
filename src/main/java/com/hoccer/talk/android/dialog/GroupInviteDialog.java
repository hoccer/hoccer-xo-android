package com.hoccer.talk.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.client.model.TalkClientContact;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupInviteDialog extends SherlockDialogFragment {

    private static final Logger LOG = Logger.getLogger(GroupInviteDialog.class);

    TalkActivity mActivity;

    TalkClientContact mGroup;

    public GroupInviteDialog(TalkActivity activity, TalkClientContact group) {
        super();
        mActivity = activity;
        mGroup = group;
    }

    private static final String KEY_NAME = "name";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();

        Map<String, Object> x = new HashMap<String, Object>();
        x.put(KEY_NAME, "Foo");
        options.add(x);

        SimpleAdapter adapter =
                new SimpleAdapter(mActivity, options, R.layout.select_client,
                        new String[]{KEY_NAME},
                        new int[]{R.id.select_client_name});

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.invite_title);
        builder.setAdapter(adapter, null);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
