package com.hoccer.talk.android.content;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.content.audio.AudioSelector;
import com.hoccer.talk.android.content.image.ImageSelector;
import com.hoccer.talk.android.util.IntentHelper;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentRegistry {

    private static final Logger LOG = Logger.getLogger(ContentRegistry.class);

    private static ContentRegistry INSTANCE = null;

    public static synchronized ContentRegistry get(Context applicationContext) {
        if(INSTANCE == null) {
            INSTANCE = new ContentRegistry(applicationContext);
        }
        return INSTANCE;
    }

    Context mContext;

    ContentSelector mAvatarSelector;

    List<ContentSelector> mSelectors = new ArrayList<ContentSelector>();
    List<ContentViewer>   mViewers = new ArrayList<ContentViewer>();

    public ContentRegistry(Context context) {
        mContext = context;
        initializeSelectors();
    }

    public ContentSelection selectAvatar(Activity activity, int requestCode) {
        ContentSelection cs = new ContentSelection(activity, mAvatarSelector);
        Intent intent = mAvatarSelector.createSelectionIntent(activity);
        activity.startActivityForResult(intent, requestCode);
        return cs;
    }

    public ContentObject createSelectedAvatar(ContentSelection selection, Intent intent) {
        return selection.getSelector().createObjectFromSelectionResult(selection.getActivity(), intent);
    }

    private static final String KEY_ICON = "icon";
    private static final String KEY_NAME = "name";
    private static final String KEY_INTENT = "intent";

    public void selectAttachment(final Activity activity, final int requestCode) {

        final List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        for(ContentSelector selector: mSelectors) {
            Intent selectionIntent = selector.createSelectionIntent(activity);
            if(IntentHelper.isIntentResolvable(selectionIntent, activity)) {
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put(KEY_INTENT, selectionIntent);
                fields.put(KEY_ICON, IntentHelper.getIconForIntent(selectionIntent, activity));
                fields.put(KEY_NAME, selector.getName());
                options.add(fields);
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(activity, options, R.layout.item_appselect,
                new String[]{KEY_ICON, KEY_NAME},
                new int[]{R.id.item_appselect_icon, R.id.item_appselect_text});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView image = (ImageView)view.findViewById(R.id.item_appselect_icon);
                    image.setImageDrawable((Drawable)data);
                    return true;
                }
                return false;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> sel = options.get(which);
                Intent intent = (Intent)sel.get(KEY_INTENT);
                activity.startActivityForResult(intent, requestCode);
            }
        });

        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    private void initializeSelectors() {
        mAvatarSelector = new ImageSelector();

        mSelectors.add(new ImageSelector());
        mSelectors.add(new AudioSelector());
    }

}