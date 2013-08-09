package com.hoccer.talk.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.content.IContentSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttachDialogFragment extends SherlockDialogFragment
    implements DialogInterface.OnClickListener {

    Listener mListener;

    List<IContentSelector> mSelectors = new ArrayList<IContentSelector>();

    public AttachDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // XXX get selectors
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof Listener) {
            mListener = (Listener)activity;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setMessage("Please select source")
               .setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_NEGATIVE) {
            if(mListener != null) {
                mListener.onAttachDialogCanceled();
            }
            AttachDialogFragment.this.getDialog().cancel();
        }
    }

    String KEY_ICON = "icon";
    String KEY_TITLE = "title";
    String KEY_DESCRIPTION = "description";

    private void makeSelectorAdapter(Context context) {
        List<HashMap<String,Object>> allData = new ArrayList<HashMap<String, Object>>();
        for(IContentSelector selector: mSelectors) {
            Intent intent = selector.createSelectionIntent(context);
            if(intent != null) {
                HashMap<String, Object> data = new HashMap<String, Object>();
                allData.add(data);
            }
        }
        SimpleAdapter adapter =
                new SimpleAdapter(context, allData,
                    R.layout.dialog_attach_item,
                    new String[]{},
                    new int[]{});

    }

    public interface Listener {
        public void onAttachDialogSelected(IContentSelector selector);
        public void onAttachDialogCanceled();
    }

}
